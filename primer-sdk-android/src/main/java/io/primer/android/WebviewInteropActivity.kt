package io.primer.android

import android.app.Activity
import android.os.Bundle
import io.primer.android.logging.Logger

internal class WebviewInteropActivity : Activity() {
  private val log = Logger("WebviewInteropActivity")

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    log("Intent data: ${intent.data?.toString() ?: ""}")

    WebviewInteropRegister.handleResult(intent.data)

    finish()
  }
}