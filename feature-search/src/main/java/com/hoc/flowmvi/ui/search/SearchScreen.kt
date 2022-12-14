package com.hoc.flowmvi.ui.search

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import com.hoc.flowmvi.core_ui.AppBarState
import com.hoc.flowmvi.core_ui.ConfigAppBar
import com.hoc.flowmvi.core_ui.OnLifecycleEvent

@Composable
fun SearchUserRoute(
  configAppBar: ConfigAppBar,
  onBackClick: () -> Unit,
) {
  ConfigAppBar(
    onBackClickState = rememberUpdatedState(onBackClick),
    configAppBar = configAppBar
  )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ConfigAppBar(
  onBackClickState: State<() -> Unit>,
  configAppBar: ConfigAppBar
) {
  val title = stringResource(id = R.string.search_user)
  val colors = TopAppBarDefaults.centerAlignedTopAppBarColors()

  val appBarState = remember(title, colors) {
    AppBarState(
      title = title,
      actions = {},
      navigationIcon = {
        IconButton(onClick = { onBackClickState.value() }) {
          Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = "Back"
          )
        }
      },
      colors = colors
    )
  }
  OnLifecycleEvent(configAppBar, appBarState) { _, event ->
    if (event == Lifecycle.Event.ON_START) {
      configAppBar(appBarState)
    }
  }
}
