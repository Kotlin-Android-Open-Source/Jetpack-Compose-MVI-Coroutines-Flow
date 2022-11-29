package com.hoc.flowmvi.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoc.flowmvi.core_ui.AppBarState
import com.hoc.flowmvi.core_ui.ConfigAppBar
import com.hoc.flowmvi.core_ui.LocalSnackbarHostState
import com.hoc.flowmvi.core_ui.OnLifecycleEvent
import com.hoc.flowmvi.core_ui.collectInLaunchedEffectWithLifecycle
import com.hoc081098.flowext.startWith
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun UsersListRoute(
  configAppBar: ConfigAppBar,
  modifier: Modifier = Modifier,
  viewModel: MainVM = hiltViewModel(),
) {
  val currentConfigAppBar by rememberUpdatedState(configAppBar)
  val title = stringResource(id = R.string.app_name)

  OnLifecycleEvent { _, event ->
    if (event == Lifecycle.Event.ON_START) {
      currentConfigAppBar(
        AppBarState(
          title = title,
          actions = {},
          navigationIcon = {},
        )
      )
    }
  }

  val intentChannel = remember { Channel<ViewIntent>(Channel.UNLIMITED) }
  LaunchedEffect(Unit) {
    intentChannel
      .consumeAsFlow()
      .startWith(ViewIntent.Initial)
      .onEach(viewModel::processIntent)
      .collect()
  }

  val snackbarHostState = LocalSnackbarHostState.current
  viewModel.singleEvent.collectInLaunchedEffectWithLifecycle { event ->
    when (event) {
      SingleEvent.Refresh.Success -> {
        launch {
          snackbarHostState.showSnackbar("Refresh successfully")
        }
      }
      is SingleEvent.Refresh.Failure -> {
        launch {
          snackbarHostState.showSnackbar("Failed to refresh")
        }
      }
      is SingleEvent.GetUsersError -> {
        launch {
          snackbarHostState.showSnackbar("Failed to get users")
        }
      }
      is SingleEvent.RemoveUser.Success -> {
        launch {
          snackbarHostState.showSnackbar("Removed '${event.user.fullName}'")
        }
      }
      is SingleEvent.RemoveUser.Failure -> {
        launch {
          snackbarHostState.showSnackbar("Failed to remove '${event.user.fullName}'")
        }
      }
    }
  }

  val viewState by viewModel.viewState.collectAsStateWithLifecycle()
}
