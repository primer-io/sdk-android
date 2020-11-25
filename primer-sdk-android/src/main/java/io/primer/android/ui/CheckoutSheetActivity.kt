package io.primer.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import io.primer.android.CheckoutConfig
import io.primer.android.api.APIClient
import io.primer.android.logging.Logger
import io.primer.android.session.ClientToken
import io.primer.android.session.SessionFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class CheckoutSheetActivity : AppCompatActivity(),
  CheckoutSheetFragmentListener {
  private val log = Logger("checkout-activity")
  private val format = Json { ignoreUnknownKeys = true }
  private lateinit var config: CheckoutConfig

  override fun onDismissed() {
    log("DISMISED")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize the view model
    val viewModel = ViewModelProvider(this).get(PrimerViewModel::class.java)
//    val viewModel = defaultViewModelProviderFactory.create(PrimerViewModel::class.java)
    val serialized = intent.getStringExtra("config")

    config = format.decodeFromString(serializer(), serialized!!)

    viewModel.initialize(config)

    supportFragmentManager.let {
      log("Showing checkout sheet")

      val fragment = CheckoutSheetFragment.newInstance(Bundle()).apply {
        show(it, tag)
      }

      fragment.register(this)
    }
  }
}
