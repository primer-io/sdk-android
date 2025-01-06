package io.primer.android.banks.implementation.validation.validator

import io.primer.android.banks.implementation.rpc.domain.models.IssuingBank
import io.primer.android.components.domain.error.PrimerValidationError

internal val banksNotLoadedPrimerValidationError by lazy {
    PrimerValidationError(
        errorId = BanksValidations.BANKS_NOT_LOADED_ERROR_ID,
        description = "Banks need to be loaded before bank id can be collected.",
    )
}

internal object BankIdValidator {
    fun validate(
        banks: List<IssuingBank>?,
        bankId: String,
    ): PrimerValidationError? =
        if (banks == null) {
            banksNotLoadedPrimerValidationError
        } else if (banks.none { it.id == bankId }) {
            PrimerValidationError(
                errorId = BanksValidations.INVALID_BANK_ID_ERROR_ID,
                description = "Bank id doesn't belong to any of the supported banks.",
            )
        } else {
            null
        }
}
