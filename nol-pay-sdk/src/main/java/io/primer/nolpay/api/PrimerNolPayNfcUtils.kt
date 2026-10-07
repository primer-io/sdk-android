package io.primer.nolpay.api

import android.app.Activity
import android.content.Intent
import com.snowballtech.transit.rta.Transit
import com.snowballtech.transit.rta.TransitNfcStatus
import io.primer.nolpay.api.models.NfcStatus

object PrimerNolPayNfcUtils {

    // region NFC toggling
    fun enableForegroundDispatch(
        activity: Activity,
        requestCode: Int
    ) = Transit.getPaymentInstance().enableForegroundDispatch(activity, requestCode)

    fun disableForegroundDispatch(
        activity: Activity
    ) = Transit.getPaymentInstance().disableForegroundDispatch(activity)
    // endregion NFC toggling

    // region Tag conversion
    fun getAvailableTag(intent: Intent?) = Transit.getPaymentInstance().getAvailableTag(intent)
    // endregion Tag conversion

    // region NFC status
    fun getNfcStatus() = Transit.getTransitInstance().getNfcStatus().let { transitNfcStatus ->
        when (transitNfcStatus) {
            TransitNfcStatus.NFC_ENABLE -> NfcStatus.NFC_ENABLED
            TransitNfcStatus.NFC_DISABLE -> NfcStatus.NFC_DISABLED
            TransitNfcStatus.NFC_UNSUPPORTED -> NfcStatus.NFC_UNSUPPORTED
        }
    }
    // endregion NFC status
}
