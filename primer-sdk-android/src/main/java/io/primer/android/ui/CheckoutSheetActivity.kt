package io.primer.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.primer.android.logging.Logger


class CheckoutSheetActivity : AppCompatActivity(),
  CheckoutSheetFragmentListener {
  private val log = Logger("checkout-activity")

  override fun onDismissed() {
    log("DISMISED")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val token = intent.getStringExtra("token")

    supportFragmentManager.let {
      val fragment = CheckoutSheetFragment.newInstance(Bundle()).apply {
        show(it, tag)
      }

      fragment.register(this)

      // TODO: initialize view model
    }
  }
}
