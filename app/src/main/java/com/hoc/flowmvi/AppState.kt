package com.hoc.flowmvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hoc.flowmvi.Screen.UsersList
import com.hoc.flowmvi.ui.main.navigation.usersListNavigationRoute

@Composable
fun rememberJetpackComposeMVICoroutinesFlowApp(
  navController: NavHostController = rememberNavController()
): JetpackComposeMVICoroutinesFlowAppState = remember(navController) {
  JetpackComposeMVICoroutinesFlowAppState(navController)
}

enum class Screen {
  UsersList,
  SearchUsers,
  AddNewUser;

  val route: String
    get() = when (this) {
      UsersList -> usersListNavigationRoute
      SearchUsers -> TODO()
      AddNewUser -> TODO()
    }

  companion object {
    /**
     * Use this instead of [values()] for more performant.
     * See [KT-48872](https://youtrack.jetbrains.com/issue/KT-48872)
     */
    val VALUES = values().asList()
  }
}

@Stable
class JetpackComposeMVICoroutinesFlowAppState(
  val navController: NavHostController,
) {
  private val currentDestination: NavDestination?
    @Composable get() = navController
      .currentBackStackEntryAsState().value?.destination

  val currentScreen: Screen?
    @Composable get() = when (currentDestination?.route) {
      usersListNavigationRoute -> UsersList
      else -> TODO()
    }

  fun onNavigateUp() {
    navController.navigateUp()
  }
}
