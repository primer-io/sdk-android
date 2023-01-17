package io.primer.android.domain.exception

import com.google.android.gms.common.api.Status

internal class GooglePayException(val status: Status) : Exception()
