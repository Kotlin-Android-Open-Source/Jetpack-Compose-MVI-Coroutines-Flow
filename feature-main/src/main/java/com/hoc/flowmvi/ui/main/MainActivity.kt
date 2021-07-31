package com.hoc.flowmvi.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.hoc.flowmvi.core.IntentDispatcher
import com.hoc.flowmvi.core.navigator.Navigator
import com.hoc.flowmvi.core.navigator.ProvideNavigator
import com.hoc.flowmvi.core.unit
import com.hoc.flowmvi.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
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
private fun MainScreen() {
  val (state, singleEvent, processIntent) = viewModel<MainVM>()

  DisposableEffect("Initial") {
    // dispatch initial intent
    processIntent(ViewIntent.Initial)
    onDispose { }
  }

  val scaffoldState = rememberScaffoldState()
  val snackbarHostState = scaffoldState.snackbarHostState

  LaunchedEffect("SingleEvent") {
    // observe single event
    singleEvent
      .onEach { event ->
        Log.d("MainActivity", "handleSingleEvent $event")

        when (event) {
          SingleEvent.Refresh.Success -> snackbarHostState.showSnackbar("Refresh success")
          is SingleEvent.Refresh.Failure -> snackbarHostState.showSnackbar("Refresh failure")
          is SingleEvent.GetUsersError -> snackbarHostState.showSnackbar("Get user failure")
          is SingleEvent.RemoveUser.Success -> snackbarHostState.showSnackbar("Removed '${event.user.fullName}'")
          is SingleEvent.RemoveUser.Failure -> snackbarHostState.showSnackbar("Error when removing '${event.user.fullName}'")
        }.unit
      }
      .collect()
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
      processIntent = processIntent,
    )
  }
}

@Composable
private fun MainContent(
  state: ViewState,
  processIntent: IntentDispatcher<ViewIntent>,
  modifier: Modifier = Modifier
) {
  if (state.error != null) {
    return Column(
      modifier = modifier
        .fillMaxSize()
        .padding(8.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = state.error.message ?: "An expected error",
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
      )

      Spacer(modifier = Modifier.height(8.dp))

      Button(
        onClick = { processIntent(ViewIntent.Retry) },
        contentPadding = PaddingValues(
          vertical = 12.dp,
          horizontal = 24.dp,
        ),
        shape = RoundedCornerShape(6.dp),
      ) {
        Text(text = "RETRY")
      }
    }
  }

  if (state.isLoading) {
    return Column(
      modifier = modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      CircularProgressIndicator()
    }
  }

  UsersList(
    processIntent = processIntent,
    isRefreshing = state.isRefreshing,
    userItems = state.userItems,
  )
}

@Composable
private fun UsersList(
  isRefreshing: Boolean,
  userItems: List<UserItem>,
  processIntent: IntentDispatcher<ViewIntent>,
  modifier: Modifier = Modifier
) {
  Log.d("UserRow", userItems.filter { it.isDeleting }.size.toString())
  val lastIndex = userItems.lastIndex

  SwipeRefresh(
    state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
    onRefresh = { processIntent(ViewIntent.Refresh) },
  ) {
    LazyColumn(
      modifier = modifier.fillMaxSize(),
    ) {
      itemsIndexed(
        userItems,
        key = { _, item -> item.id },
      ) { index, item ->

        UserRow(
          item = item,
          modifier = Modifier
            .fillParentMaxWidth(),
          onDelete = { processIntent(ViewIntent.RemoveUser(it)) }
        )

        if (index < lastIndex) {
          Divider(
            modifier = Modifier.padding(horizontal = 8.dp),
            thickness = 0.7.dp,
          )
        }
      }
    }
  }
}
