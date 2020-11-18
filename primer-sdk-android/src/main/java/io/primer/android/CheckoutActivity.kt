package io.primer.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.primer.android.api.APIClient
import io.primer.android.api.IAPIClient
import io.primer.android.logging.Logger
import io.primer.android.session.ClientSession
import io.primer.android.session.ClientToken
import io.primer.android.session.SessionFactory
import io.primer.android.ui.main.CheckoutFragment

class CheckoutActivity : AppCompatActivity() {
  private val log = Logger("activity")

  private var initialized = false

  private lateinit var token: ClientToken
  private lateinit var api: IAPIClient
  private lateinit var sessionFactory: SessionFactory

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.checkout_activity)

    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .replace(R.id.container, CheckoutFragment.newInstance())
        .commitNow()
    }

    this.init()
  }

  private fun init() {
    if (initialized) {
      return
    }

    val clientToken = intent.getStringExtra("token") as String

    token = ClientToken(clientToken)
    api = APIClient(this, token)
    sessionFactory = SessionFactory(api, token)

    sessionFactory.create(this::onSession)
  }

  private fun onSession(session: ClientSession) {
    log("Created Session!")
    log(session.coreUrl)
    log(session.pciUrl)
    log(session.paymentMethods.toString())
  }
}