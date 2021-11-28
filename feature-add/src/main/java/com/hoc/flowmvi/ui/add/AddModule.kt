package com.hoc.flowmvi.ui.add

import com.hoc.flowmvi.core_ui.navigator.IntentProviders
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AddModule {
  @Binds
  abstract fun intentProviders_add(impl: AddActivity.IntentProvider): IntentProviders.Add
}
