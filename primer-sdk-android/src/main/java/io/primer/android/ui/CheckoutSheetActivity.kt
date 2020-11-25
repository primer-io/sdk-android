package io.primer.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.primer.android.CheckoutConfig
import io.primer.android.logging.Logger
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

    val serialized = intent.getStringExtra("config")

    config = format.decodeFromString(serializer(), serialized!!)

    supportFragmentManager.let {
      val fragment = CheckoutSheetFragment.newInstance(Bundle()).apply {
        show(it, tag)
      }

      fragment.register(this)

      // TODO: initialize view model with config
    }
  }
}
