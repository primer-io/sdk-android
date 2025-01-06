package io.primer.android.nolpay.api.manager.nfc.component

import android.app.Activity
import android.content.Intent
import android.nfc.Tag
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessComponent
import io.primer.nolpay.api.PrimerNolPayNfcUtils
import io.primer.nolpay.api.models.NfcStatus

/**
 * The [NolPayNfcComponent] class provides methods for managing NFC (Near Field Communication)
 * functionality within the Primer SDK.
 *
 * This component allows you to interact with NFC features such as enabling/disabling
 * foreground dispatch, retrieving NFC tag information, and checking the NFC status.
 */
class NolPayNfcComponent private constructor() : PrimerHeadlessComponent {
    /**
     * Disable foreground dispatch for NFC events in the given activity.
     *
     * @param activity The activity in which NFC foreground dispatch should be disabled.
     */
    fun disableForegroundDispatch(activity: Activity) {
        PrimerNolPayNfcUtils.disableForegroundDispatch(activity)
    }

    /**
     * Enable foreground dispatch for NFC events in the given activity with a specific request code.
     *
     * @param activity The activity in which NFC foreground dispatch should be enabled.
     * @param requestCode The request code to associate with NFC events.
     */
    fun enableForegroundDispatch(
        activity: Activity,
        requestCode: Int,
    ) {
        PrimerNolPayNfcUtils.enableForegroundDispatch(activity, requestCode)
    }

    /**
     * Retrieve the available NFC tag from an intent.
     *
     * @param intent The intent containing NFC tag information.
     * @return The NFC tag extracted from the intent, or null if no tag is available.
     */
    fun getAvailableTag(intent: Intent?): Tag? {
        return PrimerNolPayNfcUtils.getAvailableTag(intent)
    }

    /**
     * Get the current NFC status.
     *
     * @return The current NFC status.
     */
    fun getNfcStatus(): NfcStatus = PrimerNolPayNfcUtils.getNfcStatus()

    companion object {
        fun provideInstance() = NolPayNfcComponent()
    }
}
