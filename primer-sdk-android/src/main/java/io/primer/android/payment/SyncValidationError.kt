package io.primer.android.payment

data class SyncValidationError(
  val name: String,
  val errorId: Int,
  val fieldId: Int,
)