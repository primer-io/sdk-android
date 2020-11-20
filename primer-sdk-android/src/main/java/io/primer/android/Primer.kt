package io.primer.android

import android.content.Context
import io.primer.android.api.APIClient
import io.primer.android.logging.Logger
import io.primer.android.session.ClientSession
import io.primer.android.session.ClientToken
import io.primer.android.session.SessionFactory

class Primer(context: Context, token: String) {
  private val log = Logger("client")
  private val clientToken = ClientToken(token)
  private val api = APIClient(context, clientToken)
  private val sessionFactory = SessionFactory(api, clientToken)
  private var session: ClientSession? = null

  init {
    log("Initializing session")
    sessionFactory.create {
      log("Created session")
      log(it.coreUrl)
      log(it.pciUrl)
    }
  }
}