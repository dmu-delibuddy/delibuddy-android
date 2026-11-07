package com.example.delibuddy.ui.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.delibuddy.R
import com.example.delibuddy.databinding.ItemChatMineBinding
import com.example.delibuddy.databinding.ItemChatOtherBinding
import com.example.delibuddy.databinding.ItemChatSystemBinding
import com.example.delibuddy.ui.common.loadImage
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

/*
    * 채팅 목록 어댑터
    * 말풍선 3종류 : 나(오른쪽) / 상대(왼쪽) / 시스템(가운데)
    * 실패한 내 메시지의 "다시 보내기"를 누르면 onRetryClick 으로 id 를 넘김
    * 사진을 누르면 onImageClick 으로 주소를 넘김 (크게 보기)
*/
class ChatAdapter(
    private val onRetryClick: (messageId: String) -> Unit,
    private val onImageClick: (imageUrl: String) -> Unit,
) : ListAdapter<ChatItem, RecyclerView.ViewHolder>(DIFF) {

    // 메시지 종류별 말풍선 번호
    override fun getItemViewType(position: Int): Int =
        when (val item = getItem(position)) {
            is ChatItem.System -> TYPE_SYSTEM
            is ChatItem.Bubble ->
                if (item.isMine) TYPE_MINE else TYPE_OTHER
        }

    // 종류에 맞는 말풍선 레이아웃 만들기
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_MINE -> MineHolder(
                ItemChatMineBinding.inflate(inflater, parent, false)
            )
            TYPE_OTHER -> OtherHolder(
                ItemChatOtherBinding.inflate(inflater, parent, false)
            )
            else -> SystemHolder(
                ItemChatSystemBinding.inflate(inflater, parent, false)
            )
        }
    }

    // position 번째 메시지를 말풍선에 채우기
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val item = getItem(position)
        when (holder) {
            is MineHolder -> holder.bind(item as ChatItem.Bubble)
            is OtherHolder -> holder.bind(item as ChatItem.Bubble)
            is SystemHolder -> holder.bind(item as ChatItem.System)
        }
    }

    // 내 말풍선 : 보내는 중이면 흐리게, 실패면 "다시 보내기" 표시
    inner class MineHolder(
        private val binding: ItemChatMineBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatItem.Bubble) = with(binding) {
            bindContent(textContent, imageAttachment, item)
            textTime.text = timeText(item)
            val alpha = if (item.sendState == SendState.SENDING) 0.5f else 1f
            textContent.alpha = alpha
            imageAttachment.alpha = alpha
            textFailed.isVisible =
                item.sendState == SendState.FAILED
            textFailed.setOnClickListener { onRetryClick(item.id) }
        }
    }

    // 상대 말풍선 : 위에 보낸 사람(방장 / 참여자) 표시
    inner class OtherHolder(
        private val binding: ItemChatOtherBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatItem.Bubble) = with(binding) {
            val fallback = if (item.isFromHost) R.string.detail_host else R.string.chat_sender_participant
            textSender.text = item.senderNickname?.let { nickname ->
                if (item.isFromHost) textSender.context.getString(R.string.chat_sender_host, nickname)
                else nickname
            } ?: textSender.context.getString(fallback)
            Glide.with(imageProfile)
                .load(item.senderImageUrl)
                .placeholder(R.drawable.mypage_icon)
                .error(R.drawable.mypage_icon)
                .fallback(R.drawable.mypage_icon)
                .circleCrop()
                .into(imageProfile)
            bindContent(textContent, imageAttachment, item)
            textTime.text = timeText(item)
        }
    }

    // 시스템 안내 : 가운데 작은 글씨
    inner class SystemHolder(
        private val binding: ItemChatSystemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatItem.System) {
            binding.textContent.text = item.content
        }
    }

    // 글 / 사진 채우기. 내용이 없으면 글 칸, 사진이 없으면 사진 칸을 숨김
    private fun bindContent(
        textView: TextView,
        imageView: ImageView,
        item: ChatItem.Bubble,
    ) {
        val text = contentText(textView.context, item)
        textView.text = text
        textView.isVisible = text.isNotEmpty()

        val showImage = item.hasImage && !item.deleted
        imageView.isVisible = showImage
        val url = item.imageUrl
        if (showImage && url != null) {
            imageView.loadImage(url)
            imageView.setOnClickListener { onImageClick(url) }
        } else {
            // 주소를 받기 전이면 빈 칸 (재사용된 칸의 예전 사진 지우기)
            Glide.with(imageView).clear(imageView)
            imageView.setOnClickListener(null)
        }
    }

    // 삭제된 메시지면 안내 문구, 아니면 내용
    private fun contentText(
        context: Context,
        item: ChatItem.Bubble,
    ): String =
        if (item.deleted) context.getString(R.string.chat_deleted)
        else item.content.orEmpty()

    // 저장 완료된 메시지만 시각 표시 (예 : 오후 6:30)
    private fun timeText(item: ChatItem.Bubble): String {
        if (item.sendState != SendState.SENT) return ""
        return item.createdAt
            ?.let { TIME_FORMAT.format(it.toDate()) }
            .orEmpty()
    }

    companion object {
        private const val TYPE_MINE = 0
        private const val TYPE_OTHER = 1
        private const val TYPE_SYSTEM = 2

        private val TIME_FORMAT =
            SimpleDateFormat("a h:mm", Locale.KOREA)

        private val DIFF = object : DiffUtil.ItemCallback<ChatItem>() {
            // 같은 메시지인지 id 로 판단
            override fun areItemsTheSame(
                old: ChatItem, new: ChatItem,
            ) = old.id == new.id

            // 내용이 같은지 (전송 상태 바뀌면 다시 그림)
            override fun areContentsTheSame(
                old: ChatItem, new: ChatItem,
            ) = old == new
        }
    }
}
