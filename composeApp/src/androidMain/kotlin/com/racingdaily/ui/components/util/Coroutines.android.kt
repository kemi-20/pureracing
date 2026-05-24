package com.racingdaily.ui.components.util

import kotlinx.coroutines.android.awaitFrame

actual suspend fun awaitFrame(): Unit = kotlinx.coroutines.android.awaitFrame()
