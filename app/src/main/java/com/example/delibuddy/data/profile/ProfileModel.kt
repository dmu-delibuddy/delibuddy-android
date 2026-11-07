package com.example.delibuddy.data.profile

enum class ProfileType {
    PICKUP_ONLY,
    MEAL_BUDDY
}

data class ProfileModel(
    var type: ProfileType = ProfileType.PICKUP_ONLY,
    var nickname: String = "",
    var bio: String? = "",
    var imageUrl: String? = ""
)
