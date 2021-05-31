package com.hoc.flowmvi.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.transform.CircleCropTransformation
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.ImageLoadState
import com.hoc.flowmvi.core.IntentDispatcher
import com.hoc.flowmvi.core.navigator.Navigator
import com.hoc.flowmvi.core.navigator.ProvideNavigator
import com.hoc.flowmvi.core.unit
import com.hoc.flowmvi.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

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
internal fun MainScreen() {
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
internal fun MainContent(
  state: ViewState,
  processIntent: IntentDispatcher<ViewIntent>,
  modifier: Modifier = Modifier
) {
  if (state.error != null) {
    Column(
      modifier = modifier.fillMaxSize()
    ) {
      Button(onClick = { processIntent(ViewIntent.Retry) }) {
        Text(text = "RETRY")
      }
      Text(text = state.error.message ?: "An expected error")
    }
    return
  }

  if (state.isLoading) {
    Column(
      modifier = modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      CircularProgressIndicator()
    }
    return
  }

  val imageSize = 72.dp
  val padding = 8.dp
  val itemHeight = imageSize + padding * 2

  LazyColumn(
    modifier = modifier.fillMaxSize()
  ) {
    items(
      state.userItems,
      key = { it.id },
    ) { item ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(itemHeight)
          .padding(all = padding),
      ) {
        val painter = rememberCoilPainter(
          request = item.avatar,
          requestBuilder = { transformations(CircleCropTransformation()) },
          fadeIn = true,
        )

        Box {
          Image(
            painter = painter,
            contentDescription = "Avatar for ${item.fullName}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .requiredWidth(imageSize)
              .requiredHeight(imageSize),
          )

          when(painter.loadState) {
            ImageLoadState.Empty -> Unit
            is ImageLoadState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            is ImageLoadState.Success -> Unit
            is ImageLoadState.Error -> Unit
          }
        }

        Spacer(modifier = Modifier.width(padding))

        Text(item.fullName)
      }

      Divider(
        modifier = Modifier.padding(horizontal = padding),
        thickness = 0.7.dp,
      )
    }
  }

  // TODO: Refresh
  // SwipeToRefreshLayout(
  //   refreshingState = state.isRefreshing,
  //   onRefresh = { processIntent(ViewIntent.Refresh) },
  //   refreshIndicator = {
  //     Surface(elevation = 10.dp, shape = CircleShape) {
  //       CircularProgressIndicator(
  //         modifier = Modifier
  //           .size(36.dp)
  //           .padding(4.dp),
  //         strokeWidth = 2.dp
  //       )
  //     }
  //   }) {
  //
  // }
}
