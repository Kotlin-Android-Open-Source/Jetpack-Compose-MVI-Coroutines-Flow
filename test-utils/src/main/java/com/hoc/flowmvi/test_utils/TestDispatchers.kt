package com.hoc.flowmvi.test_utils

import com.hoc.flowmvi.core.dispatchers.AppCoroutineDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher

@ExperimentalCoroutinesApi
class TestDispatchers(testCoroutineDispatcher: TestCoroutineDispatcher) :
  AppCoroutineDispatchers {
  override val main: CoroutineDispatcher = testCoroutineDispatcher
  override val io: CoroutineDispatcher = testCoroutineDispatcher
  override val mainImmediate: CoroutineDispatcher = testCoroutineDispatcher
  override val default: CoroutineDispatcher = testCoroutineDispatcher
}
