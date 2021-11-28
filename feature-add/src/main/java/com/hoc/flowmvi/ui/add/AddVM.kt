package com.hoc.flowmvi.ui.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.hoc.flowmvi.domain.usecase.AddUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class AddVM @Inject constructor(
  private val addUser: AddUserUseCase,
  private val savedStateHandle: SavedStateHandle
) : ViewModel()
