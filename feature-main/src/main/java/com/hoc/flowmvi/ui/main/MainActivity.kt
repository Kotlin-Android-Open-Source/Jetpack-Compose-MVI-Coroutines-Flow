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
import androidx.compose.material.SnackbarResult
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import arrow.core.Either
import arrow.core.right
import arrow.core.valueOr
import com.hoc.flowmvi.core.unit
import com.hoc.flowmvi.core_ui.LoadingIndicator
import com.hoc.flowmvi.core_ui.RetryButton
import com.hoc.flowmvi.core_ui.navigator.Navigator
import com.hoc.flowmvi.core_ui.navigator.ProvideNavigator
import com.hoc.flowmvi.core_ui.rememberFlowWithLifecycle
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.repository.UserRepository
import com.hoc.flowmvi.domain.usecase.GetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RefreshGetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RemoveUserUseCase
import com.hoc.flowmvi.ui.theme.AppTheme
import com.hoc081098.flowext.startWith
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
          Text(text = stringResource(id = R.string.app_name))
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
    val scope = rememberCoroutineScope()
    var job: Job? = remember { null }

    val onRemove: (UserItem) -> Unit = { item ->
      job?.cancel()
      job = scope.launch {
        val result = scaffoldState.snackbarHostState.showSnackbar(
          message = "Removed '${item.fullName}'",
          actionLabel = "OK",
        )
        when (result) {
          SnackbarResult.Dismissed -> {
            job = null
          }
          SnackbarResult.ActionPerformed -> {
            intentChannel.trySend(ViewIntent.RemoveUser(item))
          }
        }
      }
    }

    MainContent(
      state = state,
      onRefresh = { intentChannel.trySend(ViewIntent.Refresh) },
      onRemove = onRemove,
      onRetry = { intentChannel.trySend(ViewIntent.Retry) },
    )
  }
}

@Composable
private fun MainContent(
  state: ViewState,
  onRefresh: () -> Unit,
  onRemove: (UserItem) -> Unit,
  onRetry: () -> Unit,
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
      onRetry = onRetry,
      modifier = modifier,
    )
  }

  if (state.isLoading) {
    return LoadingIndicator(modifier)
  }

  UsersList(
    isRefreshing = state.isRefreshing,
    userItems = state.userItems,
    onRefresh = onRefresh,
    onRemove = onRemove,
  )
}

@Preview
@Composable
fun PreviewMainScreen() {
  val userRepository = object : UserRepository {
    override fun getUsers(): Flow<Either<UserError, List<User>>> {
      return flowOf(
        listOf(
          User.create(
            id = "1",
            email = "hoc081098@gmail.com",
            firstName = "first 1",
            lastName = "last 1",
            avatar = "",
          ).valueOr { error("") },
          User.create(
            id = "2",
            email = "mviflow@gmail.com",
            firstName = "first 2",
            lastName = "last 2",
            avatar = "",
          ).valueOr { error("") }
        ).right()
      )
    }

    override suspend fun refresh() = error("Not yet implemented")
    override suspend fun remove(user: User) = error("Not yet implemented")
    override suspend fun add(user: User) = error("Not yet implemented")
    override suspend fun search(query: String) = error("Not yet implemented")
  }

  MainScreen(
    vm = MainVM(
      getUsersUseCase = GetUsersUseCase(userRepository = userRepository),
      refreshGetUsers = RefreshGetUsersUseCase(userRepository = userRepository),
      removeUser = RemoveUserUseCase(userRepository = userRepository)
    )
  )
}
