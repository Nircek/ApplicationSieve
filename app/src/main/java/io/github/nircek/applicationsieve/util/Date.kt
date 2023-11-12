package io.github.nircek.applicationsieve.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import java.lang.System.currentTimeMillis
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


fun timestamp(): String {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date())
}

fun delayFlow(durationMillis: Long) = flow {
    while (true) {
        val states = listOf("|", "/", "-", "\\")
        val state = (currentTimeMillis() / durationMillis) % states.size
        emit(states[state.toInt()])
        delay(durationMillis)
    }
}