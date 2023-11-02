package io.primer.android.components.domain.payments.paymentMethods.nolpay.models

import io.primer.android.data.configuration.models.Environment

internal data class NolPayConfiguration(val merchantAppId: String, val environment: Environment)
