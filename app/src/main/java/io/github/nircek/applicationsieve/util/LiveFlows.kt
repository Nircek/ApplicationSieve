package io.github.nircek.applicationsieve.util

import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class LiveFlowSuppl<T>(flow: Flow<T>) {
    val live = flow.asLiveData()
}

class LiveFlow<T>(flow: Flow<T>) :
    LiveFlowSuppl<T>(flow), Flow<T> by flow

class LiveStateFlow<T>(flow: StateFlow<T>) :
    LiveFlowSuppl<T>(flow), StateFlow<T> by flow

class MutableLiveStateFlow<T>(flow: MutableStateFlow<T>) :
    LiveFlowSuppl<T>(flow), MutableStateFlow<T> by flow


fun <T> Flow<T>.toLiveFlow() = LiveFlow(this)
fun <T> StateFlow<T>.toLiveFlow() = LiveStateFlow(this)
fun <T> MutableStateFlow<T>.toLiveFlow() = MutableLiveStateFlow(this)
