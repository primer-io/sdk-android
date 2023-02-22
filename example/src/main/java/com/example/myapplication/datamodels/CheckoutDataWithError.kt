package com.example.myapplication.datamodels

import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.create.model.Payment

data class CheckoutDataWithError(val payment: Payment? = null, val error: PrimerError? = null)
