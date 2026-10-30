package io.primer.ipay88.api.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class IPay88RedirectActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Exported, so never touch the extras: reading any of them deserialises the whole bundle on Android <= 12,
        // and Activity.startActivity() does exactly that on Android 9-12.
        applicationContext.startActivity(
            Intent(this, NativeIPay88Activity::class.java)
                .setData(intent.data)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        finish()
    }
}
