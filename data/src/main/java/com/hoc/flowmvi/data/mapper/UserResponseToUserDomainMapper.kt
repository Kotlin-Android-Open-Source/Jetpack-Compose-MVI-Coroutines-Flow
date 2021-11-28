package com.hoc.flowmvi.data.mapper

import arrow.core.ValidatedNel
import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserValidationError
import javax.inject.Inject

internal class UserResponseToUserDomainMapper @Inject constructor() :
  Mapper<UserResponse, ValidatedNel<UserValidationError, User>> {
  override fun invoke(param: UserResponse): ValidatedNel<UserValidationError, User> {
    return User.create(
      id = param.id,
      avatar = param.avatar,
      email = param.email,
      firstName = param.firstName,
      lastName = param.lastName
    )
  }
}
