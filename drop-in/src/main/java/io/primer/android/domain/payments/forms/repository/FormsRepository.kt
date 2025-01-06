package io.primer.android.domain.payments.forms.repository

import io.primer.android.domain.payments.forms.models.Form
import kotlinx.coroutines.flow.Flow

internal interface FormsRepository {
    fun getForms(paymentMethodType: String): Flow<Form>
}
