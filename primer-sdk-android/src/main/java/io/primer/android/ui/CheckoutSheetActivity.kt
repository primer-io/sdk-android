package io.primer.android.ui

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.primer.android.logging.Logger
import io.primer.android.model.Model
import io.primer.android.model.json
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.PrimerViewModelFactory
import kotlinx.serialization.serializer

class CheckoutSheetActivity : AppCompatActivity() {
  private val log = Logger("checkout-activity")
  private lateinit var model: Model
  private lateinit var viewModel: PrimerViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Unmarshal the configuration from the intent
    initializeModel()

    // Initialize the view model
    initializeViewModel()
    attachViewModelListeners()

    // Open the bottom sheet
    openSheet()
  }

  private inline fun <reified T> unmarshal(name: String): T {
    val serialized = intent.getStringExtra(name)
    return json.decodeFromString<T>(serializer(), serialized!!)
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

    viewModel = provider.get(PrimerViewModel::class.java)

    viewModel.initialize()
  }

  private fun attachViewModelListeners() {
    viewModel.selectedPaymentMethod.observe(this, {
      log("Selected payment method changed: ${it?.identifier ?: "none"}")
    })

    viewModel.sheetDismissed.observe(this, { dismissed ->
      log("Sheet dismissed changed: $dismissed")

      if (dismissed) {
        finish()
      }
    })
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
