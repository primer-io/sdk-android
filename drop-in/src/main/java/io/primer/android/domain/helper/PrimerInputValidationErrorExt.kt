package io.primer.android.domain.helper

import io.primer.android.R
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.model.SyncValidationError

@Suppress("LongMethod")
internal fun PrimerInputValidationError.toSyncValidationError(cardData: PrimerCardData?) =
    when (errorId) {
        "invalid-card-number" ->
            SyncValidationError(
                inputElementType = inputElementType,
                errorId = errorId,
                errorFormatId =
                if (cardData?.cardNumber.isNullOrBlank()) {
                    R.string.form_error_required
                } else {
                    R.string.form_error_invalid
                },
                fieldId = R.string.card_number,
            )

        "unsupported-card-type" ->
            SyncValidationError(
                inputElementType = inputElementType,
                errorId = errorId,
                errorFormatId =
                if (cardData?.cardNumber.isNullOrBlank()) {
                    R.string.form_error_required
                } else {
                    R.string.form_error_card_type_not_supported
                },
                fieldId = R.string.card_number,
            )

        "invalid-cvv" ->
            SyncValidationError(
                inputElementType = inputElementType,
                errorId = errorId,
                errorFormatId =
                if (cardData?.cvv.isNullOrBlank()) {
                    R.string.form_error_required
                } else {
                    R.string.form_error_invalid
                },
                fieldId = R.string.card_cvv,
            )

        "invalid-expiry-date" ->
            SyncValidationError(
                inputElementType = inputElementType,
                errorId = errorId,
                errorFormatId =
                if (cardData?.expiryDate.isNullOrBlank()) {
                    R.string.form_error_required
                } else {
                    R.string.form_error_invalid
                },
                fieldId = R.string.card_expiry,
            )

        "invalid-cardholder-name" ->
            SyncValidationError(
                inputElementType = inputElementType,
                errorId = errorId,
                errorFormatId =
                if (cardData?.cardHolderName.isNullOrBlank()) {
                    R.string.form_error_required
                } else {
                    R.string.form_error_card_holder_name_length
                },
                fieldId = R.string.card_holder_name,
            )

        else -> error("Unsupported error id mapping for $errorId")
    }
