package com.hoc.flowmvi.ui.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.hoc.flowmvi.core_ui.ConfigAppBar
import com.hoc.flowmvi.ui.search.SearchUserRoute

const val SearchUserNavigationRoute = "search_user_route"

/**
 * Navigate to search user screen
 */
fun NavController.navigateToSearchUser(navOptions: NavOptions? = null) =
  navigate(SearchUserNavigationRoute, navOptions)

/**
 * Add the search user screen to the navigation graph.
 */
fun NavGraphBuilder.searchUserScreen(
  configAppBar: ConfigAppBar,
  onBackClick: () -> Unit,
) {
  composable(route = SearchUserNavigationRoute) {
    SearchUserRoute(
      configAppBar = configAppBar,
      onBackClick = onBackClick,
    )
  }
}
