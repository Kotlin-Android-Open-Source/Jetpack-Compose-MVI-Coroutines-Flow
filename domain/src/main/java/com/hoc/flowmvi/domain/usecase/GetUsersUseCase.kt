package com.hoc.flowmvi.domain.usecase

import arrow.core.Either
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.repository.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetUsersUseCase @Inject constructor(private val userRepository: UserRepository) {
  operator fun invoke(): Flow<Either<UserError, List<User>>> = userRepository.getUsers()
}
