package io.primer.android.components.domain.payments.paymentMethods.raw.validation

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.bancontact.PrimerBancontactCardData
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.otp.PrimerOtpCodeData
import io.primer.android.components.domain.core.models.phoneNumber.PrimerPhoneNumberData
import io.primer.android.components.domain.core.models.retailOutlet.PrimerRetailerData
import io.primer.android.components.domain.payments.repository.CheckoutModuleRepository
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.card.CardInputDataValidator
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.otp.blik.BlikInputDataValidator
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.phoneNumber.mbway.MBWayPhoneNumberInputDataValidator
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.phoneNumber.ovo.OvoPhoneNumberInputDataValidator
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.card.BancontactCardInputDataValidator
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.retailerOutlet.XenditRetailerOutletInputValidator
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository

internal class PaymentInputDataValidatorFactory(
    private val checkoutModuleRepository: CheckoutModuleRepository,
    private val retailOutletRepository: RetailOutletRepository
) {

    fun getPaymentInputDataValidator(
        paymentMethodType: String,
        inputData: PrimerRawData
    ): PaymentInputDataValidator<PrimerRawData> {
        return when (inputData) {
            is PrimerCardData -> CardInputDataValidator(
                checkoutModuleRepository
            )
            is PrimerBancontactCardData -> BancontactCardInputDataValidator()
            is PrimerPhoneNumberData -> {
                when (paymentMethodType) {
                    PaymentMethodType.XENDIT_OVO.name -> OvoPhoneNumberInputDataValidator()
                    PaymentMethodType.ADYEN_MBWAY.name -> MBWayPhoneNumberInputDataValidator()
                    else -> throw IllegalArgumentException(
                        "Unsupported phone data validation for $paymentMethodType."
                    )
                }
            }
            is PrimerOtpCodeData -> {
                when (paymentMethodType) {
                    PaymentMethodType.ADYEN_BLIK.name -> BlikInputDataValidator()
                    else -> throw IllegalArgumentException(
                        "Unsupported otp data validation for $paymentMethodType."
                    )
                }
            }
            is PrimerRetailerData -> {
                when (paymentMethodType) {
                    PaymentMethodType.XENDIT_RETAIL_OUTLETS.name ->
                        XenditRetailerOutletInputValidator(retailOutletRepository)
                    else -> throw IllegalArgumentException(
                        "Unsupported retailer outlet validation for $paymentMethodType."
                    )
                }
            }
            else -> throw IllegalArgumentException(
                "Unsupported data validation for ${inputData::class}."
            )
        } as PaymentInputDataValidator<PrimerRawData>
    }
}
