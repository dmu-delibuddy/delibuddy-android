package com.example.delibuddy.ui.room.detail

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.delibuddy.R
import com.example.delibuddy.data.profile.ProfileRepository
import com.example.delibuddy.data.profile.ProfileType
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.databinding.LayoutRoomDetailParticipantsBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ParticipantRenderer(
    private val fragment: Fragment,
    private val binding: LayoutRoomDetailParticipantsBinding,
) {
    private val profileRepository = ProfileRepository()
    private fun Int.dp(): Int = (this * fragment.resources.displayMetrics.density).toInt()

    fun render(room : RoomModel) {
        binding.textParticipantsTitle.text = fragment.getString(
            R.string.detail_participants,
            room.currentCount, room.maxParticipants)

        val myUid = FirebaseAuth.getInstance().currentUser?.uid
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
                container.addView(row, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 8.dp() })
            }
            val cardRow = requireNotNull(row)
            val uid = ordered.getOrNull(index)
            val isMine = uid != null && uid == myUid
            val isEmptySlot = uid == null && index < room.maxParticipants
            val isUnavailable = uid == null && !isEmptySlot
            val fallbackName = when {
                uid == room.hostId -> "방장"
                uid == myUid && uid != null -> "나"
                uid != null -> "참여자 ${index + 1}"
                index < room.maxParticipants -> "빈자리"
                else -> "모집 안 함"
            }
            val card = LinearLayout(fragment.requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                setPadding(4.dp(), 10.dp(), 4.dp(), 10.dp())
                background = GradientDrawable().apply {
                    setColor(if (isUnavailable) Color.rgb(235, 237, 240) else Color.WHITE)
                    cornerRadius = 8.dp().toFloat()
                    val strokeColor = if (isMine) {
                        ContextCompat.getColor(context, R.color.point_orange)
                    } else {
                        Color.rgb(120, 129, 145)
                    }
                    if (isEmptySlot) {
                        setStroke(1.dp(), strokeColor, 4.dp().toFloat(), 3.dp().toFloat())
                    } else {
                        setStroke(if (isMine) 2.dp() else 1.dp(), strokeColor)
                    }
                }
            }
            val avatar = ImageView(fragment.requireContext()).apply {
                setImageResource(R.drawable.mypage_icon)
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = if (uid != null) "$fallbackName 프로필 사진" else null
                background = GradientDrawable().apply {
                    setColor(Color.rgb(245, 246, 248))
                    cornerRadius = 24.dp().toFloat()
                }
                clipToOutline = true
            }
            card.addView(avatar, LinearLayout.LayoutParams(48.dp(), 48.dp()))
            val name = TextView(fragment.requireContext()).apply {
                text = fallbackName
                textSize = 15f
                gravity = android.view.Gravity.CENTER
                setTextColor(if (uid == null) Color.GRAY else Color.BLACK)
                setPadding(0, 6.dp(), 0, 0)
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            }
            card.addView(name, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            cardRow.addView(card, LinearLayout.LayoutParams(0, 100.dp(), 1f).apply {
                if (index % 3 != 2) marginEnd = 8.dp()
            })

            if (uid != null) {
                fragment.viewLifecycleOwner.lifecycleScope.launch {
                    val profile = try {
                        val profileType = when (room.mode) {
                            RoomMode.PICKUP_ONLY -> ProfileType.PICKUP_ONLY
                            RoomMode.MEAL_BUDDY -> ProfileType.MEAL_BUDDY
                        }
                        profileRepository.getById(uid, profileType).await()
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
