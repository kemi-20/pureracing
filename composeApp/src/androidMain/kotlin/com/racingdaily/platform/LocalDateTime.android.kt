package com.racingdaily.platform

import java.util.Calendar

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
