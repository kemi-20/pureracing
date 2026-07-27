package com.racingdaily.platform

import java.util.Calendar
import java.util.TimeZone

actual fun currentLocalDateTimeParts(): LocalDateTimeParts {
    val calendar = Calendar.getInstance()
    return LocalDateTimeParts(
        year = calendar.get(Calendar.YEAR),
        month = calendar.get(Calendar.MONTH) + 1,
        day = calendar.get(Calendar.DAY_OF_MONTH),
        hour = calendar.get(Calendar.HOUR_OF_DAY),
        minute = calendar.get(Calendar.MINUTE)
    )
}

actual fun raceCalendarUtcToLocal(utcTimestamp: String): LocalDateTimeParts? =
    runCatching {
        val value = utcTimestamp.removeSuffix("Z")
        val source = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(
                value.substring(0, 4).toInt(),
                value.substring(4, 6).toInt() - 1,
                value.substring(6, 8).toInt(),
                value.substring(9, 11).toInt(),
                value.substring(11, 13).toInt()
            )
        }
        val local = Calendar.getInstance().apply { timeInMillis = source.timeInMillis }
        LocalDateTimeParts(
            year = local.get(Calendar.YEAR),
            month = local.get(Calendar.MONTH) + 1,
            day = local.get(Calendar.DAY_OF_MONTH),
            hour = local.get(Calendar.HOUR_OF_DAY),
            minute = local.get(Calendar.MINUTE)
        )
    }.getOrNull()
