package com.hoc.flowmvi.ui.main

import androidx.lifecycle.viewModelScope
import arrow.core.flatMap
import com.hoc.flowmvi.domain.usecase.GetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RefreshGetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RemoveUserUseCase
import com.hoc.flowmvi.mvi_base.AbstractMviViewModel
import com.hoc081098.flowext.defer
import com.hoc081098.flowext.flatMapFirst
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.startWith
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import timber.log.Timber
import javax.inject.Inject

private val ViewState.canRefresh get() = !isLoading && error === null

@HiltViewModel
@OptIn(FlowPreview::class)
class MainVM @Inject constructor(
  private val getUsersUseCase: GetUsersUseCase,
  private val refreshGetUsers: RefreshGetUsersUseCase,
  private val removeUser: RemoveUserUseCase,
) : AbstractMviViewModel<ViewIntent, ViewState, SingleEvent>() {

  override val viewState: StateFlow<ViewState>

  init {
    val initialVS = ViewState.initial()

    viewState = merge(
      intentFlow.filterIsInstance<ViewIntent.Initial>().take(1),
      intentFlow.filterNot { it is ViewIntent.Initial }
    )
      .shareWhileSubscribed()
      .toPartialStateChangeFlow()
      .log("PartialStateChange")
      .sendSingleEvent()
      .scan(initialVS) { vs, change -> change.reduce(vs) }
      .log("ViewState")
      .stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialVS
      )
  }

  private fun Flow<PartialStateChange>.sendSingleEvent(): Flow<PartialStateChange> {
    return onEach { change ->
      val event = when (change) {
        is PartialStateChange.Users.Error -> SingleEvent.GetUsersError(change.error)
        is PartialStateChange.Refresh.Success -> SingleEvent.Refresh.Success
        is PartialStateChange.Refresh.Failure -> SingleEvent.Refresh.Failure(change.error)
        is PartialStateChange.RemoveUser.Success -> SingleEvent.RemoveUser.Success(change.user)
        is PartialStateChange.RemoveUser.Failure -> SingleEvent.RemoveUser.Failure(
          user = change.user,
          error = change.error,
        )
        PartialStateChange.Users.Loading -> return@onEach
        is PartialStateChange.Users.Data -> return@onEach
        PartialStateChange.Refresh.Loading -> return@onEach
        is PartialStateChange.RemoveUser.Loading -> return@onEach
      }
      sendEvent(event)
    }
  }

  private fun SharedFlow<ViewIntent>.toPartialStateChangeFlow(): Flow<PartialStateChange> {
    val userChanges = defer(getUsersUseCase::invoke)
      .onEach { either -> Timber.tag(logTag).d("Emit users.size=${either.map { it.size }}") }
      .map { result ->
        result.fold(
          ifLeft = { PartialStateChange.Users.Error(it) },
          ifRight = { PartialStateChange.Users.Data(it.map(::UserItem)) }
        )
      }
      .startWith(PartialStateChange.Users.Loading)

    return merge(
      // users change
      merge(
        filterIsInstance<ViewIntent.Initial>(),
        filterIsInstance<ViewIntent.Retry>()
          .filter { viewState.value.error != null }
      ).flatMapLatest { userChanges },
      // refresh change
      filterIsInstance<ViewIntent.Refresh>()
        .toRefreshChangeFlow(),
      // remove user change
      filterIsInstance<ViewIntent.RemoveUser>()
        .toRemoveUserChangeFlow()
    )
  }

  //region Processors
  private fun Flow<ViewIntent.Refresh>.toRefreshChangeFlow(): Flow<PartialStateChange.Refresh> {
    val refreshChanges = refreshGetUsers::invoke
      .asFlow()
      .map { result ->
        result.fold(
          ifLeft = { PartialStateChange.Refresh.Failure(it) },
          ifRight = { PartialStateChange.Refresh.Success }
        )
      }
      .startWith(PartialStateChange.Refresh.Loading)

    return filter { viewState.value.canRefresh }
      .flatMapFirst { refreshChanges }
  }

  private fun Flow<ViewIntent.RemoveUser>.toRemoveUserChangeFlow(): Flow<PartialStateChange.RemoveUser> =
    map { it.user }
      .flatMapMerge { userItem ->
        flowFromSuspend {
          userItem
            .toDomain()
            .flatMap { removeUser(it) }
        }
          .map { result ->
            result.fold(
              ifLeft = { PartialStateChange.RemoveUser.Failure(userItem, it) },
              ifRight = { PartialStateChange.RemoveUser.Success(userItem) },
            )
          }
          .startWith(PartialStateChange.RemoveUser.Loading(userItem))
      }
  //endregion
}
