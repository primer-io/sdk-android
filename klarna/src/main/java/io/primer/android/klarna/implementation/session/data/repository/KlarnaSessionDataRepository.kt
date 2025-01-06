package io.primer.android.klarna.implementation.session.data.repository

import io.primer.android.PrimerSessionIntent
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.AddressData
import io.primer.android.configuration.data.model.CustomerDataResponse
import io.primer.android.configuration.data.model.OrderDataResponse
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.errors.data.exception.SessionCreateException
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.klarna.implementation.session.data.datasource.RemoteKlarnaCheckoutPaymentSessionDataSource
import io.primer.android.klarna.implementation.session.data.datasource.RemoteKlarnaVaultPaymentSessionDataSource
import io.primer.android.klarna.implementation.session.data.exception.KlarnaIllegalValueKey
import io.primer.android.klarna.implementation.session.data.models.CreateCheckoutPaymentSessionDataRequest
import io.primer.android.klarna.implementation.session.data.models.CreateVaultPaymentSessionDataRequest
import io.primer.android.klarna.implementation.session.data.models.KlarnaSessionType
import io.primer.android.klarna.implementation.session.data.models.LocaleDataRequest
import io.primer.android.klarna.implementation.session.data.models.toKlarnaSession
import io.primer.android.klarna.implementation.session.domain.models.KlarnaSession
import io.primer.android.klarna.implementation.session.domain.repository.KlarnaSessionRepository
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

internal class KlarnaSessionDataRepository(
    private val klarnaCheckoutPaymentSessionDataSource: RemoteKlarnaCheckoutPaymentSessionDataSource,
    private val klarnaVaultPaymentSessionDataSource: RemoteKlarnaVaultPaymentSessionDataSource,
    private val configurationDataSource: CacheConfigurationDataSource,
    private val config: PrimerConfig,
) : KlarnaSessionRepository {
    override suspend fun createSession(
        surcharge: Int?,
        primerSessionIntent: PrimerSessionIntent,
    ): Result<KlarnaSession> {
        val paymentMethodConfig =
            configurationDataSource.get().paymentMethods
                .first { it.type == PaymentMethodType.KLARNA.name }
        val customer = configurationDataSource.get().clientSession.customer
        val order = configurationDataSource.get().clientSession.order
        return runSuspendCatching {
            when (primerSessionIntent) {
                PrimerSessionIntent.VAULT ->
                    createVaultSession(
                        paymentMethodConfig = paymentMethodConfig,
                        order = requireNotNull(order),
                    )

                PrimerSessionIntent.CHECKOUT ->
                    createCheckoutSession(
                        paymentMethodConfig = paymentMethodConfig,
                        customer = requireNotNull(customer),
                        order = requireNotNull(order),
                        surcharge = surcharge,
                    )
            }.toKlarnaSession()
        }.recoverCatching {
            when {
                it is HttpException && it.isClientError() ->
                    throw SessionCreateException(
                        PaymentMethodType.KLARNA.name,
                        it.error.diagnosticsId,
                        it.error.description,
                    )

                else -> throw it
            }
        }
    }

    private suspend fun createVaultSession(
        paymentMethodConfig: PaymentMethodConfigDataResponse,
        order: OrderDataResponse,
    ) = klarnaVaultPaymentSessionDataSource.execute(
        BaseRemoteHostRequest(
            host = configurationDataSource.get().coreUrl,
            data =
                CreateVaultPaymentSessionDataRequest(
                    paymentMethodConfigId =
                        requireNotNullCheck(
                            paymentMethodConfig.id,
                            KlarnaIllegalValueKey.PAYMENT_METHOD_CONFIG_ID,
                        ),
                    sessionType = KlarnaSessionType.RECURRING_PAYMENT,
                    description =
                        config.settings.paymentMethodOptions.klarnaOptions.recurringPaymentDescription,
                    localeData =
                        LocaleDataRequest(
                            order.countryCode,
                            order.currencyCode.orEmpty(),
                            config.settings.locale.toLanguageTag(),
                        ),
                ),
        ),
    )

    private suspend fun createCheckoutSession(
        paymentMethodConfig: PaymentMethodConfigDataResponse,
        customer: CustomerDataResponse,
        order: OrderDataResponse,
        surcharge: Int?,
    ) = klarnaCheckoutPaymentSessionDataSource.execute(
        BaseRemoteHostRequest(
            host = configurationDataSource.get().coreUrl,
            data =
                CreateCheckoutPaymentSessionDataRequest(
                    paymentMethodConfigId =
                        requireNotNullCheck(
                            paymentMethodConfig.id,
                            KlarnaIllegalValueKey.PAYMENT_METHOD_CONFIG_ID,
                        ),
                    sessionType = KlarnaSessionType.ONE_OFF_PAYMENT,
                    totalAmount =
                        requireNotNullCheck(
                            order.totalOrderAmount,
                            KlarnaIllegalValueKey.TOTAL_ORDER_AMOUNT,
                        ),
                    localeData =
                        LocaleDataRequest(
                            order.countryCode,
                            order.currencyCode.orEmpty(),
                            config.settings.locale.toLanguageTag(),
                        ),
                    orderItems = createOrderItems(surcharge, order),
                    billingAddress = customer.billingAddress?.toAddressData(customer),
                    shippingAddress = customer.shippingAddress?.toAddressData(customer),
                ),
        ),
    )

    private fun createOrderItems(
        surcharge: Int?,
        order: OrderDataResponse,
    ) = buildList {
        addAll(
            order.lineItems.map {
                CreateCheckoutPaymentSessionDataRequest.OrderItem(
                    name =
                        requireNotNullCheck(
                            it.description,
                            KlarnaIllegalValueKey.ORDER_LINE_ITEM_DESCRIPTION,
                        ),
                    unitAmount =
                        requireNotNullCheck(
                            it.unitAmount,
                            KlarnaIllegalValueKey.ORDER_LINE_ITEM_UNIT_AMOUNT,
                        ),
                    reference = it.itemId,
                    quantity = it.quantity,
                    discountAmount = it.discountAmount,
                    productType = it.productType,
                    taxAmount = it.taxAmount,
                )
            },
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
            taxAmount = null,
        )
}

internal fun AddressData.toAddressData(customerDataResponse: CustomerDataResponse) =
    AddressData(
        addressLine1 = addressLine1,
        addressLine2 = addressLine2,
        addressLine3 = null,
        city = city,
        countryCode = countryCode,
        email = customerDataResponse.emailAddress,
        firstName = firstName,
        lastName = lastName,
        phoneNumber = customerDataResponse.mobileNumber,
        postalCode = postalCode,
        state = state,
    )
