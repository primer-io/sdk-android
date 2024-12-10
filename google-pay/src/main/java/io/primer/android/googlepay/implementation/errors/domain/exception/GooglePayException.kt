package io.primer.android.googlepay.implementation.errors.domain.exception

import com.google.android.gms.common.api.Status

internal class GooglePayException(val status: Status) : Exception()
