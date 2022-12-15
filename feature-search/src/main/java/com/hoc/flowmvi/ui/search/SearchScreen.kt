package com.hoc.flowmvi.ui.search

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.valueOr
import com.hoc.flowmvi.core_ui.AppBarState
import com.hoc.flowmvi.core_ui.AppBarTextField
import com.hoc.flowmvi.core_ui.ConfigAppBar
import com.hoc.flowmvi.core_ui.LoadingIndicator
import com.hoc.flowmvi.core_ui.LocalSnackbarHostState
import com.hoc.flowmvi.core_ui.OnLifecycleEvent
import com.hoc.flowmvi.core_ui.RetryButton
import com.hoc.flowmvi.core_ui.collectInLaunchedEffectWithLifecycle
import com.hoc.flowmvi.core_ui.debugCheckImmediateMainDispatcher
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.ui.theme.AppTheme
import com.hoc081098.flowext.select
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SearchUserRoute(
  configAppBar: ConfigAppBar,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: SearchVM = hiltViewModel(),
) {
  val intentChannel = remember { Channel<ViewIntent>(Channel.UNLIMITED) }
  LaunchedEffect(Unit) {
    withContext(Dispatchers.Main.immediate) {
      intentChannel
        .consumeAsFlow()
        .onEach(viewModel::processIntent)
        .collect()
    }
  }

  val scope = rememberCoroutineScope()
  val snackbarHostState by rememberUpdatedState(LocalSnackbarHostState.current)
  val context = LocalContext.current

  viewModel.singleEvent.collectInLaunchedEffectWithLifecycle { event ->
    debugCheckImmediateMainDispatcher()
    when (event) {
      is SingleEvent.SearchFailure -> {
        scope.launch {
          snackbarHostState.showSnackbar(
            message = "Failed to search: ${event.error.getReadableMessage(context)}",
          )
        }
      }
    }
  }

  val originalQueryState = viewModel.viewState
    .select { it.originalQuery }
    .collectAsStateWithLifecycle(initialValue = viewModel.viewState.value.originalQuery)

  val dispatch = remember {
    { intent: ViewIntent ->
      intentChannel.trySend(intent).getOrThrow()
    }
  }

  ConfigAppBar(
    onBackClickState = rememberUpdatedState(onBackClick),
    configAppBar = configAppBar,
    originalQueryState = originalQueryState,
    onValueChange = { dispatch(ViewIntent.Search(it)) }
  )

  val viewState by viewModel.viewState.collectAsStateWithLifecycle()
  SearchContent(
    modifier = modifier,
    viewState = viewState,
    onRetry = { dispatch(ViewIntent.Retry) },
  )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ConfigAppBar(
  onBackClickState: State<() -> Unit>,
  configAppBar: ConfigAppBar,
  originalQueryState: State<String>,
  onValueChange: (String) -> Unit,
) {
  val title = stringResource(id = R.string.search_user)
  val colors = TopAppBarDefaults.centerAlignedTopAppBarColors()

  val focusManager = LocalFocusManager.current

  val appBarState = remember(title, colors) {
    AppBarState(
      title = {
        val value = originalQueryState.value
        SideEffect { Timber.d("recomposition with $value") }

        AppBarTextField(
          modifier = Modifier.fillMaxWidth(),
          value = value,
          onValueChange = onValueChange,
          hint = "Search user...",
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
          keyboardActions = KeyboardActions(
            onSearch = { focusManager.clearFocus() }
          ),
        )
      },
      actions = {
        IconButton(onClick = { focusManager.clearFocus() }) {
          Icon(
            Icons.Default.Search,
            contentDescription = "Search",
          )
        }
      },
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

@Composable
private fun SearchContent(
  viewState: ViewState,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  UsersGrid(
    modifier = modifier,
    userItems = viewState.users
  )

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
}

@Composable
private fun UsersGrid(
  userItems: ImmutableList<UserItem>,
  modifier: Modifier = Modifier,
) {
  LazyVerticalGrid(
    modifier = modifier.fillMaxSize(),
    columns = GridCells.Fixed(
      count = 3,
    ),
    contentPadding = PaddingValues(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    items(userItems, key = { it.id }) { userItem ->
      UserItemCell(
        userItem = userItem
      )
    }
  }
}

@Preview(
  showBackground = true,
  showSystemUi = true,
)
@Composable
fun PreviewUsersGrid() {
  AppTheme {
    UsersGrid(
      userItems = (1..10).map { id ->
        UserItem.from(
          User.create(
            id = id.toString(),
            email = "hoc081098_$id@gmail.com",
            firstName = "Petrus",
            lastName = "Hoc $id",
            avatar = "avatar/$id",
          ).valueOr { throw IllegalArgumentException() }
        )
      }.toImmutableList()
    )
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

private fun UserError.getReadableMessage(context: Context): String = when (this) {
  is UserError.InvalidId -> context.getString(R.string.invalid_id_error_message)
  UserError.NetworkError -> context.getString(R.string.network_error_error_message)
  UserError.ServerError -> context.getString(R.string.server_error_error_message)
  UserError.Unexpected -> context.getString(R.string.unexpected_error_error_message)
  is UserError.UserNotFound -> context.getString(R.string.user_not_found_error_message)
  is UserError.ValidationFailed -> context.getString(R.string.validation_failed_error_message)
}
