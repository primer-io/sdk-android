package com.example.customtabs

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.core.net.toUri

fun Context.canLaunchCustomTabs(): Boolean {
    return CustomTabsClient.getPackageName(this, null) != null
}

fun Activity.launchCustomTab(launcher: ActivityResultLauncher<Intent>, url: String) {
    val uri = url.toUri()
    val packageName = CustomTabsClient.getPackageName(this, null)
    CustomTabsClient.bindCustomTabsService(
        this,
        requireNotNull(packageName),
        object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                name: ComponentName,
                client: CustomTabsClient,
            ) {
                val customTabsIntentBuilder =
                    CustomTabsIntent.Builder(client.newSession(CustomTabsCallback()))
                        .setUrlBarHidingEnabled(true)
                        .setShowTitle(true)
                        .build()
                val customTabsIntent: Intent = customTabsIntentBuilder.intent
                customTabsIntent.setData(uri)
                launcher.launch(customTabsIntent)
            }

            override fun onServiceDisconnected(name: ComponentName?) = Unit
        },
    )
}

fun Activity.openInBrowser(
    url: String,
) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    startActivity(intent)
}
