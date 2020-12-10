package io.primer.android.model

import io.primer.android.model.dto.ClientSession

internal class APIEndpoint {

  enum class Target { CORE, PCI }

  companion object {
    const val PAYMENT_INSTRUMENTS = "/payment-instruments"
    const val DELETE_TOKEN = "/payment-instruments/{id}/vault"

    fun get(
      session: ClientSession,
      target: Target,
      pathname: String,
      params: Map<String, String>? = null
    ) : String {
      val baseUrl = if (target == Target.PCI) session.pciUrl else session.coreUrl
      var url = "$baseUrl$pathname"

      params?.entries?.forEach {
        url = url.replace("{${it.key}}", it.value)
      }

      return url
    }
  }
}