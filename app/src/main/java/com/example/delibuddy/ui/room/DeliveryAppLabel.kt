package com.example.delibuddy.ui.room

import android.content.Context
import com.example.delibuddy.R
import com.example.delibuddy.data.room.DeliveryApp
import com.example.delibuddy.data.room.RoomModel

// 화면에 보여줄 배달 앱 이름 (방 카드 / 방 상세 공통)
// 직접 입력이면 입력한 이름, 그것도 비어 있으면(예전 방) "기타"
fun RoomModel.deliveryAppLabel(context: Context): String = when (deliveryApp) {
    DeliveryApp.BAEMIN -> context.getString(R.string.room_delivery_app_baemin)
    DeliveryApp.COUPANG_EATS -> context.getString(R.string.room_delivery_app_coupang_eats)
    DeliveryApp.YOGIYO -> context.getString(R.string.room_delivery_app_yogiyo)
    DeliveryApp.OTHER -> deliveryAppName
        .ifBlank { context.getString(R.string.room_delivery_app_unknown) }
}
