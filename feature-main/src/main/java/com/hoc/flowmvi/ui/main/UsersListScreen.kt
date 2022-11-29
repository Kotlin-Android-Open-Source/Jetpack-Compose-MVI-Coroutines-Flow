package com.hoc.flowmvi.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.AlertDialog
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.getOrElse
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.hoc.flowmvi.core_ui.AppBarState
import com.hoc.flowmvi.core_ui.ConfigAppBar
import com.hoc.flowmvi.core_ui.LoadingIndicator
import com.hoc.flowmvi.core_ui.LocalSnackbarHostState
import com.hoc.flowmvi.core_ui.OnLifecycleEvent
import com.hoc.flowmvi.core_ui.RetryButton
import com.hoc.flowmvi.core_ui.collectInLaunchedEffectWithLifecycle
import com.hoc.flowmvi.domain.model.Email
import com.hoc.flowmvi.domain.model.FirstName
import com.hoc.flowmvi.domain.model.LastName
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.ui.theme.AppTheme
import com.hoc081098.flowext.startWith
import kotlinx.collections.immutable.ImmutableList
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
  viewModel.singleEvent.collectInLaunchedEffectWithLifecycle(snackbarHostState) { event ->
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
  val dispatch = remember {
    { intent: ViewIntent ->
      intentChannel.trySend(intent).getOrThrow()
    }
  }
  var shouldBeDeletedItem by remember { mutableStateOf<UserItem?>(null) }
  if (shouldBeDeletedItem != null) {
    DeleteUserConfirmationDialog(
      shouldBeDeletedItem = shouldBeDeletedItem!!,
      onConfirm = {
        dispatch(ViewIntent.RemoveUser(it))
        shouldBeDeletedItem = null
      },
      onDismiss = {
        shouldBeDeletedItem = null
      },
    )
  }

  UsersListContent(
    modifier = modifier,
    viewState = viewState,
    onRetry = { dispatch(ViewIntent.Retry) },
    refresh = { dispatch(ViewIntent.Refresh) },
    removeItem = { shouldBeDeletedItem = it },
  )
}

@Composable
private fun DeleteUserConfirmationDialog(
  shouldBeDeletedItem: UserItem,
  onDismiss: () -> Unit,
  onConfirm: (UserItem) -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
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
      errorMessage = when (viewState.error) {
        is UserError.InvalidId -> "Invalid id"
        UserError.NetworkError -> "Network error"
        UserError.ServerError -> "Server error"
        UserError.Unexpected -> "Unexpected error"
        is UserError.UserNotFound -> "User not found"
        is UserError.ValidationFailed -> "Validation failed"
        null -> ""
      }
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

private val ImageSize = 72.dp
private val Padding = 8.dp
private val ItemHeight = ImageSize + Padding * 2
private val DismissBackgroundColor = Color.Red.copy(alpha = 0.75f)

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun UserItemRow(
  item: UserItem,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val dismissState = rememberDismissState(
    confirmStateChange = { dismissValue ->
      if (dismissValue == DismissValue.DismissedToStart) {
        onRemove()
      }
      false
    }
  )

  SwipeToDismiss(
    state = dismissState,
    background = {
      val scale by animateFloatAsState(
        if (dismissState.targetValue == DismissValue.Default) {
          0.75f
        } else {
          1f
        }
      )

      Box(
        Modifier
          .fillMaxSize()
          .background(DismissBackgroundColor)
          .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterEnd
      ) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = stringResource(R.string.delete),
          modifier = Modifier.scale(scale),
          tint = Color.White
        )
      }
    },
    directions = setOf(DismissDirection.EndToStart),
    dismissThresholds = { FractionalThreshold(0.25f) },
  ) {
    Row(
      modifier = modifier
        .background(MaterialTheme.colorScheme.background)
        .padding(all = Padding),
    ) {
      UserItemAvatar(item)

      Spacer(modifier = Modifier.width(Padding))

      Column(
        modifier = Modifier
          .weight(1f)
          .align(Alignment.CenterVertically),
        verticalArrangement = Arrangement.Center,
      ) {
        Text(
          modifier = Modifier.fillMaxWidth(),
          text = item.fullName,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(Padding))

        Text(
          modifier = Modifier.fillMaxWidth(),
          text = item.email,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 12.sp,
            color = MaterialTheme.typography.bodySmall.color,
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      if (item.isDeleting) {
        CircularProgressIndicator(
          modifier = Modifier.align(Alignment.CenterVertically),
          strokeWidth = 2.dp,
        )
      }
    }
  }
}

@Composable
private fun UserItemAvatar(item: UserItem) {
  SubcomposeAsyncImage(
    modifier = Modifier
      .requiredWidth(ImageSize)
      .requiredHeight(ImageSize),
    model = ImageRequest.Builder(LocalContext.current)
      .data(item.avatar)
      .crossfade(true)
      .transformations(CircleCropTransformation())
      .placeholder(R.drawable.ic_baseline_person_24)
      .build(),
    contentDescription = "Avatar of ${item.fullName}",
    error = {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Icon(
          imageVector = Icons.Filled.ErrorOutline,
          contentDescription = "Error",
          modifier = Modifier.size(width = 20.dp, height = 20.dp)
        )
      }
    }
  )
}

@Preview
@Composable
fun PreviewUserItemRow() {
  AppTheme {
    UserItemRow(
      item = UserItem(
        domain = User(
          id = "",
          email = Email.create("hoc081098@gmail.com")
            .getOrElse { error("") },
          firstName = FirstName.create("Petrus")
            .getOrElse { error("") },
          lastName = LastName.create("Hoc")
            .getOrElse { error("") },
          avatar = ""
        )
      ).copy(isDeleting = true),
      onRemove = {},
    )
  }
}
