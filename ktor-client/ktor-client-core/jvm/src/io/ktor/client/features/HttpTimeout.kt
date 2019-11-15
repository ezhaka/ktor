/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.features

import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import java.net.*

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class HttpConnectTimeoutException : ConnectException("Connect timeout has been expired")

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class HttpSocketTimeoutException : SocketTimeoutException("Socket timeout has been expired")

/**
 * Returns [ByteReadChannel] with [ByteChannel.close] handler that returns [HttpSocketTimeoutException] instead of
 * [SocketTimeoutException].
 */
@InternalAPI
fun CoroutineScope.mapEngineExceptions(input: ByteReadChannel): ByteReadChannel = ByteChannel(autoFlush = true).also {
    launch {
        input.joinTo(
            object : ByteChannel by it {
                override fun close(cause: Throwable?): Boolean {
                    val mappedCause = when (cause?.rootCause) {
                        is SocketTimeoutException -> HttpSocketTimeoutException()
                        else -> cause
                    }

                    return it.close(mappedCause)
                }
            },
            closeOnEnd = true
        )
    }
}
