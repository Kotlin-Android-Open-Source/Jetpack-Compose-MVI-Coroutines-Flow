package com.hoc.flowmvi.core

import android.content.Context
import com.hoc.flowmvi.core_ui.navigator.IntentProviders
import com.hoc.flowmvi.core_ui.navigator.Navigator
import javax.inject.Inject

internal class NavigatorImpl @Inject constructor(
  private val add: IntentProviders.Add
) : Navigator {
  override fun Context.navigateToAdd() =
    startActivity(add.makeIntent(this))
}
