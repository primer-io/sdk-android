package io.primer.android

import android.net.Uri
import io.primer.android.logging.Logger
import io.primer.android.payment.WebBrowserIntentBehaviour
import org.koin.core.component.KoinApiExtension
import java.util.*
import kotlin.collections.HashMap

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
        private val callbacks: MutableMap<String, Callback> = HashMap()
        private var packageName: String = ""

        fun init(name: String) {
            log.info("Initializing: $name")
            packageName = name
        }

        fun register(behaviour: WebBrowserIntentBehaviour): Callback {
            val id = UUID.randomUUID().toString()
            val callback = Callback(
                id = id,
                cancelUrl = "$packageName.primer://$id/cancel",
                successUrl = "$packageName.primer://$id/success",
                behaviour = behaviour
            )

            callbacks[id] = callback

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
