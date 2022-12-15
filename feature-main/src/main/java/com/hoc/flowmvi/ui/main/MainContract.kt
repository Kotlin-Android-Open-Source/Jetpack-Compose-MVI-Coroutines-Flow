package com.hoc.flowmvi.ui.main

import androidx.annotation.MainThread
import androidx.compose.runtime.Immutable
import arrow.core.Either
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.mvi_base.MviIntent
import com.hoc.flowmvi.mvi_base.MviSingleEvent
import com.hoc.flowmvi.mvi_base.MviViewState
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlin.LazyThreadSafetyMode.NONE

@Immutable
data class UserItem(
  val id: String,
  val email: String,
  val avatar: String,
  val firstName: String,
  val lastName: String,
  val isDeleting: Boolean
) {
  @get:MainThread
  val fullName by lazy(NONE) { "$firstName $lastName" }

  constructor(domain: User) : this(
    id = domain.id,
    email = domain.email.value,
    avatar = domain.avatar,
    firstName = domain.firstName.value,
    lastName = domain.lastName.value,
    isDeleting = false,
  )

  fun toDomain(): Either<UserError.ValidationFailed, User> = User.create(
    id = id,
    lastName = lastName,
    firstName = firstName,
    avatar = avatar,
    email = email
  ).toEither().mapLeft { UserError.ValidationFailed(it.toSet()) }
}

@Immutable
sealed interface ViewIntent : MviIntent {
  object Initial : ViewIntent
  object Refresh : ViewIntent
  object Retry : ViewIntent
  data class RemoveUser(val user: UserItem) : ViewIntent
}

@Immutable
data class ViewState(
  val userItems: PersistentList<UserItem>,
  val isLoading: Boolean,
  val error: UserError?,
  val isRefreshing: Boolean
) : MviViewState {
  companion object {
    fun initial() = ViewState(
      userItems = persistentListOf(),
      isLoading = true,
      error = null,
      isRefreshing = false
    )
  }
}

internal sealed interface PartialStateChange {
  fun reduce(viewState: ViewState): ViewState

  sealed interface Users : PartialStateChange {
    override fun reduce(viewState: ViewState): ViewState {
      return when (this) {
        Loading -> viewState.copy(
          isLoading = true,
          error = null
        )
        is Data -> viewState.copy(
          isLoading = false,
          error = null,
          userItems = users.toPersistentList()
        )
        is Error -> viewState.copy(
          isLoading = false,
          error = error
        )
      }
    }

    object Loading : Users
    data class Data(val users: List<UserItem>) : Users
    data class Error(val error: UserError) : Users
  }

  sealed interface Refresh : PartialStateChange {
    override fun reduce(viewState: ViewState): ViewState {
      return when (this) {
        is Success -> viewState.copy(isRefreshing = false)
        is Failure -> viewState.copy(isRefreshing = false)
        Loading -> viewState.copy(isRefreshing = true)
      }
    }

    object Loading : Refresh
    object Success : Refresh
    data class Failure(val error: UserError) : Refresh
  }

  sealed interface RemoveUser : PartialStateChange {
    data class Loading(val user: UserItem) : RemoveUser
    data class Success(val user: UserItem) : RemoveUser
    data class Failure(val user: UserItem, val error: UserError) : RemoveUser

    override fun reduce(viewState: ViewState) = when (this) {
      is Failure -> {
        // if the user is not found, remove it from the current list.
        if (error is UserError.UserNotFound && error.id == user.id) {
          viewState.copy(
            userItems = viewState
              .userItems
              .removeAll { it.id == user.id }
          )
        } else {
          viewState.copy(
            userItems = viewState.userItems.mutate { userItems ->
              userItems.forEachIndexed { index, userItem ->
                if (userItem.id == user.id) {
                  userItems[index] = userItem.copy(isDeleting = false)
                  return@mutate
                }
              }
            }
          )
        }
      }
      is Loading -> viewState.copy(
        userItems = viewState.userItems.mutate { userItems ->
          userItems.forEachIndexed { index, userItem ->
            if (userItem.id == user.id) {
              userItems[index] = userItem.copy(isDeleting = true)
              return@mutate
            }
          }
        }
      )
      is Success -> viewState
    }
  }
}

sealed interface SingleEvent : MviSingleEvent {
  sealed interface Refresh : SingleEvent {
    object Success : Refresh
    data class Failure(val error: UserError) : Refresh
  }

  data class GetUsersError(val error: UserError) : SingleEvent

  sealed interface RemoveUser : SingleEvent {
    data class Success(val user: UserItem) : RemoveUser
    data class Failure(val user: UserItem, val error: UserError) : RemoveUser
  }
}
