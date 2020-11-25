package io.primer.android.session

import io.primer.android.api.IAPIClient
import io.primer.android.logging.Logger

class SessionFactory(api: IAPIClient, token: ClientToken) {
  private var log = Logger("session-factory")
  private var api = api
  private var token = token

  fun create(callback: ((ClientSession) -> Unit)) {
    log("Loading remote configuration")
    api.get(
      token.configurationURL,
      {
        log("Created session")
        callback(ClientSession.fromJSON(it.data))
      },
      {
        // TODO: Figure out what to do with the error here
        log("FAIL :( " + it.data.description)
      }
    )
  }
}