package io.primer.checkout.orchestrator.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class StateProcessorManifestTest {

    @Test
    fun `deserialize should parse full manifest`() {
        val json = buildManifestJson()

        val result = StateProcessorManifest.deserializer.deserialize(json)

        assertEquals("1.2.3", result.stateProcessor.version)
        assertEquals("https://cdn.example.com/sp.js", result.stateProcessor.umd.url)
        assertEquals("sp-sha256", result.stateProcessor.umd.sha256)
        assertEquals("2.0.0", result.cel.version)
        assertEquals("https://cdn.example.com/cel.js", result.cel.noModules.js.url)
        assertEquals("cel-js-sha256", result.cel.noModules.js.sha256)
        assertEquals("https://cdn.example.com/cel.wasm", result.cel.noModules.wasm.url)
        assertEquals("cel-wasm-sha256", result.cel.noModules.wasm.sha256)
        assertEquals("https://cdn.example.com/cel.wasm.gz", result.cel.noModules.wasm.gz)
    }

    @Test
    fun `parseStateProcessor should parse state processor info`() {
        val json = JSONObject().apply {
            put("version", "3.0.0")
            put(
                "umd",
                JSONObject().apply {
                    put("url", "https://cdn.example.com/bundle.js")
                    put("sha256", "bundle-hash")
                },
            )
        }

        val result = StateProcessorManifest.parseStateProcessor(json)

        assertEquals("3.0.0", result.version)
        assertEquals("https://cdn.example.com/bundle.js", result.umd.url)
        assertEquals("bundle-hash", result.umd.sha256)
    }

    @Test
    fun `parseCel should parse cel info with noModules target`() {
        val json = JSONObject().apply {
            put("version", "4.0.0")
            put(
                "noModules",
                JSONObject().apply {
                    put(
                        "js",
                        JSONObject().apply {
                            put("url", "https://cdn.example.com/cel-nomod.js")
                            put("sha256", "js-hash")
                        },
                    )
                    put(
                        "wasm",
                        JSONObject().apply {
                            put("url", "https://cdn.example.com/cel-nomod.wasm")
                            put("sha256", "wasm-hash")
                            put("gz", "https://cdn.example.com/cel-nomod.wasm.gz")
                        },
                    )
                },
            )
        }

        val result = StateProcessorManifest.parseCel(json)

        assertEquals("4.0.0", result.version)
        assertEquals("https://cdn.example.com/cel-nomod.js", result.noModules.js.url)
        assertEquals("js-hash", result.noModules.js.sha256)
        assertEquals("https://cdn.example.com/cel-nomod.wasm", result.noModules.wasm.url)
        assertEquals("wasm-hash", result.noModules.wasm.sha256)
        assertEquals("https://cdn.example.com/cel-nomod.wasm.gz", result.noModules.wasm.gz)
    }

    @Test
    fun `deserialize should throw when stateProcessor is missing`() {
        val json = JSONObject().apply {
            put("cel", buildCelJson())
        }

        assertThrows(Exception::class.java) {
            StateProcessorManifest.deserializer.deserialize(json)
        }
    }

    @Test
    fun `deserialize should throw when cel is missing`() {
        val json = JSONObject().apply {
            put("stateProcessor", buildStateProcessorJson())
        }

        assertThrows(Exception::class.java) {
            StateProcessorManifest.deserializer.deserialize(json)
        }
    }

    @Test
    fun `deserialize should throw when stateProcessor version is missing`() {
        val json = JSONObject().apply {
            put(
                "stateProcessor",
                JSONObject().apply {
                    put(
                        "umd",
                        JSONObject().apply {
                            put("url", "https://cdn.example.com/sp.js")
                            put("sha256", "hash")
                        },
                    )
                },
            )
            put("cel", buildCelJson())
        }

        assertThrows(Exception::class.java) {
            StateProcessorManifest.deserializer.deserialize(json)
        }
    }

    @Test
    fun `deserialize should throw when wasm gz field is missing`() {
        val json = JSONObject().apply {
            put("stateProcessor", buildStateProcessorJson())
            put(
                "cel",
                JSONObject().apply {
                    put("version", "1.0.0")
                    put(
                        "noModules",
                        JSONObject().apply {
                            put(
                                "js",
                                JSONObject().apply {
                                    put("url", "https://cdn.example.com/cel.js")
                                    put("sha256", "hash")
                                },
                            )
                            put(
                                "wasm",
                                JSONObject().apply {
                                    put("url", "https://cdn.example.com/cel.wasm")
                                    put("sha256", "hash")
                                },
                            )
                        },
                    )
                },
            )
        }

        assertThrows(Exception::class.java) {
            StateProcessorManifest.deserializer.deserialize(json)
        }
    }

    private fun buildManifestJson() = JSONObject().apply {
        put("stateProcessor", buildStateProcessorJson())
        put("cel", buildCelJson())
    }

    private fun buildStateProcessorJson() = JSONObject().apply {
        put("version", "1.2.3")
        put(
            "umd",
            JSONObject().apply {
                put("url", "https://cdn.example.com/sp.js")
                put("sha256", "sp-sha256")
            },
        )
    }

    private fun buildCelJson() = JSONObject().apply {
        put("version", "2.0.0")
        put(
            "noModules",
            JSONObject().apply {
                put(
                    "js",
                    JSONObject().apply {
                        put("url", "https://cdn.example.com/cel.js")
                        put("sha256", "cel-js-sha256")
                    },
                )
                put(
                    "wasm",
                    JSONObject().apply {
                        put("url", "https://cdn.example.com/cel.wasm")
                        put("sha256", "cel-wasm-sha256")
                        put("gz", "https://cdn.example.com/cel.wasm.gz")
                    },
                )
            },
        )
    }
}
