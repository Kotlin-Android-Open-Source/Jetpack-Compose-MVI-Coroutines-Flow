package com.hoc.flowmvi.core_ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

/**
 * A composition local for [SnackbarHostState].
 */
val LocalSnackbarHostState =
  compositionLocalOf<SnackbarHostState> { error("No SnackbarHostState provided") }

@Composable
fun ProvideSnackbarHostState(
  snackbarHostState: SnackbarHostState,
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(
    LocalSnackbarHostState provides snackbarHostState,
    content = content
  )
}
