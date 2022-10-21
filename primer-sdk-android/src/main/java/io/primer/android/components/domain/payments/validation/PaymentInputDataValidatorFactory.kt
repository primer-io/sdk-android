package io.primer.android.components.domain.payments.validation

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.bancontact.PrimerRawBancontactCardData
import io.primer.android.components.domain.core.models.card.PrimerRawCardData
import io.primer.android.components.domain.core.models.otp.PrimerOtpCodeRawData
import io.primer.android.components.domain.core.models.phoneNumber.PrimerRawPhoneNumberData
import io.primer.android.components.domain.core.models.retailOutlet.PrimerRawRetailerData
import io.primer.android.components.domain.payments.repository.CheckoutModuleRepository
import io.primer.android.components.domain.payments.validation.card.BancontactCardInputDataValidator
import io.primer.android.components.domain.payments.validation.card.CardInputDataValidator
import io.primer.android.components.domain.payments.validation.otp.blik.BlikInputDataValidator
import io.primer.android.components.domain.payments.validation.phoneNumber.mbway.MBWayPhoneNumberInputDataValidator
import io.primer.android.components.domain.payments.validation.phoneNumber.ovo.OvoPhoneNumberInputDataValidator
import io.primer.android.components.domain.payments.validation.retailerOutlet.XenditRetailerOutletInputValidator
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
            is PrimerRawCardData -> CardInputDataValidator(
                checkoutModuleRepository,
            )
            is PrimerRawBancontactCardData -> BancontactCardInputDataValidator()
            is PrimerRawPhoneNumberData -> {
                when (paymentMethodType) {
                    PaymentMethodType.XENDIT_OVO.name -> OvoPhoneNumberInputDataValidator()
                    PaymentMethodType.ADYEN_MBWAY.name -> MBWayPhoneNumberInputDataValidator()
                    else -> throw IllegalArgumentException(
                        "Unsupported phone data validation for $paymentMethodType."
                    )
                }
            }
            is PrimerOtpCodeRawData -> {
                when (paymentMethodType) {
                    PaymentMethodType.ADYEN_BLIK.name -> BlikInputDataValidator()
                    else -> throw IllegalArgumentException(
                        "Unsupported otp data validation for $paymentMethodType."
                    )
                }
            }
            is PrimerRawRetailerData -> {
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
