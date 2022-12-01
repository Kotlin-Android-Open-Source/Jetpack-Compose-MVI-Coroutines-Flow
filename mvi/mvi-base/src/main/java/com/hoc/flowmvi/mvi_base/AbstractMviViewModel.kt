package com.hoc.flowmvi.mvi_base

import android.os.Build
import android.os.Looper
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc.flowmvi.core_ui.debugCheckImmediateMainDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.PUBLICATION

private fun debugCheckMainThread() {
  if (BuildConfig.DEBUG) {
    check(Looper.getMainLooper() === Looper.myLooper()) {
      "Expected to be called on the main thread but was " + Thread.currentThread().name
    }
  }
}

abstract class AbstractMviViewModel<I : MviIntent, S : MviViewState, E : MviSingleEvent> :
  MviViewModel<I, S, E>, ViewModel() {
  protected val logTag by lazy(PUBLICATION) {
    this::class.java.simpleName.let { tag: String ->
      // Tag length limit was removed in API 26.
      if (tag.length <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= 26) {
        tag
      } else {
        tag.take(MAX_TAG_LENGTH)
      }
    }
  }

  private val eventChannel = Channel<E>(Channel.UNLIMITED)
  private val intentMutableFlow = MutableSharedFlow<I>(extraBufferCapacity = Int.MAX_VALUE)

  final override val singleEvent: Flow<E> get() = eventChannel.receiveAsFlow()

  @MainThread
  final override fun processIntent(intent: I) {
    debugCheckMainThread()
    check(intentMutableFlow.tryEmit(intent)) { "Failed to emit intent: $intent" }
  }

  @CallSuper
  override fun onCleared() {
    super.onCleared()
    eventChannel.close()
  }

  // Send event and access intent flow.

  protected suspend fun sendEvent(event: E) {
    debugCheckMainThread()
    debugCheckImmediateMainDispatcher()
    eventChannel.trySendBlocking(event).getOrThrow()
  }

  protected val intentFlow: SharedFlow<I> get() = intentMutableFlow

  // Extensions on Flow using viewModelScope.

  protected fun <T> Flow<T>.log(subject: String): Flow<T> =
    onEach { Timber.tag(logTag).d(">>> $subject: $it") }

  protected fun <T> Flow<T>.shareWhileSubscribed(): SharedFlow<T> =
    shareIn(viewModelScope, SharingStarted.WhileSubscribed())

  protected fun <T> Flow<T>.stateWithInitialNullWhileSubscribed(): StateFlow<T?> =
    stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

  private companion object {
    private const val MAX_TAG_LENGTH = 23
  }
}
