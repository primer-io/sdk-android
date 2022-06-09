package io.primer.android.data.payments.forms.repository

import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.forms.datasource.LocalFormDataSourceFactory
import io.primer.android.data.payments.forms.models.toForm
import io.primer.android.data.token.datasource.LocalClientTokenDataSource
import io.primer.android.domain.payments.forms.repository.FormsRepository
import kotlinx.coroutines.flow.mapLatest

internal class FormsDataRepository(
    private val factory: LocalFormDataSourceFactory,
    private val clientTokenDataSource: LocalClientTokenDataSource
) :
    FormsRepository {

    override fun getForms(paymentMethodType: String) =
        factory.getLocalFormDataSource(
            PaymentMethodType.safeValueOf(paymentMethodType),
            clientTokenDataSource.get()
        ).get()
            .mapLatest { it.toForm() }
}
