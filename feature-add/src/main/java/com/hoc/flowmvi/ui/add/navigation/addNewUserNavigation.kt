package com.hoc.flowmvi.ui.add.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.hoc.flowmvi.core_ui.ConfigAppBar
import com.hoc.flowmvi.ui.add.AddNewUserRoute

const val AddNewUserNavigationRoute = "add_new_user_route"

/**
 * Navigate to add new user screen
 */
fun NavController.navigateToAddNewUser(navOptions: NavOptions? = null) =
  navigate(AddNewUserNavigationRoute, navOptions)

/**
 * Add the add new user screen to the navigation graph.
 */
fun NavGraphBuilder.addNewUserScreen(
  configAppBar: ConfigAppBar,
  onBackClick: () -> Unit,
) {
  composable(route = AddNewUserNavigationRoute) {
    AddNewUserRoute(
      configAppBar = configAppBar,
      onBackClick = onBackClick,
    )
  }
}
