package io.primer.nolpay.api

import android.nfc.Tag
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import com.snowballtech.transit.rta.Transit
import com.snowballtech.transit.rta.TransitException
import com.snowballtech.transit.rta.configuration.TransitAppSecretKeyHandler
import com.snowballtech.transit.rta.configuration.TransitConfiguration
import com.snowballtech.transit.rta.module.payment.TransitLinkPaymentCardOTPRequest
import com.snowballtech.transit.rta.module.payment.TransitLinkPaymentCardRequest
import com.snowballtech.transit.rta.module.payment.TransitLinkPaymentCardTokenRequest
import com.snowballtech.transit.rta.module.payment.TransitPayRequest
import com.snowballtech.transit.rta.module.payment.TransitPaymentCardListRequest
import com.snowballtech.transit.rta.module.payment.TransitUnlinkPaymentCardOTPRequest
import com.snowballtech.transit.rta.module.payment.TransitUnlinkPaymentCardRequest
import io.primer.nolpay.api.exceptions.NolPaySdkException
import io.primer.nolpay.api.models.PrimerLinkCardMetadata
import io.primer.nolpay.api.models.PrimerNolPaymentCard
import io.primer.nolpay.api.models.PrimerUnlinkCardMetadata

object PrimerNolPay {
    // region SDK initialization
    @MainThread
    @Throws(NolPaySdkException::class)
    fun initSDK(
        enableSandbox: Boolean,
        enableDebug: Boolean,
        merchantAppId: String,
        handler: TransitAppSecretKeyHandler,
    ) = try {
        TransitConfiguration.Builder()
            .enableSandbox(enableSandbox)
            .enableDebug(enableDebug)
            .setAppID(merchantAppId)
            .setAppSecretKeyHandler(handler)
            .build()
            .apply {
                Transit.initSDK(this)
            }
    } catch (e: TransitException) {
        throw NolPaySdkException(e.code, e.message)
    }
    // endregion SDK initialization

    // region link payment card
    @WorkerThread
    @Throws(NolPaySdkException::class)
    fun getLinkPaymentCardToken(tag: Tag) =
        try {
            Transit.getPaymentInstance().getLinkPaymentCardToken(
                TransitLinkPaymentCardTokenRequest.Builder()
                    .setTag(tag)
                    .build(),
            ).let { linkResponse ->
                PrimerLinkCardMetadata(
                    requireNotNull(linkResponse.linkToken),
                    linkResponse.cardNumber,
                )
            }
        } catch (e: TransitException) {
            throw NolPaySdkException(e.code, e.message)
        }

    @WorkerThread
    @Throws(NolPaySdkException::class)
    fun getLinkPaymentCardOTP(
        mobileNumber: String,
        countryCallingCode: String,
        linkToken: String,
    ) = try {
        Transit.getPaymentInstance().getLinkPaymentCardOTP(
            TransitLinkPaymentCardOTPRequest.Builder()
                .setMobile(mobileNumber)
                .setLinkPaymentCardToken(linkToken)
                .setRegionCode(countryCallingCode)
                .build(),
        )
    } catch (e: TransitException) {
        throw NolPaySdkException(e.code, e.message)
    }

    @WorkerThread
    @Throws(NolPaySdkException::class)
    fun linkPaymentCard(
        linkToken: String,
        otpCode: String,
    ) = try {
        Transit.getPaymentInstance().linkPaymentCard(
            TransitLinkPaymentCardRequest.Builder()
                .setOTPCode(otpCode)
                .setLinkPaymentCardToken(linkToken)
                .build(),
        )
    } catch (e: TransitException) {
        throw NolPaySdkException(e.code, e.message)
    }
    // endregion link payment card

    // region unlink payment card
    @WorkerThread
    @Throws(NolPaySdkException::class)
    fun getUnlinkPaymentCardOTP(
        mobileNumber: String,
        countryCallingCode: String,
        cardNumber: String,
    ) = try {
        Transit.getPaymentInstance().getUnlinkPaymentCardOTP(
            TransitUnlinkPaymentCardOTPRequest.Builder()
                .setMobile(mobileNumber)
                .setRegionCode(countryCallingCode)
                .setCardNumber(cardNumber)
                .build(),
        ).let { unlinkResponse ->
            PrimerUnlinkCardMetadata(
                requireNotNull(unlinkResponse.unlinkToken),
                requireNotNull(unlinkResponse.cardNumber),
            )
        }
    } catch (e: TransitException) {
        throw NolPaySdkException(e.code, e.message)
    }

    @WorkerThread
    @Throws(NolPaySdkException::class)
    fun unlinkPaymentCard(
        cardNumber: String,
        otpCode: String,
        unlinkToken: String,
    ) = try {
        Transit.getPaymentInstance().unlinkPaymentCard(
            TransitUnlinkPaymentCardRequest.Builder()
                .setCardNumber(cardNumber)
                .setOTPCode(otpCode)
                .setUnlinkPaymentCardToken(unlinkToken)
                .build(),
        )
    } catch (e: TransitException) {
        throw NolPaySdkException(e.code, e.message)
    }
    // endregion unlink payment card

    // region retrieve linked cards
    @Throws(NolPaySdkException::class)
    @WorkerThread
    fun getLinkedPaymentCards(
        mobileNumber: String,
        countryCallingCode: String,
    ) = try {
        Transit.getPaymentInstance().getPaymentCardList(
            TransitPaymentCardListRequest.Builder()
                .setMobile(mobileNumber)
                .setRegionCode(countryCallingCode)
                .build(),
        ).cardList
            .orEmpty()
            .map { transitPaymentCard -> PrimerNolPaymentCard(transitPaymentCard.cardNumber) }
    } catch (e: TransitException) {
        throw NolPaySdkException(e.code, e.message)
    }
    // endregion retrieve linked cards

    // region create payment
    @WorkerThread
    @Throws(NolPaySdkException::class)
    fun createPayment(
        tag: Tag,
        transactionNumber: String,
    ) = try {
        Transit.getPaymentInstance().requestPayment(
            TransitPayRequest.Builder().setTransactionNo(transactionNumber).setTag(tag).build(),
        )
    } catch (e: TransitException) {
        throw NolPaySdkException(e.code, e.message)
    }
    // endregion create payment
}
