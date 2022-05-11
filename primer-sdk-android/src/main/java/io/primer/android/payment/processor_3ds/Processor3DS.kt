package io.primer.android.payment.processor_3ds

internal data class Processor3DS(
    val redirectUrl: String,
    val statusUrl: String,
    val title: String = "3D Secure"
)
