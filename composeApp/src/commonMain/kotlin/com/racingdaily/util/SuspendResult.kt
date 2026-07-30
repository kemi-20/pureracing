package com.racingdaily.util

import kotlinx.coroutines.CancellationException

suspend inline fun <T> runSuspendCatching(
    crossinline block: suspend () -> T
): Result<T> = try {
    Result.success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (failure: Throwable) {
    Result.failure(failure)
}
