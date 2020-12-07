package io.primer.android.viewmodel

import androidx.lifecycle.ViewModel
import io.primer.android.model.Model

internal abstract class BaseViewModel(protected val model: Model) : ViewModel() {
  open fun initialize() {}
}