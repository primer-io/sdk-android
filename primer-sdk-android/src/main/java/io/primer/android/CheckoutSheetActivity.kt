package io.primer.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.primer.android.logging.Logger
import io.primer.android.ui.main.CheckoutSheetFragment
import io.primer.android.ui.main.CheckoutSheetFragmentListener
import io.primer.android.ui.main.CheckoutSheetFragmentPublisher

class CheckoutSheetActivity : AppCompatActivity(), CheckoutSheetFragmentListener {
  private val log = Logger("checkout-activity")

  override fun onDismissed() {
    log("DISMISED")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_checkout_sheet)

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