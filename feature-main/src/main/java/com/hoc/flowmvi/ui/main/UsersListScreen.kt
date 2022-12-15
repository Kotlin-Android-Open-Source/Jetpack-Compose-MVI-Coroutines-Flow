package com.hoc.flowmvi.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.hoc.flowmvi.core_ui.AppBarState
import com.hoc.flowmvi.core_ui.ConfigAppBar
import com.hoc.flowmvi.core_ui.LoadingIndicator
import com.hoc.flowmvi.core_ui.LocalSnackbarHostState
import com.hoc.flowmvi.core_ui.OnLifecycleEvent
import com.hoc.flowmvi.core_ui.RetryButton
import com.hoc.flowmvi.core_ui.collectInLaunchedEffectWithLifecycle
import com.hoc.flowmvi.core_ui.debugCheckImmediateMainDispatcher
import com.hoc.flowmvi.domain.model.UserError
import com.hoc081098.flowext.startWith
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun UsersListRoute(
  configAppBar: ConfigAppBar,
  navigateToAddUser: () -> Unit,
  navigateToSearchUser: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: MainVM = hiltViewModel(),
) {
  ConfigAppBar(
    navigateToAddUser = navigateToAddUser,
    navigateToSearchUser = navigateToSearchUser,
    configAppBar = configAppBar
  )

  val intentChannel = remember { Channel<ViewIntent>(Channel.UNLIMITED) }
  LaunchedEffect(Unit) {
    withContext(Dispatchers.Main.immediate) {
      intentChannel
        .consumeAsFlow()
        .startWith(ViewIntent.Initial)
        .onEach(viewModel::processIntent)
        .collect()
    }
  }

  val snackbarHostState by rememberUpdatedState(LocalSnackbarHostState.current)
  val scope = rememberCoroutineScope()
  viewModel.singleEvent.collectInLaunchedEffectWithLifecycle { event ->
    debugCheckImmediateMainDispatcher()

    when (event) {
      SingleEvent.Refresh.Success -> {
        scope.launch {
          snackbarHostState.showSnackbar("Refresh successfully")
        }
      }
      is SingleEvent.Refresh.Failure -> {
        scope.launch {
          snackbarHostState.showSnackbar("Failed to refresh")
        }
      }
      is SingleEvent.GetUsersError -> {
        scope.launch {
          snackbarHostState.showSnackbar("Failed to get users")
        }
      }
      is SingleEvent.RemoveUser.Success -> {
        scope.launch {
          snackbarHostState.showSnackbar("Removed '${event.user.fullName}'")
        }
      }
      is SingleEvent.RemoveUser.Failure -> {
        scope.launch {
          snackbarHostState.showSnackbar("Failed to remove '${event.user.fullName}'")
        }
      }
    }
  }

  val viewState by viewModel.viewState.collectAsStateWithLifecycle()
  val dispatch = remember {
    { intent: ViewIntent ->
      intentChannel.trySend(intent).getOrThrow()
    }
  }

  val (shouldBeDeletedItem, setShouldBeDeletedItem) = remember { mutableStateOf<UserItem?>(null) }
  if (shouldBeDeletedItem != null) {
    DeleteUserConfirmationDialog(
      shouldBeDeletedItem = shouldBeDeletedItem,
      onConfirm = {
        dispatch(ViewIntent.RemoveUser(it))
        setShouldBeDeletedItem(null)
      },
      onDismiss = {
        setShouldBeDeletedItem(null)
      },
    )
  }

  UsersListContent(
    modifier = modifier,
    viewState = viewState,
    onRetry = { dispatch(ViewIntent.Retry) },
    refresh = { dispatch(ViewIntent.Refresh) },
    removeItem = { setShouldBeDeletedItem(it) },
  )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ConfigAppBar(
  navigateToAddUser: () -> Unit,
  navigateToSearchUser: () -> Unit,
  configAppBar: ConfigAppBar
) {
  val title = stringResource(id = R.string.users_list_title)
  val colors = TopAppBarDefaults.centerAlignedTopAppBarColors()

  val currentNavigateToAddUser by rememberUpdatedState(navigateToAddUser)
  val currentNavigateToSearchUser by rememberUpdatedState(navigateToSearchUser)

  val appBarState = remember(colors, title) {
    AppBarState(
      title = { Text(text = title) },
      actions = {
        IconButton(onClick = { currentNavigateToAddUser() }) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add new user",
          )
        }

        IconButton(onClick = { currentNavigateToSearchUser() }) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search user",
          )
        }
      },
      navigationIcon = {},
      colors = colors,
    )
  }

  OnLifecycleEvent(configAppBar, appBarState) { _, event ->
    if (event == Lifecycle.Event.ON_START) {
      configAppBar(appBarState)
    }
  }
}

@Composable
private fun DeleteUserConfirmationDialog(
  shouldBeDeletedItem: UserItem,
  onDismiss: () -> Unit,
  onConfirm: (UserItem) -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Filled.Delete,
        contentDescription = null,
      )
    },
    title = {
      Text(
        text = stringResource(id = R.string.delete),
      )
    },
    text = {
      Text(
        text = stringResource(
          id = R.string.delete_user_confirmaion,
          shouldBeDeletedItem.fullName,
        ),
      )
    },
    confirmButton = {
      TextButton(onClick = { onConfirm(shouldBeDeletedItem) }) {
        Text(
          text = "OK"
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(
          text = "Cancel"
        )
      }
    },
  )
}

@Composable
private fun UsersListContent(
  viewState: ViewState,
  onRetry: () -> Unit,
  refresh: () -> Unit,
  removeItem: (UserItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
    modifier = modifier.fillMaxSize(),
    visible = viewState.error !== null,
    enter = fadeIn(),
    exit = fadeOut(),
  ) {
    RetryButton(
      modifier = Modifier
        .fillMaxSize(),
      onRetry = onRetry,
      errorMessage = viewState.error?.getReadableMessage() ?: "",
    )
  }

  AnimatedVisibility(
    modifier = modifier.fillMaxSize(),
    visible = viewState.isLoading,
    enter = fadeIn(),
    exit = fadeOut(),
  ) {
    LoadingIndicator(
      modifier = Modifier
        .fillMaxSize(),
    )
  }

  UsersList(
    isRefreshing = viewState.isRefreshing,
    userItems = viewState.userItems,
    refresh = refresh,
    removeItem = removeItem,
  )
}

@Suppress("DEPRECATION") // TODO: Remove when SwipeRefresh is migrated to Material3
@Composable
private fun UsersList(
  isRefreshing: Boolean,
  userItems: ImmutableList<UserItem>,
  refresh: () -> Unit,
  removeItem: (UserItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  SwipeRefresh(
    modifier = modifier.fillMaxSize(),
    state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
    onRefresh = refresh
  ) {
    LazyColumn {
      itemsIndexed(
        items = userItems,
        key = { _, item -> item.id },
      ) { index, userItem ->
        UserItemRow(
          item = userItem,
          onRemove = { removeItem(userItem) },
        )

        if (index < userItems.lastIndex) {
          Divider(
            modifier = Modifier.padding(horizontal = 8.dp),
          )
        }
      }
    }
  }
}

@ReadOnlyComposable
@Composable
private fun UserError.getReadableMessage(): String = when (this) {
  is UserError.InvalidId -> stringResource(R.string.invalid_id_error_message)
  UserError.NetworkError -> stringResource(R.string.network_error_error_message)
  UserError.ServerError -> stringResource(R.string.server_error_error_message)
  UserError.Unexpected -> stringResource(R.string.unexpected_error_error_message)
  is UserError.UserNotFound -> stringResource(R.string.user_not_found_error_message)
  is UserError.ValidationFailed -> stringResource(R.string.validation_failed_error_message)
}
