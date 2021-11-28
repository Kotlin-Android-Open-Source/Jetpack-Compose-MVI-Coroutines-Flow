package com.hoc.flowmvi.core_ui.navigator

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf

interface IntentProviders {
  interface Add {
    fun makeIntent(context: Context): Intent
  }
}

@Stable
interface Navigator {
  fun Context.navigateToAdd()

  companion object {
    val current: Navigator
      @Composable
      get() = LocalNavigator.current
  }
}

@Composable
fun ProvideNavigator(
  navigator: Navigator,
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(
    LocalNavigator provides navigator,
    content = content
  )
}

private val LocalNavigator = staticCompositionLocalOf<Navigator> { error("No Navigator provided") }
