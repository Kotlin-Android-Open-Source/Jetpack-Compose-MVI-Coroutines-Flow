package com.hoc.flowmvi.ui.main.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.hoc.flowmvi.core_ui.ConfigAppBar
import com.hoc.flowmvi.ui.main.UsersListRoute

const val UsersListNavigationRoute = "users_list_route"

/**
 * Navigate to users list screen
 */
fun NavController.navigateUsersList(navOptions: NavOptions? = null) =
  navigate(UsersListNavigationRoute, navOptions)

/**
 * Add the users list screen to the navigation graph.
 */
fun NavGraphBuilder.usersListScreen(
  configAppBar: ConfigAppBar,
  navigateToAddUser: () -> Unit,
) {
  composable(route = UsersListNavigationRoute) {
    UsersListRoute(
      configAppBar = configAppBar,
      navigateToAddUser = navigateToAddUser,
    )
  }
}
