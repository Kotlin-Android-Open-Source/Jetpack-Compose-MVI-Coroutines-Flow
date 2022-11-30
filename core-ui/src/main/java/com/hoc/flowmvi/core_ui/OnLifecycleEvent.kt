package com.hoc.flowmvi.core_ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

@Composable
fun OnLifecycleEvent(
  vararg keys: Any?,
  onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit
) {
  val eventHandler by rememberUpdatedState(onEvent)
  val lifecycleOwner = LocalLifecycleOwner.current

  DisposableEffect(*keys, lifecycleOwner) {
    val observer = LifecycleEventObserver { owner, event ->
      eventHandler(owner, event)
    }
    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }
}
