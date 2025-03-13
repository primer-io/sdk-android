package io.primer.components.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A data class representing a validation error encountered during the payment method data validation process.
 *
 * @property code A unique identifier for the error.
 * @property message A descriptive message explaining the error.
 */
@Parcelize
data class ValidationError(
    val code: String,
    val message: String,
) : Parcelable
