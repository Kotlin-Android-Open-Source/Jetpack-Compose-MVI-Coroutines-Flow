package com.hoc.flowmvi.core

import com.hoc.flowmvi.core.dispatchers.AppCoroutineDispatchers
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CoreModule {
  @Binds
  @Singleton
  abstract fun coroutineDispatchers(impl: DefaultCoroutineDispatchers): AppCoroutineDispatchers
}
