package com.hoc.flowmvi.ui.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoc.flowmvi.core.unit
import com.hoc.flowmvi.core_ui.navigator.Navigator
import com.hoc.flowmvi.core_ui.navigator.ProvideNavigator
import com.hoc.flowmvi.core_ui.rememberFlowWithLifecycle
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.ui.theme.AppTheme
import com.hoc.flowmvi.ui.theme.LoadingIndicator
import com.hoc.flowmvi.ui.theme.RetryButton
import com.hoc081098.flowext.startWith
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
  @Inject
  internal lateinit var navigator: Navigator

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      AppTheme {
        Surface(color = MaterialTheme.colors.background) {
          ProvideNavigator(navigator = navigator) {
            MainScreen()
          }
        }
      }
    }
  }
}

@Composable
private fun MainScreen(
  vm: MainVM = viewModel(),
  scaffoldState: ScaffoldState = rememberScaffoldState()
) {
  val singleEvent = rememberFlowWithLifecycle(vm.singleEvent)
  val state by vm.viewState.collectAsState()
  val intentChannel = remember { Channel<ViewIntent>(Channel.UNLIMITED) }

  LaunchedEffect(vm) {
    intentChannel
      .consumeAsFlow()
      .startWith(ViewIntent.Initial)
      .onEach(vm::processIntent)
      .collect()
  }

  LaunchedEffect(singleEvent, scaffoldState) {
    val snackbarHostState = scaffoldState.snackbarHostState

    singleEvent.collectLatest { event ->
      when (event) {
        SingleEvent.Refresh.Success -> {
          snackbarHostState.showSnackbar("Refresh success")
        }
        is SingleEvent.Refresh.Failure -> {
          snackbarHostState.showSnackbar("Refresh failure")
        }
        is SingleEvent.GetUsersError -> {
          snackbarHostState.showSnackbar("Get user failure")
        }
        is SingleEvent.RemoveUser.Success -> {
          snackbarHostState.showSnackbar("Removed '${event.user.fullName}'")
        }
        is SingleEvent.RemoveUser.Failure -> {
          snackbarHostState.showSnackbar("Error when removing '${event.user.fullName}'")
        }
      }.unit
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(text = "MVI Coroutines Flow")
        },
        actions = {
          CompositionLocalProvider(
            LocalContentAlpha provides ContentAlpha.medium
          ) {
            val navigator = Navigator.current
            val context = LocalContext.current

            IconButton(onClick = { navigator.run { context.navigateToAdd() } }) {
              Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(id = R.string.add_user)
              )
            }
          }
        },
      )
    },
    scaffoldState = scaffoldState,
  ) {
    MainContent(
      state = state,
      processIntent = intentChannel::trySend,
    )
  }
}

@Composable
private fun MainContent(
  state: ViewState,
  processIntent: (ViewIntent) -> Unit,
  modifier: Modifier = Modifier
) {
  if (state.error != null) {
    return RetryButton(
      errorMessage = when (state.error) {
        is UserError.InvalidId -> "Invalid id"
        UserError.NetworkError -> "Network error"
        UserError.ServerError -> "Server error"
        UserError.Unexpected -> "Unexpected error"
        is UserError.UserNotFound -> "User not found"
        is UserError.ValidationFailed -> "Validation failed"
      },
      onRetry = { processIntent(ViewIntent.Retry) },
      modifier = modifier,
    )
  }

  if (state.isLoading) {
    return LoadingIndicator(modifier)
  }

  UsersList(
    processIntent = processIntent,
    isRefreshing = state.isRefreshing,
    userItems = state.userItems,
  )
}
