package com.hoc.flowmvi.ui.add

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.model.UserValidationError
import com.hoc.flowmvi.mvi_base.MviIntent
import com.hoc.flowmvi.mvi_base.MviSingleEvent
import com.hoc.flowmvi.mvi_base.MviViewState
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

internal object UserValidationErrorPersistentSetParceler :
  Parceler<PersistentSet<UserValidationError>> {
  override fun create(parcel: Parcel) =
    persistentHashSetOf<UserValidationError>().mutate { errors ->
      repeat(parcel.readInt()) {
        errors.add(UserValidationError.valueOf(parcel.readString()!!))
      }
    }

  override fun PersistentSet<UserValidationError>.write(parcel: Parcel, flags: Int) {
    parcel.writeInt(size)
    forEach { parcel.writeString(it.name) }
  }
}

@Immutable
@Parcelize
@TypeParceler<PersistentSet<UserValidationError>, UserValidationErrorPersistentSetParceler>
data class ViewState(
  val errors: PersistentSet<UserValidationError>,
  val isLoading: Boolean,
  // show error or not
  val emailChanged: Boolean,
  val firstNameChanged: Boolean,
  val lastNameChanged: Boolean,
  // form values
  val email: String?,
  val firstName: String?,
  val lastName: String?,
) : MviViewState, Parcelable {
  companion object {
    fun initial() = ViewState(
      errors = persistentHashSetOf(),
      isLoading = false,
      emailChanged = false,
      firstNameChanged = false,
      lastNameChanged = false,
      email = null,
      firstName = null,
      lastName = null,
    )
  }
}

@Immutable
sealed interface ViewIntent : MviIntent {
  data class EmailChanged(val email: String) : ViewIntent
  data class FirstNameChanged(val firstName: String) : ViewIntent
  data class LastNameChanged(val lastName: String) : ViewIntent

  object Submit : ViewIntent
}

internal sealed interface PartialStateChange {
  fun reduce(viewState: ViewState): ViewState

  data class Errors(val errors: PersistentSet<UserValidationError>) : PartialStateChange {
    override fun reduce(viewState: ViewState) =
      if (viewState.errors == errors) viewState
      else viewState.copy(errors = errors)
  }

  sealed interface AddUser : PartialStateChange {
    object Loading : AddUser
    data class AddUserSuccess(val user: User) : AddUser
    data class AddUserFailure(val user: User, val error: UserError) : AddUser

    override fun reduce(viewState: ViewState): ViewState {
      return when (this) {
        Loading -> viewState.copy(isLoading = true)
        is AddUserSuccess -> viewState.copy(isLoading = false)
        is AddUserFailure -> viewState.copy(isLoading = false)
      }
    }
  }

  sealed interface FirstChange : PartialStateChange {
    object EmailChangedFirstTime : FirstChange
    object FirstNameChangedFirstTime : FirstChange
    object LastNameChangedFirstTime : FirstChange

    override fun reduce(viewState: ViewState): ViewState {
      return when (this) {
        EmailChangedFirstTime -> {
          if (viewState.emailChanged) viewState
          else viewState.copy(emailChanged = true)
        }
        FirstNameChangedFirstTime -> {
          if (viewState.firstNameChanged) viewState
          else viewState.copy(firstNameChanged = true)
        }
        LastNameChangedFirstTime -> {
          if (viewState.lastNameChanged) viewState
          else viewState.copy(lastNameChanged = true)
        }
      }
    }
  }

  sealed interface FormValue : PartialStateChange {
    override fun reduce(viewState: ViewState): ViewState {
      return when (this) {
        is EmailChanged -> {
          if (viewState.email == email) viewState
          else viewState.copy(email = email)
        }
        is FirstNameChanged -> {
          if (viewState.firstName == firstName) viewState
          else viewState.copy(firstName = firstName)
        }
        is LastNameChanged -> {
          if (viewState.lastName == lastName) viewState
          else viewState.copy(lastName = lastName)
        }
      }
    }

    data class EmailChanged(val email: String?) : FormValue
    data class FirstNameChanged(val firstName: String?) : FormValue
    data class LastNameChanged(val lastName: String?) : FormValue
  }
}

sealed interface SingleEvent : MviSingleEvent {
  data class AddUserSuccess(val user: User) : SingleEvent
  data class AddUserFailure(val user: User, val error: UserError) : SingleEvent
}
