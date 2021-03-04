package com.hoc.flowmvi.data.mapper

import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.entity.User
import javax.inject.Inject

internal class UserResponseToUserDomainMapper @Inject constructor() : Mapper<UserResponse, User> {
  override fun invoke(param: UserResponse): User {
    return User(
      id = param.id,
      avatar = param.avatar,
      email = param.email,
      firstName = param.firstName,
      lastName = param.lastName
    )
  }
}
