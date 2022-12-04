package com.hoc.flowmvi.ui.add

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoc.flowmvi.core_ui.AppBarState
import com.hoc.flowmvi.core_ui.ConfigAppBar
import com.hoc.flowmvi.core_ui.LoadingIndicator
import com.hoc.flowmvi.core_ui.LocalSnackbarHostState
import com.hoc.flowmvi.core_ui.OnLifecycleEvent
import com.hoc.flowmvi.core_ui.collectInLaunchedEffectWithLifecycle
import com.hoc.flowmvi.core_ui.debugCheckImmediateMainDispatcher
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.model.UserValidationError
import com.hoc.flowmvi.ui.theme.AppTheme
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
internal fun AddNewUserRoute(
  configAppBar: ConfigAppBar,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: AddVM = hiltViewModel(),
) {
  val currentOnBackClickState = rememberUpdatedState(onBackClick)
  ConfigAppBar(currentOnBackClickState, configAppBar)

  val intentChannel = remember { Channel<ViewIntent>(Channel.UNLIMITED) }
  LaunchedEffect(Unit) {
    withContext(Dispatchers.Main.immediate) {
      intentChannel
        .consumeAsFlow()
        .onEach(viewModel::processIntent)
        .collect()
    }
  }

  val snackbarHostState by rememberUpdatedState(LocalSnackbarHostState.current)
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  viewModel.singleEvent.collectInLaunchedEffectWithLifecycle { event ->
    debugCheckImmediateMainDispatcher()

    when (event) {
      is SingleEvent.AddUserFailure -> {
        scope.launch {
          snackbarHostState.showSnackbar(
            event.error.getReadableMessage(context)
          )
        }
      }
      is SingleEvent.AddUserSuccess -> {
        scope.launch {
          snackbarHostState.showSnackbar(
            context.getString(R.string.add_user_success)
          )
        }
        scope.launch {
          delay(200)
          currentOnBackClickState.value()
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

  AddNewUserContent(
    modifier = modifier,
    viewState = viewState,
    onEmailChanged = { dispatch(ViewIntent.EmailChanged(it)) },
    onFirstNameChanged = { dispatch(ViewIntent.FirstNameChanged(it)) },
    onLastNameChanged = { dispatch(ViewIntent.LastNameChanged(it)) },
    onSubmit = { dispatch(ViewIntent.Submit) }
  )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ConfigAppBar(
  currentOnBackClickState: State<() -> Unit>,
  configAppBar: ConfigAppBar
) {
  val title = stringResource(id = R.string.add_new_user)
  val colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
  val appBarState = remember(colors) {
    AppBarState(
      title = title,
      actions = {},
      navigationIcon = {
        IconButton(onClick = { currentOnBackClickState.value() }) {
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
private fun AddNewUserContent(
  viewState: ViewState,
  onEmailChanged: (String) -> Unit,
  onFirstNameChanged: (String) -> Unit,
  onLastNameChanged: (String) -> Unit,
  onSubmit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val resources = LocalContext.current.resources
  val errors = viewState.errors

  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .wrapContentHeight(Alignment.CenterVertically)
        .verticalScroll(rememberScrollState())
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      EmailTextField(
        email = viewState.email ?: "",
        onEmailChanged = onEmailChanged,
        emailError = remember(viewState.emailChanged, errors, resources) {
          if (viewState.emailChanged && UserValidationError.INVALID_EMAIL_ADDRESS in errors) {
            resources.getString(R.string.invalid_email)
          } else {
            null
          }
        }
      )

      Spacer(modifier = Modifier.height(16.dp))

      FirstNameTextField(
        firstName = viewState.firstName ?: "",
        onFirstNameChanged = onFirstNameChanged,
        firstNameError = remember(viewState.firstNameChanged, errors, resources) {
          if (viewState.firstNameChanged && UserValidationError.TOO_SHORT_FIRST_NAME in errors) {
            resources.getString(R.string.too_short_first_name)
          } else {
            null
          }
        }
      )

      Spacer(modifier = Modifier.height(16.dp))

      LastNameTextField(
        lastName = viewState.lastName ?: "",
        onLastNameChanged = onLastNameChanged,
        lastNameError = remember(viewState.lastNameChanged, errors, resources) {
          if (viewState.lastNameChanged && UserValidationError.TOO_SHORT_LAST_NAME in errors) {
            resources.getString(R.string.too_short_last_name)
          } else {
            null
          }
        }
      )

      Spacer(modifier = Modifier.height(24.dp))

      AddButton(
        isLoading = viewState.isLoading,
        onSubmit = onSubmit,
      )

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun AddButton(
  isLoading: Boolean,
  onSubmit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Crossfade(
    modifier = modifier
      .fillMaxWidth()
      .heightIn(min = 64.dp),
    targetState = isLoading,
    animationSpec = tween(durationMillis = 200),
    label = "LoadingIndicator/ElevatedButton",
  ) { state ->
    if (state) {
      LoadingIndicator(
        modifier = Modifier
          .fillMaxWidth(),
      )
    } else {
      ElevatedButton(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentSize(Alignment.Center),
        colors = ButtonDefaults.elevatedButtonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        onClick = onSubmit,
        contentPadding = PaddingValues(
          horizontal = 32.dp,
          vertical = 16.dp,
        ),
      ) {
        Text(text = "Add")
      }
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LastNameTextField(
  lastName: String,
  onLastNameChanged: (String) -> Unit,
  lastNameError: String?,
  modifier: Modifier = Modifier,
) {
  TextField(
    modifier = modifier.fillMaxWidth(),
    value = lastName,
    onValueChange = onLastNameChanged,
    label = { Text(text = "Last name") },
    leadingIcon = {
      Icon(
        imageVector = Icons.Filled.Person,
        contentDescription = "Last name"
      )
    },
    maxLines = 1,
    singleLine = true,
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Text,
      imeAction = ImeAction.Done
    ),
    isError = lastNameError !== null,
    supportingText = {
      lastNameError?.let {
        Text(text = it)
      }
    }
  )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FirstNameTextField(
  firstName: String,
  onFirstNameChanged: (String) -> Unit,
  firstNameError: String?,
  modifier: Modifier = Modifier,
) {
  TextField(
    modifier = modifier.fillMaxWidth(),
    value = firstName,
    onValueChange = onFirstNameChanged,
    label = { Text(text = "First name") },
    leadingIcon = {
      Icon(
        imageVector = Icons.Filled.Person,
        contentDescription = "First name"
      )
    },
    maxLines = 1,
    singleLine = true,
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Text,
      imeAction = ImeAction.Next
    ),
    isError = firstNameError !== null,
    supportingText = {
      firstNameError?.let {
        Text(text = it)
      }
    }
  )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun EmailTextField(
  email: String,
  onEmailChanged: (String) -> Unit,
  emailError: String?,
  modifier: Modifier = Modifier,
) {
  TextField(
    modifier = modifier.fillMaxWidth(),
    value = email,
    onValueChange = onEmailChanged,
    label = { Text(text = "Email") },
    leadingIcon = {
      Icon(
        imageVector = Icons.Filled.Email,
        contentDescription = "Email"
      )
    },
    maxLines = 1,
    singleLine = true,
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Email,
      imeAction = ImeAction.Next
    ),
    isError = emailError !== null,
    supportingText = {
      emailError?.let {
        Text(text = it)
      }
    }
  )
}

@Preview(
  showBackground = true,
  showSystemUi = true,
  device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480",
)
@Composable
fun PreviewAddNewUserContent() {
  AppTheme {
    AddNewUserContent(
      viewState = ViewState(
        errors = persistentHashSetOf(),
        isLoading = true,
        emailChanged = false,
        firstNameChanged = false,
        lastNameChanged = false,
        email = "hoc081098@gmail.com",
        firstName = "Petrus",
        lastName = "Hoc",
      ),
      onEmailChanged = {},
      onFirstNameChanged = {},
      onLastNameChanged = {},
      onSubmit = {}
    )
  }
}

private fun UserError.getReadableMessage(context: Context): String = when (this) {
  is UserError.InvalidId -> context.getString(R.string.invalid_id_error_message)
  UserError.NetworkError -> context.getString(R.string.network_error_error_message)
  UserError.ServerError -> context.getString(R.string.server_error_error_message)
  UserError.Unexpected -> context.getString(R.string.unexpected_error_error_message)
  is UserError.UserNotFound -> context.getString(R.string.user_not_found_error_message)
  is UserError.ValidationFailed -> context.getString(R.string.validation_failed_error_message)
}
