package com.hoc.flowmvi.ui.main

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

@Immutable
data class UserItem(
  val id: String,
  val email: String,
  val avatar: String,
  val firstName: String,
  val lastName: String,
  val isDeleting: Boolean
) {
  val fullName get() = "$firstName $lastName"

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

internal sealed interface PartialChange {
  fun reduce(vs: ViewState): ViewState

  sealed class GetUser : PartialChange {
    override fun reduce(vs: ViewState): ViewState {
      return when (this) {
        Loading -> vs.copy(
          isLoading = true,
          error = null
        )
        is Data -> vs.copy(
          isLoading = false,
          error = null,
          userItems = users.toPersistentList()
        )
        is Error -> vs.copy(
          isLoading = false,
          error = error
        )
      }
    }

    object Loading : GetUser()
    data class Data(val users: List<UserItem>) : GetUser()
    data class Error(val error: UserError) : GetUser()
  }

  sealed class Refresh : PartialChange {
    override fun reduce(vs: ViewState): ViewState {
      return when (this) {
        is Success -> vs.copy(isRefreshing = false)
        is Failure -> vs.copy(isRefreshing = false)
        Loading -> vs.copy(isRefreshing = true)
      }
    }

    object Loading : Refresh()
    object Success : Refresh()
    data class Failure(val error: UserError) : Refresh()
  }

  sealed class RemoveUser : PartialChange {
    data class Loading(val user: UserItem) : RemoveUser()
    data class Success(val user: UserItem) : RemoveUser()
    data class Failure(val user: UserItem, val error: Throwable) : RemoveUser()

    override fun reduce(vs: ViewState) = when (this) {
      is Failure -> {
        vs.copy(
          userItems = vs.userItems.mutate { userItems ->
            userItems.forEachIndexed { index, userItem ->
              if (userItem.id == user.id) {
                userItems[index] = userItem.copy(isDeleting = false)
                return@mutate
              }
            }
          }
        )
      }
      is Loading -> vs.copy(
        userItems = vs.userItems.mutate { userItems ->
          userItems.forEachIndexed { index, userItem ->
            if (userItem.id == user.id) {
              userItems[index] = userItem.copy(isDeleting = true)
              return@mutate
            }
          }
        }
      )
      is Success -> vs
    }
  }
}

sealed interface SingleEvent : MviSingleEvent {
  sealed class Refresh : SingleEvent {
    object Success : Refresh()
    data class Failure(val error: Throwable) : Refresh()
  }

  data class GetUsersError(val error: Throwable) : SingleEvent

  sealed class RemoveUser : SingleEvent {
    data class Success(val user: UserItem) : RemoveUser()
    data class Failure(val user: UserItem, val error: Throwable) : RemoveUser()
  }
}
