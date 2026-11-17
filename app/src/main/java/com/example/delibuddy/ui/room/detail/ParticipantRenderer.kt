package com.example.delibuddy.ui.room.detail

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.delibuddy.R
import com.example.delibuddy.data.profile.ProfileRepository
import com.example.delibuddy.data.profile.ProfileType
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.databinding.LayoutRoomDetailParticipantsBinding
import com.example.delibuddy.databinding.ItemRoomDetailParticipantBinding
import com.example.delibuddy.ui.common.profile.ProfileDialogFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ParticipantRenderer(
    private val fragment: Fragment,
    private val binding: LayoutRoomDetailParticipantsBinding,
) {
    private val profileRepository = ProfileRepository()
    private fun Int.dp(): Int = (this * fragment.resources.displayMetrics.density).toInt()

    fun render(room: RoomModel) {
        binding.textParticipantsTitle.text = fragment.getString(
            R.string.detail_participants,
            room.currentCount, room.maxParticipants
        )

        val myUid = FirebaseAuth.getInstance().currentUser?.uid
        val profileType = when (room.mode) {
            RoomMode.PICKUP_ONLY -> ProfileType.PICKUP_ONLY
            RoomMode.MEAL_BUDDY -> ProfileType.MEAL_BUDDY
        }
        val ordered = listOf(room.hostId) +
                room.participantIds.filter { it != room.hostId }

        val container = binding.layoutParticipants
        container.removeAllViews()
        val slots = maxOf(6, room.currentCount, room.maxParticipants)
        var row: LinearLayout? = null
        for (index in 0 until slots) {
            if (index % 3 == 0) {
                row = LinearLayout(fragment.requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                }
                container.addView(
                    row, LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = 8.dp() })
            }
            val cardRow = requireNotNull(row)
            val uid = ordered.getOrNull(index)
            val isMine = uid != null && uid == myUid
            val isHost = uid != null && uid == room.hostId
            val isEmptySlot = uid == null && index < room.maxParticipants
            val isUnavailable = uid == null && !isEmptySlot
            val fallbackName = when {
                isHost -> "방장"
                uid != null -> "참여자 ${index + 1}"
                index < room.maxParticipants -> "빈자리"
                else -> "모집 안 함"
            }
            val item = ItemRoomDetailParticipantBinding.inflate(
                LayoutInflater.from(fragment.requireContext()), cardRow, false
            )
            val card = item.root.apply {
                background = GradientDrawable().apply {
                    setColor(if (isUnavailable) Color.rgb(235, 237, 240) else Color.WHITE)
                    cornerRadius = 8.dp().toFloat()
                    val strokeColor = Color.rgb(120, 129, 145)
                    if (isEmptySlot) {
                        setStroke(1.dp(), strokeColor, 4.dp().toFloat(), 3.dp().toFloat())
                    } else {
                        setStroke(1.dp(), strokeColor)
                    }
                }
            }
            val avatar = item.imageProfile.apply {
                setImageResource(
                    when {
                        isEmptySlot -> R.drawable.question_mark_icon
                        isUnavailable -> R.drawable.x_mark_icon
                        else -> R.drawable.mypage_icon
                    }
                )
                contentDescription = if (uid != null) "$fallbackName 프로필 사진" else null
            }
            val name = item.textName.apply {
                text = fallbackName
                setTextColor(if (uid == null) Color.GRAY else Color.BLACK)
            }
            item.imageHost.isVisible = isHost
            item.textMe.isVisible = isMine
            cardRow.addView(card, LinearLayout.LayoutParams(0, 120.dp(), 1f).apply {
                if (index % 3 != 2) marginEnd = 8.dp()
            })

            if (uid != null) {
                card.setOnClickListener {
                    val manager = fragment.childFragmentManager
                    if (!manager.isStateSaved && manager.findFragmentByTag(ProfileDialogFragment.TAG) == null) {
                        ProfileDialogFragment.show(manager, uid, profileType)
                    }
                }
                fragment.viewLifecycleOwner.lifecycleScope.launch {
                    val profile = try {
                        profileRepository.getById(uid, profileType)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        null
                    }
                    if (card.parent !== cardRow || cardRow.parent !== container || profile == null) return@launch
                    val nickname = profile.nickname.takeIf { it.isNotBlank() }
                    val photoUrl = profile.imageUrl?.takeIf { it.isNotBlank() }
                    name.text = nickname ?: fallbackName
                    name.contentDescription = name.text
                    if (photoUrl != null) {
                        Glide.with(fragment)
                            .load(photoUrl)
                            .placeholder(R.drawable.mypage_icon)
                            .error(R.drawable.mypage_icon)
                            .circleCrop()
                            .into(avatar)
                    }
                }
            }
        }
    }
}
