package io.primer.android

import android.net.Uri
import io.primer.android.logging.Logger
import io.primer.android.payment.WebBrowserIntentBehaviour
import org.koin.core.component.KoinApiExtension
import java.util.UUID

internal class WebviewInteropRegister {

    @KoinApiExtension
    data class Callback(
        val id: String,
        val cancelUrl: String,
        val successUrl: String,
        val behaviour: WebBrowserIntentBehaviour,
        var result: Uri? = null,
    )

    @KoinApiExtension
    companion object {

        private val log = Logger("WebviewInteropActivity")
        private val callbacks: MutableMap<String, Callback> = mutableMapOf()
        private lateinit var scheme: String
        private lateinit var host: String

        fun init(scheme: String, host: String) {
            log.info("Initializing: $scheme")
            this.scheme = scheme
            this.host = host
        }

        fun register(behaviour: WebBrowserIntentBehaviour): Callback {
            val callback = Callback(
                id = host,
                cancelUrl = "$scheme://$host/cancel",
                successUrl = "$scheme://$host/success",
                behaviour = behaviour
            )

            callbacks[host] = callback

            return callback
        }

        fun handleResult(uri: Uri?) {
            val callback = callbacks[uri?.host]

            if (callback != null) {
                callback.result = uri
            }
        }

        fun invokeAll() {
            callbacks.forEach {
                it.value.result.let { uri ->
                    when (uri?.pathSegments?.last()) {
                        "success" -> it.value.behaviour.onSuccess(uri)
                        "cancel" -> it.value.behaviour.onCancel(uri)
                        null -> it.value.behaviour.onCancel()
                    }
                }

                if (it.value.result == null) {
                    it.value.behaviour.onCancel()
                }
            }

            callbacks.clear()
        }
    }
}
