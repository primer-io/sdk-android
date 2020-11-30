package io.primer.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.primer.android.model.Model

class PrimerViewModelFactory(private val model: Model) : ViewModelProvider.Factory {
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return PrimerViewModel(model) as T
  }
}