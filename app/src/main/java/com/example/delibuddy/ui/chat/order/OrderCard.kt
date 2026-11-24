package com.example.delibuddy.ui.chat.order

import android.content.ContentResolver
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.delibuddy.R
import com.example.delibuddy.data.order.OrderModel
import com.example.delibuddy.databinding.LayoutRoomDetailOrderBinding

/*
    * 주문 카드(layout_room_detail_order) 공용 처리
    * 방 상세와 채팅방 내 주문 창이 같은 카드를 쓰므로 채우기 / 저장 규칙을 한 곳에 둔다
*/

// 메모 글자 수 표시
fun LayoutRoomDetailOrderBinding.setUpMemoCount() {
    inputOrderMemo.doAfterTextChanged {
        textMemoCount.text = root.context.getString(R.string.my_order_memo_count, it?.length ?: 0)
    }
}

// 저장된 주문 채우기 (입력 중인 값은 그대로 두고, 새로 고른 사진이 있으면 사진도 그대로 둠)
fun LayoutRoomDetailOrderBinding.fill(order: OrderModel, hasNewPhoto: Boolean, fragment: Fragment) {
    val price = orderPriceAmount.editText
    if (price.text.isEmpty()) price.setText(order.price.toString())
    if (inputOrderMemo.text.isEmpty()) inputOrderMemo.setText(order.memo.orEmpty())
    val url = order.cartScreenshotUrl
    if (!hasNewPhoto && !url.isNullOrBlank()) {
        Glide.with(fragment).load(url).into(imageOrderPreview)
    }
}

// 입력한 음식값 / 메모로 저장 요청
fun LayoutRoomDetailOrderBinding.saveTo(viewModel: MyOrderViewModel, resolver: ContentResolver) {
    viewModel.save(
        price = orderPriceAmount.editText.text.toString().trim().toLongOrNull(),
        memo = inputOrderMemo.text.toString(),
        resolver = resolver,
    )
}
