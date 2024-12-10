package io.primer.android.paymentmethods.core.configuration.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfiguration
import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfigurationParams
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * A base interactor class for handling payment method configurations.
 *
 * @param T The type of the payment method configuration that extends [PaymentMethodConfiguration].
 * @param P The type of the parameters required to fetch the payment method configuration, extending [PaymentMethodConfigurationParams].
 * @property configurationRepository A repository that provides access to the payment method configuration data.
 * @property dispatcher The [CoroutineDispatcher] used to execute the interactor's actions. Defaults to [Dispatchers.Default].
 */
open class PaymentMethodConfigurationInteractor<T : PaymentMethodConfiguration, P : PaymentMethodConfigurationParams>(
    private val configurationRepository: PaymentMethodConfigurationRepository<T, P>,
    override val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseSuspendInteractor<T, P>() {

    /**
     * Performs the action to retrieve the payment method configuration based on the given parameters.
     *
     * @param params The parameters required to fetch the payment method configuration.
     * @return A [Result] containing the payment method configuration of type [T], or an error if the operation fails.
     */
    override suspend fun performAction(params: P): Result<T> {
        return configurationRepository.getPaymentMethodConfiguration(params = params)
    }
}
