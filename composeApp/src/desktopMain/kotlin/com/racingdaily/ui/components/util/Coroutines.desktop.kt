package com.racingdaily.ui.components.util

import kotlinx.coroutines.delay

actual suspend fun awaitFrame() {
    delay(16)
}
