package io.primer.android.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import io.primer.android.CheckoutConfig
import io.primer.android.PaymentMethod
import io.primer.android.api.APIClient
import io.primer.android.logging.Logger
import io.primer.android.session.ClientToken
import io.primer.android.session.SessionFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class CheckoutSheetActivity : AppCompatActivity(), CheckoutSheetFragment.CheckoutSheetListener {
  private val log = Logger("checkout-activity")
  private val format = Json { ignoreUnknownKeys = true }
  private lateinit var config: CheckoutConfig
  private lateinit var paymentMethods: List<PaymentMethod>;

  override fun onPaymentMethodSelected(type: String) {
    log("Payment method selected! - $type")
  }

  override fun onSheetDismissed() {
    log("Sheet dismissed!")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize the view model
    val viewModel = ViewModelProvider(this).get(PrimerViewModel::class.java)

    config = unmarshal("config")
    paymentMethods = unmarshal("paymentMethods")

    log("Loaded intent data:")
    log(paymentMethods.toString())
    log(config.toString())

    viewModel.initialize(config)

    supportFragmentManager.let {
      log("Showing checkout sheet")

      CheckoutSheetFragment.newInstance(Bundle()).apply {
        show(it, tag)
      }
    }
  }

  private inline fun <reified T> unmarshal(name: String): T {
    val serialized = intent.getStringExtra(name)
    val decoded = format.decodeFromString<T>(serializer(), serialized!!)
    return decoded
  }
}
