package com.racingdaily.platform

data class LocalDateTimeParts(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int
)

expect fun currentLocalDateTimeParts(): LocalDateTimeParts
