package io.primer.android.viewmodel

import androidx.lifecycle.ViewModel
import io.primer.android.model.Model

internal abstract class BaseViewModel : ViewModel() {
  protected var model: Model? = null

  fun requireModel(): Model {
    return model!!
  }

  open fun initialize(model: Model) {
    this.model = model
  }
}