package com.hoc.flowmvi

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.hoc.flowmvi.core_ui.AppBarState
import com.hoc.flowmvi.core_ui.ProvideSnackbarHostState
import com.hoc.flowmvi.ui.main.navigation.usersListScreen
import com.hoc.flowmvi.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      AppTheme {
        JetpackComposeMVICoroutinesFlowApp()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JetpackComposeMVICoroutinesFlowAppBar(
  title: String?,
  navigationIcon: @Composable () -> Unit,
  actions: @Composable RowScope.() -> Unit,
  modifier: Modifier = Modifier
) {
  CenterAlignedTopAppBar(
    title = {
      if (title != null) {
        Text(text = title)
      }
    },
    modifier = modifier,
    navigationIcon = navigationIcon,
    actions = actions
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JetpackComposeMVICoroutinesFlowApp(
  startScreen: Screen = Screen.UsersList,
  modifier: Modifier = Modifier,
  appState: JetpackComposeMVICoroutinesFlowAppState = rememberJetpackComposeMVICoroutinesFlowApp(),
) {
  val navController = appState.navController
  val snackbarHostState = remember { SnackbarHostState() }
  val (appBarState, setAppBarState) = remember { mutableStateOf<AppBarState?>(null) }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      appBarState?.let {
        JetpackComposeMVICoroutinesFlowAppBar(
          title = it.title,
          actions = it.actions,
          navigationIcon = it.navigationIcon,
        )
      }
    }
  ) { innerPadding ->
    ProvideSnackbarHostState(snackbarHostState = snackbarHostState) {
      NavHost(
        navController = navController,
        modifier = modifier.padding(innerPadding),
        startDestination = startScreen.route
      ) {
        usersListScreen(
          configAppBar = setAppBarState,
        )
      }
    }
  }
}
