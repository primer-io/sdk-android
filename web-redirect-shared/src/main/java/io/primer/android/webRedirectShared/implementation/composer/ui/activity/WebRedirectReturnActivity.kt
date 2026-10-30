package io.primer.android.webRedirectShared.implementation.composer.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class WebRedirectReturnActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Exported, so never touch the extras: reading any of them deserialises the whole bundle on Android <= 12,
        // and Activity.startActivity() does exactly that on Android 9-12.
        applicationContext.startActivity(
            Intent(this, WebRedirectActivity::class.java)
                .setData(intent.data)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        )
        finish()
    }
}
