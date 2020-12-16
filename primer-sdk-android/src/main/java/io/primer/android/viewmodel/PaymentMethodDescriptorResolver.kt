package io.primer.android.viewmodel

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.PaymentMethodDescriptor

internal class PaymentMethodDescriptorResolver(
  viewModel: PrimerViewModel,
  private val configured: List<PaymentMethod>,
  private val remote: List<PaymentMethodRemoteConfig>
) {

  private val factory = PaymentMethodDescriptor.Factory(viewModel)

  fun resolve(): List<PaymentMethodDescriptor> {
    val list = ArrayList<PaymentMethodDescriptor>()

    remote.forEach { pm ->
      val config = configured.find { it.identifier == pm.type }

      if (config != null) {
        val descriptor = factory.create(pm, config)
        if (descriptor != null) {
          list.add(descriptor)
        }
      }
    }

    return list
  }
}