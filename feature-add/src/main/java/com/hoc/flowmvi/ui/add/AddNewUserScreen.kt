package com.hoc.flowmvi.ui.add

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
internal fun AddNewUserRoute(
  configAppBar: ConfigAppBar,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: AddVM = hiltViewModel(),
) {
  val title = stringResource(id = R.string.add_new_user)
  val colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
  val appBarState = remember(colors) {
    AppBarState(
      title = title,
      actions = {},
      navigationIcon = {
        IconButton(onClick = onBackClick) {
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

  val intentChannel = remember { Channel<ViewIntent>(Channel.UNLIMITED) }
  LaunchedEffect(Unit) {
    intentChannel
      .consumeAsFlow()
      .onEach(viewModel::processIntent)
      .collect()
  }

  val snackbarHostState by rememberUpdatedState(LocalSnackbarHostState.current)
  val scope = rememberCoroutineScope()
  viewModel.singleEvent.collectInLaunchedEffectWithLifecycle { event ->
    when (event) {
      is SingleEvent.AddUserFailure -> TODO()
      is SingleEvent.AddUserSuccess -> TODO()
    }
  }

  val viewState by viewModel.viewState.collectAsStateWithLifecycle()
  val dispatch = remember {
    { intent: ViewIntent ->
      intentChannel.trySend(intent).getOrThrow()
    }
  }

  AddNewUserContent(
    modifier = modifier,
    viewState = viewState,
    onEmailChanged = { dispatch(ViewIntent.EmailChanged(it)) },
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddNewUserContent(
  viewState: ViewState,
  onEmailChanged: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    TextField(
      value = viewState.email ?: "",
      onValueChange = onEmailChanged
    )
  }
}
