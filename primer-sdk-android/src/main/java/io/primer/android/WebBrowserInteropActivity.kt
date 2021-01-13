package io.primer.android

import android.app.Activity
import android.os.Bundle
import io.primer.android.logging.Logger

internal class WebBrowserInteropActivity : Activity() {
  private val log = Logger("WebBrowserInteropActivity")

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    WebviewInteropRegister.handleResult(intent.data)

    finish()
  }
}