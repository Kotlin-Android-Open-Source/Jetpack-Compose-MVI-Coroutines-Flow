package com.hoc.flowmvi.core.dispatchers

import kotlinx.coroutines.CoroutineDispatcher

interface AppCoroutineDispatchers {
  val main: CoroutineDispatcher
  val io: CoroutineDispatcher
  val mainImmediate: CoroutineDispatcher
  val default: CoroutineDispatcher
}
