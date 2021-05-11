package io.primer.android

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

internal class WebBrowserInteropActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WebviewInteropRegister.handleResult(intent.data)

        finish()
    }
}
