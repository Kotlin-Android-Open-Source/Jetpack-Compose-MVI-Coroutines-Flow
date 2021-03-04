package com.hoc.flowmvi.data.mapper

import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.data.remote.UserBody
import com.hoc.flowmvi.domain.entity.User
import javax.inject.Inject

internal class UserDomainToUserBodyMapper @Inject constructor() : Mapper<User, UserBody> {
  override fun invoke(param: User): UserBody {
    return UserBody(
      email = param.email,
      avatar = param.avatar,
      firstName = param.firstName,
      lastName = param.lastName
    )
  }
}
