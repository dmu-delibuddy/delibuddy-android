package com.example.delibuddy.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.delibuddy.R
import com.example.delibuddy.databinding.ItemChatParticipantBinding

data class ChatParticipant(
    val id: String,
    val nickname: String,
    val imageUrl: String?,
    val isHost: Boolean
)

class ChatParticipantAdapter : ListAdapter<ChatParticipant, ChatParticipantAdapter.Holder>(DIFF) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(
        ItemChatParticipantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    class Holder(private val binding: ItemChatParticipantBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(participant: ChatParticipant) {
            binding.imageHost.isVisible = participant.isHost
            binding.textName.text = participant.nickname.takeIf { it.isNotBlank() }
                ?: binding.root.context.getString(R.string.chat_sender_participant)
            Glide.with(binding.imageProfile)
                .load(participant.imageUrl?.takeIf { it.isNotBlank() })
                .placeholder(R.drawable.mypage_icon)
                .fallback(R.drawable.mypage_icon)
                .error(R.drawable.mypage_icon)
                .circleCrop()
                .into(binding.imageProfile)
            binding.imageProfile.contentDescription =
                participant.nickname.takeIf { it.isNotBlank() }
                    ?: binding.root.context.getString(R.string.chat_profile_image_desc)
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ChatParticipant>() {
            override fun areItemsTheSame(oldItem: ChatParticipant, newItem: ChatParticipant) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ChatParticipant, newItem: ChatParticipant) =
                oldItem == newItem
        }
    }
}
