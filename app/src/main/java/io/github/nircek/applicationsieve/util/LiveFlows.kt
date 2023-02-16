package io.github.nircek.applicationsieve.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LiveFlow<T>(private val flow: Flow<T>) : Flow<T> {
    val live: LiveData<T> = flow.asLiveData()

    override suspend fun collect(collector: FlowCollector<T>) = flow.collect(collector)
}

class LiveStateFlow<T>(private val flow: StateFlow<T>) : StateFlow<T> {
    val live = flow.asLiveData()

    override suspend fun collect(collector: FlowCollector<T>) = flow.collect(collector)
    override val replayCache by flow::replayCache
    override val value by flow::value
}

@ExperimentalCoroutinesApi
class MutableLiveStateFlow<T>(private val flow: MutableStateFlow<T>) : MutableStateFlow<T> {
    val live: LiveData<T> = flow.asLiveData()

    override suspend fun collect(collector: FlowCollector<T>) = flow.collect(collector)
    override val replayCache by flow::replayCache
    override val subscriptionCount = flow.subscriptionCount
    override var value by flow::value
    override fun compareAndSet(expect: T, update: T) = flow.compareAndSet(expect, update)
    override fun resetReplayCache() = flow.resetReplayCache()
    override fun tryEmit(value: T) = flow.tryEmit(value)
    override suspend fun emit(value: T) = flow.emit(value)
}
