package com.hoc.flowmvi.ui.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import arrow.core.ValidatedNel
import arrow.core.orNull
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserValidationError
import com.hoc.flowmvi.domain.usecase.AddUserUseCase
import com.hoc.flowmvi.mvi_base.AbstractMviViewModel
import com.hoc081098.flowext.flatMapFirst
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.mapTo
import com.hoc081098.flowext.startWith
import com.hoc081098.flowext.withLatestFrom
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.toPersistentHashSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import timber.log.Timber
import javax.inject.Inject

private typealias UserFormStateFlow = StateFlow<ValidatedNel<UserValidationError, User>?>

@HiltViewModel
@ExperimentalCoroutinesApi
class AddVM @Inject constructor(
  private val addUser: AddUserUseCase,
  savedStateHandle: SavedStateHandle,
) : AbstractMviViewModel<ViewIntent, ViewState, SingleEvent>() {

  override val viewState: StateFlow<ViewState>

  init {
    val initialVS = savedStateHandle
      .get<ViewState?>(VIEW_STATE)
      ?.copy(isLoading = false)
      ?: ViewState.initial()
    Timber.tag(logTag).d("[ADD_VM] initialVS: $initialVS")

    viewState = intentSharedFlow
      .toPartialStateChangeFlow(initialVS)
      .debugLog("PartialStateChange")
      .sendSingleEvent()
      .scan(initialVS) { state, change -> change.reduce(state) }
      .onEach { savedStateHandle[VIEW_STATE] = it }
      .debugLog("ViewState")
      .stateIn(viewModelScope, SharingStarted.Eagerly, initialVS)
  }

  private fun Flow<PartialStateChange>.sendSingleEvent(): Flow<PartialStateChange> {
    return onEach { change ->
      val event = when (change) {
        is PartialStateChange.Errors -> return@onEach
        PartialStateChange.AddUser.Loading -> return@onEach
        is PartialStateChange.AddUser.AddUserSuccess -> SingleEvent.AddUserSuccess(change.user)
        is PartialStateChange.AddUser.AddUserFailure -> SingleEvent.AddUserFailure(
          change.user,
          change.error
        )
        PartialStateChange.FirstChange.EmailChangedFirstTime -> return@onEach
        PartialStateChange.FirstChange.FirstNameChangedFirstTime -> return@onEach
        PartialStateChange.FirstChange.LastNameChangedFirstTime -> return@onEach
        is PartialStateChange.FormValue.EmailChanged -> return@onEach
        is PartialStateChange.FormValue.FirstNameChanged -> return@onEach
        is PartialStateChange.FormValue.LastNameChanged -> return@onEach
      }
      sendEvent(event)
    }
  }

  private fun SharedFlow<ViewIntent>.toPartialStateChangeFlow(initialVS: ViewState): Flow<PartialStateChange> {
    val emailFlow = filterIsInstance<ViewIntent.EmailChanged>()
      .map { it.email }
      .startWith(initialVS.email)
      .distinctUntilChanged()
      .shareWhileSubscribed()

    val firstNameFlow = filterIsInstance<ViewIntent.FirstNameChanged>()
      .map { it.firstName }
      .startWith(initialVS.firstName)
      .distinctUntilChanged()
      .shareWhileSubscribed()

    val lastNameFlow = filterIsInstance<ViewIntent.LastNameChanged>()
      .map { it.lastName }
      .startWith(initialVS.lastName)
      .distinctUntilChanged()
      .shareWhileSubscribed()

    val userFormFlow = combine(
      emailFlow,
      firstNameFlow,
      lastNameFlow,
    ) { email, firstName, lastName ->
      User.create(
        email = email,
        firstName = firstName,
        lastName = lastName,
        id = "",
        avatar = "",
      )
    }.stateWithInitialNullWhileSubscribed()

    val formValuesChangeFlow = merge(
      emailFlow.map { PartialStateChange.FormValue.EmailChanged(it) },
      firstNameFlow.map { PartialStateChange.FormValue.FirstNameChanged(it) },
      lastNameFlow.map { PartialStateChange.FormValue.LastNameChanged(it) },
    )

    return merge(
      // form values change
      formValuesChangeFlow,
      // first change
      toFirstChangeFlow(),
      // errors change
      userFormFlow.toErrorsChangeFlow(),
      // add user change
      filterIsInstance<ViewIntent.Submit>()
        .toAddUserChangeFlow(userFormFlow),
    )
  }

  //region Processors
  private fun SharedFlow<ViewIntent>.toFirstChangeFlow(): Flow<PartialStateChange.FirstChange> =
    merge(
      filterIsInstance<ViewIntent.EmailChanged>()
        .take(1)
        .mapTo(PartialStateChange.FirstChange.EmailChangedFirstTime),
      filterIsInstance<ViewIntent.FirstNameChanged>()
        .take(1)
        .mapTo(PartialStateChange.FirstChange.FirstNameChangedFirstTime),
      filterIsInstance<ViewIntent.LastNameChanged>()
        .take(1)
        .mapTo(PartialStateChange.FirstChange.LastNameChangedFirstTime)
    )

  private fun Flow<ViewIntent.Submit>.toAddUserChangeFlow(userFormFlow: UserFormStateFlow): Flow<PartialStateChange.AddUser> =
    withLatestFrom(userFormFlow) { _, userForm -> userForm }
      .mapNotNull { it?.orNull() }
      .flatMapFirst { user ->
        flowFromSuspend { addUser(user) }
          .map { result ->
            result.fold(
              ifLeft = { PartialStateChange.AddUser.AddUserFailure(user, it) },
              ifRight = { PartialStateChange.AddUser.AddUserSuccess(user) }
            )
          }
          .startWith(PartialStateChange.AddUser.Loading)
      }

  private fun UserFormStateFlow.toErrorsChangeFlow(): Flow<PartialStateChange.Errors> =
    map { validated ->
      PartialStateChange.Errors(
        validated?.fold(
          fe = { it.toPersistentHashSet() },
          fa = { persistentHashSetOf() }
        ) ?: persistentHashSetOf()
      )
    }
  //endregion

  private companion object {
    private const val VIEW_STATE = "com.hoc.flowmvi.ui.add.view_state"
  }
}
