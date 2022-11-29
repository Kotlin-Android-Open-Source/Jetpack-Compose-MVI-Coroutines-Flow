// package com.hoc.flowmvi.ui.main
//
// import android.os.Bundle
// import android.widget.Toast
// import androidx.activity.compose.setContent
// import androidx.appcompat.app.AppCompatActivity
// import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.Add
// import androidx.compose.material3.ExperimentalMaterial3Api
// import androidx.compose.material3.Icon
// import androidx.compose.material3.IconButton
// import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.Scaffold
// import androidx.compose.material3.SnackbarHost
// import androidx.compose.material3.SnackbarHostState
// import androidx.compose.material3.Surface
// import androidx.compose.material3.Text
// import androidx.compose.material3.TopAppBar
// import androidx.compose.runtime.Composable
// import androidx.compose.runtime.LaunchedEffect
// import androidx.compose.runtime.getValue
// import androidx.compose.runtime.remember
// import androidx.compose.runtime.rememberCoroutineScope
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.res.stringResource
// import androidx.compose.ui.tooling.preview.Preview
// import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
// import androidx.lifecycle.compose.collectAsStateWithLifecycle
// import androidx.lifecycle.viewmodel.compose.viewModel
// import arrow.core.valueOr
// import com.hoc.flowmvi.core.unit
// import com.hoc.flowmvi.core_ui.LoadingIndicator
// import com.hoc.flowmvi.core_ui.RetryButton
// import com.hoc.flowmvi.core_ui.collectInLaunchedEffectWithLifecycle
// import com.hoc.flowmvi.core_ui.navigator.Navigator
// import com.hoc.flowmvi.core_ui.navigator.ProvideNavigator
// import com.hoc.flowmvi.core_ui.rememberFlowWithLifecycle
// import com.hoc.flowmvi.domain.model.User
// import com.hoc.flowmvi.domain.model.UserError
// import com.hoc.flowmvi.ui.theme.AppTheme
// import com.hoc081098.flowext.startWith
// import dagger.hilt.android.AndroidEntryPoint
// import javax.inject.Inject
// import kotlinx.coroutines.channels.Channel
// import kotlinx.coroutines.flow.collect
// import kotlinx.coroutines.flow.onEach
// import kotlinx.coroutines.flow.receiveAsFlow
// import kotlinx.coroutines.launch
//
// @AndroidEntryPoint
// class MainActivity : AppCompatActivity() {
//  @Inject
//  internal lateinit var navigator: Navigator
//
//  override fun onCreate(savedInstanceState: Bundle?) {
//    super.onCreate(savedInstanceState)
//
//    setContent {
//      AppTheme {
//        Surface(color = MaterialTheme.colorScheme.background) {
//          ProvideNavigator(navigator = navigator) {
//            MainScreen()
//          }
//        }
//      }
//    }
//  }
// }
//
// @OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterial3Api::class)
// @Composable
// private fun MainScreen(
//  vm: MainVM = viewModel(),
//  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
// ) {
//
//
//  val state by vm.viewState.collectAsStateWithLifecycle()
//
//  vm.singleEvent.collectInLaunchedEffectWithLifecycle { event ->
//
//  }
//
//  Scaffold(
//    snackbarHost = { SnackbarHost(snackbarHostState) },
//    topBar = {
//      TopAppBar(
//        title = {
//          Text(text = stringResource(id = R.string.app_name))
//        },
//        actions = {
//          val navigator = Navigator.current
//          val context = LocalContext.current
//
//          IconButton(onClick = { navigator.run { context.navigateToAdd() } }) {
//            Icon(
//              imageVector = Icons.Filled.Add,
//              contentDescription = stringResource(id = R.string.add_user)
//            )
//          }
//        },
//      )
//    },
//  ) { innerPadding ->
//    val scope = rememberCoroutineScope()
//    val context = LocalContext.current
//
//    MainContent(
//      state = state,
//      onRefresh = { intentChannel.trySend(ViewIntent.Refresh) },
//      onRemove = { item ->
//        scope.launch {
//          val result = scaffoldState.snackbarHostState.showSnackbar(
//            message = "Do you want to remove '${item.fullName}'",
//            actionLabel = "OK",
//          )
//          when (result) {
//            SnackbarResult.Dismissed -> {
//              Toast.makeText(context, "DISMISSED: $item", Toast.LENGTH_SHORT).show()
//            }
//            SnackbarResult.ActionPerformed -> {
//              intentChannel.trySend(ViewIntent.RemoveUser(item))
//            }
//          }
//        }
//      },
//      onRetry = { intentChannel.trySend(ViewIntent.Retry) },
//    )
//  }
// }
//
// @Composable
// private fun MainContent(
//  modifier: Modifier = Modifier,
//  state: ViewState,
//  onRefresh: () -> Unit,
//  onRemove: (UserItem) -> Unit,
//  onRetry: () -> Unit,
// ) {
//  if (state.error != null) {
//    return RetryButton(
//      errorMessage = when (state.error) {
//        is UserError.InvalidId -> "Invalid id"
//        UserError.NetworkError -> "Network error"
//        UserError.ServerError -> "Server error"
//        UserError.Unexpected -> "Unexpected error"
//        is UserError.UserNotFound -> "User not found"
//        is UserError.ValidationFailed -> "Validation failed"
//      },
//      onRetry = onRetry,
//      modifier = modifier,
//    )
//  }
//
//  if (state.isLoading) {
//    return LoadingIndicator(modifier)
//  }
//
//  UsersList(
//    isRefreshing = state.isRefreshing,
//    userItems = state.userItems,
//    onRefresh = onRefresh,
//    onRemove = onRemove,
//  )
// }
//
// @Preview
// @Composable
// fun PreviewMainContent() {
//  MainContent(
//    state = ViewState(
//      userItems = listOf(
//        User.create(
//          id = "1",
//          email = "hoc081098@gmail.com",
//          firstName = "first 1",
//          lastName = "last 1",
//          avatar = "",
//        ),
//        User.create(
//          id = "2",
//          email = "mviflow@gmail.com",
//          firstName = "first 2",
//          lastName = "last 2",
//          avatar = "",
//        )
//      ).map { UserItem(it.valueOr { error("") }) },
//      isLoading = false,
//      error = null,
//      isRefreshing = false
//    ),
//    onRefresh = {},
//    onRemove = {},
//    onRetry = {},
//  )
// }
