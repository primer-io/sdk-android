package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.repository

import io.primer.android.PrimerSessionIntent
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource.RemoteKlarnaCheckoutPaymentSessionDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource.RemoteKlarnaVaultPaymentSessionDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.exception.KlarnaIllegalValueKey
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.AddressData
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCheckoutPaymentSessionDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateVaultPaymentSessionDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.KlarnaSessionType
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.LocaleDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.toKlarnaSession
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSession
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository.KlarnaSessionRepository
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.AddressDataResponse
import io.primer.android.data.configuration.models.CustomerDataResponse
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.SessionCreateException
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.extensions.runSuspendCatching
import io.primer.android.http.exception.HttpException

internal class KlarnaSessionDataRepository(
    private val klarnaCheckoutPaymentSessionDataSource:
        RemoteKlarnaCheckoutPaymentSessionDataSource,
    private val klarnaVaultPaymentSessionDataSource:
        RemoteKlarnaVaultPaymentSessionDataSource,
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val config: PrimerConfig
) : KlarnaSessionRepository {
    override suspend fun createSession(
        surcharge: Int?,
        primerSessionIntent: PrimerSessionIntent
    ): Result<KlarnaSession> {
        val paymentMethodConfig =
            localConfigurationDataSource.getConfiguration().paymentMethods
                .first { it.type == PaymentMethodType.KLARNA.name }
        val customer = config.settings.customer
        return runSuspendCatching {
            when (primerSessionIntent) {
                PrimerSessionIntent.VAULT -> createVaultSession(paymentMethodConfig, customer)
                PrimerSessionIntent.CHECKOUT -> createCheckoutSession(
                    paymentMethodConfig = paymentMethodConfig,
                    customer = customer,
                    surcharge = surcharge
                )
            }.toKlarnaSession()
        }.recoverCatching {
            when {
                it is HttpException && it.isClientError() ->
                    throw SessionCreateException(
                        PaymentMethodType.KLARNA,
                        it.error.diagnosticsId,
                        it.error.description
                    )

                else -> throw it
            }
        }
    }

    private suspend fun createVaultSession(
        paymentMethodConfig: PaymentMethodConfigDataResponse,
        customer: CustomerDataResponse
    ) = klarnaVaultPaymentSessionDataSource.execute(
        BaseRemoteRequest(
            configuration = localConfigurationDataSource.getConfiguration(),
            data = CreateVaultPaymentSessionDataRequest(
                paymentMethodConfigId = requireNotNullCheck(
                    paymentMethodConfig.id,
                    KlarnaIllegalValueKey.PAYMENT_METHOD_CONFIG_ID
                ),
                sessionType = KlarnaSessionType.RECURRING_PAYMENT,
                description =
                config.settings.paymentMethodOptions.klarnaOptions.recurringPaymentDescription,
                localeData = LocaleDataRequest(
                    config.settings.order.countryCode,
                    config.settings.currency,
                    config.settings.locale.toLanguageTag()
                )
            )
        )
    )

    private suspend fun createCheckoutSession(
        paymentMethodConfig: PaymentMethodConfigDataResponse,
        customer: CustomerDataResponse,
        surcharge: Int?
    ) = klarnaCheckoutPaymentSessionDataSource.execute(
        BaseRemoteRequest(
            configuration = localConfigurationDataSource.getConfiguration(),
            data = CreateCheckoutPaymentSessionDataRequest(
                paymentMethodConfigId = requireNotNullCheck(
                    paymentMethodConfig.id,
                    KlarnaIllegalValueKey.PAYMENT_METHOD_CONFIG_ID
                ),
                sessionType = KlarnaSessionType.ONE_OFF_PAYMENT,
                totalAmount = requireNotNullCheck(
                    config.settings.order.totalOrderAmount,
                    KlarnaIllegalValueKey.TOTAL_ORDER_AMOUNT
                ),
                localeData = LocaleDataRequest(
                    config.settings.order.countryCode,
                    config.settings.currency,
                    config.settings.locale.toLanguageTag()
                ),
                orderItems = createOrderItems(surcharge),
                billingAddress = customer.billingAddress?.toAddressData(customer),
                shippingAddress = customer.shippingAddress?.toAddressData(customer)
            )
        )
    )

    private fun createOrderItems(surcharge: Int?) = buildList {
        addAll(
            config.settings.order.lineItems.map {
                CreateCheckoutPaymentSessionDataRequest.OrderItem(
                    name = requireNotNullCheck(
                        it.description,
                        KlarnaIllegalValueKey.ORDER_LINE_ITEM_DESCRIPTION
                    ),
                    unitAmount = requireNotNullCheck(
                        it.unitAmount,
                        KlarnaIllegalValueKey.ORDER_LINE_ITEM_UNIT_AMOUNT
                    ),
                    reference = it.itemId,
                    quantity = it.quantity,
                    discountAmount = it.discountAmount,
                    productType = it.productType,
                    taxAmount = it.taxAmount
                )
            }
        )
        if (surcharge != null) {
            add(createSurchargeOrderItem(surcharge))
        }
    }

    private fun createSurchargeOrderItem(surcharge: Int) =
        CreateCheckoutPaymentSessionDataRequest.OrderItem(
            name = "surcharge",
            unitAmount = surcharge,
            reference = null,
            quantity = 1,
            discountAmount = null,
            productType = "surcharge",
            taxAmount = null
        )
}

internal fun AddressDataResponse.toAddressData(
    customerDataResponse: CustomerDataResponse
) = AddressData(
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    addressLine3 = null,
    city = city,
    countryCode = countryCode?.name,
    email = customerDataResponse.emailAddress,
    firstName = firstName,
    lastName = lastName,
    phoneNumber = customerDataResponse.mobileNumber,
    postalCode = postalCode,
    state = state,
    title = null
)
