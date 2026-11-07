package com.example.delibuddy.data.profile

object RandomNicknameGenerator {
    private val adjectives = listOf(
        "용감한",
        "느긋한",
        "다정한",
        "신난",
        "씩씩한",
        "수줍은",
        "명랑한",
        "차분한",
        "상냥한",
        "귀여운",
        "코가 큰",
        "똑똑한",
        "꼬리가 긴"
    )
    private val animals = listOf(
        "수달",
        "토끼",
        "고양이",
        "강아지",
        "여우",
        "곰",
        "판다",
        "펭귄",
        "다람쥐",
        "고슴도치",
        "햄스터",
        "사슴",
        "코알라",
        "돌고래",
        "참새",
        "오리"
    )

    fun generate(): String = "${adjectives.random()} ${animals.random()}"
}
