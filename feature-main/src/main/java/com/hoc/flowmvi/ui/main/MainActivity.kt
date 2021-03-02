package com.hoc.flowmvi.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoc.flowmvi.core.launchWhenStartedUntilStopped
import com.hoc.flowmvi.core.navigator.Navigator
import com.hoc.flowmvi.core.toast
import com.hoc.flowmvi.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
  @Inject
  internal lateinit var navigator: Navigator

//  private val userAdapter = UserAdapter()

  private val removeChannel = Channel<UserItem>(Channel.BUFFERED)


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      val mainVM = viewModel<MainVM>()

      LaunchedEffect("Initial") { mainVM.processIntent(ViewIntent.Initial) }

      LaunchedEffect("SingleEvent") {
        // observe single event
        mainVM.singleEvent
          .onEach { handleSingleEvent(it) }
          .collect()
      }

      AppTheme {
        Surface(color = MaterialTheme.colors.background) {
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
                    IconButton(onClick = { navigator.run { this@MainActivity.navigateToAdd() } }) {
                      Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.add_user)
                      )
                    }
                  }
                },
              )
            }
          ) {
            MainContent(mainVM.viewState.collectAsState().value)
          }
        }
      }
    }

//    setupViews()
//    bindVM(mainVM)
  }

  private fun setupViews() {
//    mainBinding.usersRecycler.run {
//      setHasFixedSize(true)
//      layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
//      adapter = userAdapter
//      addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
//
//      ItemTouchHelper(
//        SwipeLeftToDeleteCallback(context) cb@{ position ->
//          val userItem = mainVM.viewState.value.userItems[position]
//          removeChannel.safeOffer(userItem)
//        }
//      ).attachToRecyclerView(this)
//    }
  }

  private fun bindVM(mainVM: MainVM) {
    // observe view model
    mainVM.viewState
      .onEach { render(it) }
      .launchWhenStartedUntilStopped(this)

    // observe single event
    mainVM.singleEvent
      .onEach { handleSingleEvent(it) }
      .launchWhenStartedUntilStopped(this)

    // pass view intent to view model
    intents()
      .onEach { mainVM.processIntent(it) }
      .launchIn(lifecycleScope)
  }

  private fun intents() = merge(
    flowOf(ViewIntent.Initial),
//    mainBinding.swipeRefreshLayout.refreshes().map { ViewIntent.Refresh },
//    mainBinding.retryButton.clicks().map { ViewIntent.Retry },
    removeChannel.consumeAsFlow().map { ViewIntent.RemoveUser(it) }
  )

  private fun handleSingleEvent(event: SingleEvent) {
    Log.d("MainActivity", "handleSingleEvent $event")
    return when (event) {
      SingleEvent.Refresh.Success -> toast("Refresh success")
      is SingleEvent.Refresh.Failure -> toast("Refresh failure")
      is SingleEvent.GetUsersError -> toast("Get user failure")
      is SingleEvent.RemoveUser.Success -> toast("Removed '${event.user.fullName}'")
      is SingleEvent.RemoveUser.Failure -> toast("Error when removing '${event.user.fullName}'")
    }
  }

  private fun render(viewState: ViewState) {
    Log.d("MainActivity", "render $viewState")

//    userAdapter.submitList(viewState.userItems)

//    mainBinding.run {
//      errorGroup.isVisible = viewState.error !== null
//      errorMessageTextView.text = viewState.error?.message
//
//      progressBar.isVisible = viewState.isLoading
//
//      if (viewState.isRefreshing) {
//        if (!swipeRefreshLayout.isRefreshing) {
//          swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = true }
//        }
//      } else {
//        swipeRefreshLayout.isRefreshing = false
//      }
//
//      swipeRefreshLayout.isEnabled = !viewState.isLoading && viewState.error === null
//    }
  }
}

@Composable
internal fun MainContent(state: ViewState, modifier: Modifier = Modifier) {
  val scope = rememberCoroutineScope()
  val mainVM = viewModel<MainVM>()

  if (state.error != null) {
    Column(
      modifier = modifier.fillMaxSize()
    ) {
      Button(onClick = {
        scope.launch {
          mainVM.processIntent(ViewIntent.Retry)
        }
      }) {
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

  Text(text = state.userItems.toString())
}
