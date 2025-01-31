/* Spotify Web API, Kotlin Wrapper; MIT License, 2017-2021; Original author: Adam Ratzman */
package com.adamratzman.spotify

import kotlin.coroutines.CoroutineContext
import kotlinx.cinterop.toKString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.runBlocking
import platform.posix.getenv

actual fun getEnvironmentVariable(name: String): String? {
    return getenv(name)?.toKString().apply { println("env variable $name: $this") }
}

actual fun Exception.stackTrace() = printStackTrace()

val testCoroutineContext: CoroutineContext = MainScope().coroutineContext
actual fun runBlockingTest(block: suspend CoroutineScope.() -> Unit) =
    runBlocking { block() }
