package io.primer.android.ipay88.implementation.composer.ui.navigation.extension

import io.primer.android.ipay88.implementation.composer.ui.navigation.launcher.IPay88ActivityLauncherParams
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
        errorCode,
    )
