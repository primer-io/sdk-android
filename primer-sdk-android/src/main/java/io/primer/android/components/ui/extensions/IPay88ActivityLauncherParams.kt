package io.primer.android.components.ui.extensions

import io.primer.android.components.ui.activity.IPay88ActivityLauncherParams
import io.primer.ipay88.api.ui.IPay88LauncherParams

internal fun IPay88ActivityLauncherParams.toIPay88LauncherParams() =
    IPay88LauncherParams(
        iPayPaymentId,
        iPayMethod,
        merchantCode,
        actionType,
        amount,
        referenceNumber,
        prodDesc,
        currencyCode,
        countryCode,
        customerName,
        customerEmail,
        remark,
        backendCallbackUrl,
        deeplinkUrl,
        errorCode
    )
