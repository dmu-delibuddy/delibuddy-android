package com.example.delibuddy.data.room

enum class RoomStatus {
    RECRUITING, ORDER_CONFIRMED, ORDERED, RECEIVED, COMPLETED;

    val next : RoomStatus?
        get() = entries.getOrNull(ordinal + 1)
}

enum class RoomMode { PICKUP_ONLY, MEAL_BUDDY }

enum class FoodCategory { KOR, CHN, JPN, WEST, ETC }

enum class FoodCostSplitMethod { HOST_PAYS, EQUAL_SPLIT, PROPORTIONAL_SPLIT, CUSTOM }

enum class DeliveryFeeSplitMethod { HOST_PAYS, EQUAL_SPLIT, CUSTOM }

enum class DeliveryApp { BAEMIN, COUPANG_EATS, YOGIYO, OTHER }
