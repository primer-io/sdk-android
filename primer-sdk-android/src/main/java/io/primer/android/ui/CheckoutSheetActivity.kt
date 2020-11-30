package io.primer.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.primer.android.logging.Logger
import io.primer.android.model.Model
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class CheckoutSheetActivity : AppCompatActivity(), CheckoutSheetFragment.CheckoutSheetListener {
  private val log = Logger("checkout-activity")
  private val format = Json { ignoreUnknownKeys = true }
  private lateinit var model: Model

  override fun onPaymentMethodSelected(type: String) {
    log("Payment method selected! - $type")
  }

  override fun onSheetDismissed() {
    log("Sheet dismissed!")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Unmarshal the configuration from the intent
    initializeModel()

    // Initialize the view model
    initializeViewModel()

    // Open the bottom sheet
    openSheet()
  }

  private inline fun <reified T> unmarshal(name: String): T {
    val serialized = intent.getStringExtra(name)
    val decoded = format.decodeFromString<T>(serializer(), serialized!!)
    return decoded
  }

  private fun initializeModel() {
    model = Model(
      unmarshal("config"),
      unmarshal("paymentMethods")
    )
  }

  private fun initializeViewModel() {
    val factory = PrimerViewModelFactory(model)
    val provider = ViewModelProvider(this, factory)
    val viewModel = provider.get(PrimerViewModel::class.java)

    viewModel.initialize()
  }

  private fun openSheet() {
    supportFragmentManager.let {
      log("Showing checkout sheet")

      CheckoutSheetFragment.newInstance(Bundle()).apply {
        show(it, tag)
      }
    }
  }
}
