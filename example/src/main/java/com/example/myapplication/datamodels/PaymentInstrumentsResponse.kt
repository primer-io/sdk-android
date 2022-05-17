package com.example.myapplication.datamodels

import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData

data class PaymentInstrumentsResponse(val data: List<PrimerPaymentMethodTokenData>)