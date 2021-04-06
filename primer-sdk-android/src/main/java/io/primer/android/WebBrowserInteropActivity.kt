package io.primer.android

import android.app.Activity
import android.os.Bundle
import io.primer.android.logging.Logger

internal class WebBrowserInteropActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WebviewInteropRegister.handleResult(intent.data)

        finish()
    }
}
