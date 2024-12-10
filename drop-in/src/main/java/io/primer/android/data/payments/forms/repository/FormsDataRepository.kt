@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.data.payments.forms.repository

import io.primer.android.data.payments.forms.datasource.LocalFormDataSourceFactory
import io.primer.android.data.payments.forms.models.toForm
import io.primer.android.domain.payments.forms.repository.FormsRepository
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

internal class FormsDataRepository(
    private val factory: LocalFormDataSourceFactory
) : FormsRepository {

    override fun getForms(paymentMethodType: String) =
        factory.getLocalFormDataSource(
            PaymentMethodType.safeValueOf(paymentMethodType)
        ).get()
            .mapLatest { it.toForm() }
}
