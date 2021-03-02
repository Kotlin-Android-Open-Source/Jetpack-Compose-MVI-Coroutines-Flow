package com.hoc.flowmvi.core

import android.content.Context
import com.hoc.flowmvi.core.navigator.IntentProviders
import com.hoc.flowmvi.core.navigator.Navigator
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
internal class NavigatorImpl @Inject constructor(
  private val add: IntentProviders.Add
) : Navigator {
  override fun Context.navigateToAdd() =
    startActivity(add.makeIntent(this))
}
