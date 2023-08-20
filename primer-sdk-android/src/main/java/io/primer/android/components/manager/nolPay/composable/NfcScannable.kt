package io.primer.android.components.manager.nolPay.composable

import android.app.Activity
import android.content.Intent
import android.nfc.Tag

internal interface NfcScannable {

    fun enableForegroundDispatch(activity: Activity, requestCode: Int)

    fun disableForegroundDispatch(activity: Activity)

    fun getAvailableTag(intent: Intent?): Tag?
}
