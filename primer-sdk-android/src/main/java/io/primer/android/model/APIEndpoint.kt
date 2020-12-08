package io.primer.android.model

import io.primer.android.model.dto.ClientSession

internal class APIEndpoint {

  enum class Target { CORE, PCI }

  companion object {
    val PAYMENT_INSTRUMENTS = "/payment-instruments"

    fun get(session: ClientSession, target: Target, pathname: String) : String {
      val baseUrl = if (target == Target.PCI) session.pciUrl else session.coreUrl
      return "$baseUrl$pathname"
    }
  }
}