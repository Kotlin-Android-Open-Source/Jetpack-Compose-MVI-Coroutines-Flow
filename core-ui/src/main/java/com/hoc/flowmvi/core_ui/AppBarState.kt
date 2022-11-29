package com.hoc.flowmvi.core_ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

data class AppBarState(
  val title: String?,
  val actions: @Composable RowScope.() -> Unit,
  val navigationIcon: @Composable () -> Unit,
)

typealias ConfigAppBar = (AppBarState) -> Unit
