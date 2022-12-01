package com.hoc.flowmvi.core_ui

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.ContinuationInterceptor

suspend fun debugCheckImmediateMainDispatcher() {
  if (BuildConfig.DEBUG) {
    val interceptor = currentCoroutineContext()[ContinuationInterceptor]
    Log.d(
      "###",
      "debugCheckImmediateMainDispatcher: $interceptor, ${Dispatchers.Main.immediate}, ${Dispatchers.Main}"
    )

    check(interceptor === Dispatchers.Main.immediate) {
      "Expected ContinuationInterceptor to be Dispatchers.Main.immediate but was $interceptor"
    }
  }
}
