package ru.nsu.ccfit.cinemaguesser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest

class ValueFlow<T>(private val flow: Flow<T>, private val getValue: () -> T) : Flow<T> by flow {
  val value
    get() = getValue()
}

fun <T> StateFlow<T>.asValueFlow() = ValueFlow(this) { value }

@OptIn(ExperimentalCoroutinesApi::class)
fun <T, R> ValueFlow<T>.mapValue(transform: (value: T) -> R) =
    ValueFlow(mapLatest(transform)) { transform(value) }

@Composable fun <T> ValueFlow<T>.collectValueAsState() = this.collectAsState(initial = value)

fun <T> ValueFlow<T>.distinctUntilChangedValue() = ValueFlow(distinctUntilChanged()) { value }
