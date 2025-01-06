package com.example.customtabs

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection

fun Activity.launchCustomTab(url: String) {
    val uri = Uri.parse(url)
    val packageName = CustomTabsClient.getPackageName(this, null)

    if (packageName == null) {
        openInBrowser(this, uri)
        return
    }

    CustomTabsClient.bindCustomTabsService(
        this,
        packageName,
        object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                name: ComponentName,
                client: CustomTabsClient,
            ) {
                val customTabsIntent =
                    CustomTabsIntent.Builder(client.newSession(CustomTabsCallback()))
                        .setUrlBarHidingEnabled(true)
                        .setShowTitle(true)
                        .build()
                customTabsIntent.launchUrl(this@launchCustomTab, uri)
            }

            override fun onServiceDisconnected(name: ComponentName?) = Unit
        },
    )
}

private fun openInBrowser(
    activity: Activity,
    uri: Uri,
) {
    val intent = Intent(Intent.ACTION_VIEW, uri)
    activity.startActivity(intent)
}
