package com.hoc.flowmvi.core_ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@OptIn(ExperimentalMaterial3Api::class)
@Stable
data class AppBarState(
  val title: String?,
  val actions: @Composable RowScope.() -> Unit,
  val navigationIcon: @Composable () -> Unit,
  val colors: TopAppBarColors,
)

typealias ConfigAppBar = (AppBarState?) -> Unit
