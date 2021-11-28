package com.hoc.flowmvi.data.mapper

import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.data.remote.UserBody
import com.hoc.flowmvi.domain.model.User
import javax.inject.Inject

internal class UserDomainToUserBodyMapper @Inject constructor() : Mapper<User, UserBody> {
  override fun invoke(param: User): UserBody {
    return UserBody(
      email = param.email.value,
      firstName = param.firstName.value,
      lastName = param.lastName.value
    )
  }
}
