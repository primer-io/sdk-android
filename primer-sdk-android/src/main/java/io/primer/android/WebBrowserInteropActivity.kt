package io.primer.android

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

internal class WebBrowserInteropActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("URL", ">>> WebBrowserInteropActivity")

        Toast.makeText(
            this,
            "hello",
            Toast.LENGTH_SHORT
        ).show()

        WebviewInteropRegister.handleResult(intent.data)

        finish()
    }
}
