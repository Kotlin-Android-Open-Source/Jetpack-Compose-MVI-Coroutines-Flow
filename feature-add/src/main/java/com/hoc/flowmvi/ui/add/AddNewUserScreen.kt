package com.hoc.flowmvi.ui.add

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import com.hoc.flowmvi.core_ui.AppBarState
import com.hoc.flowmvi.core_ui.ConfigAppBar
import com.hoc.flowmvi.core_ui.OnLifecycleEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddNewUserRoute(
  configAppBar: ConfigAppBar,
  onBackClick: () -> Unit,
) {
  val title = stringResource(id = R.string.add_new_user)
  val colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
  val appBarState = remember(colors) {
    AppBarState(
      title = title,
      actions = {},
      navigationIcon = {
        IconButton(onClick = onBackClick) {
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

  Text(
    text = "ADD NEW USER",
  )
}
