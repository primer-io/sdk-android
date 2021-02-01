package io.primer.android.viewmodel

import androidx.lifecycle.ViewModel

internal abstract class BaseViewModel : ViewModel() {
  open fun initialize() {}
}