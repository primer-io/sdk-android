package io.primer.android

import android.app.Activity
import android.os.Bundle
import io.primer.android.logging.Logger

class CheckoutSheetActivity : Activity() {
  private val log = Logger("checkout-activity")
  private var primer: Primer? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_checkout_sheet)

    val token = intent.getStringExtra("token")

    if (token != null) {
      primer = Primer(this, token)
    }
  }
}