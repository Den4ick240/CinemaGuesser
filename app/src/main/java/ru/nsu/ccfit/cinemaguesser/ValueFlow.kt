package ru.nsu.ccfit.cinemaguesser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class ValueFlow<T>(private val flow: Flow<T>, private val getValue: () -> T) : Flow<T> by flow {
  val value
    get() = getValue()
}

fun <T> StateFlow<T>.asValueFlow() = ValueFlow(this) { value }

@Composable fun <T> ValueFlow<T>.collectValueAsState() = this.collectAsState(initial = value)

fun <T> ValueFlow<T>.distinctUntilChangedValue() = ValueFlow(distinctUntilChanged()) { value }
