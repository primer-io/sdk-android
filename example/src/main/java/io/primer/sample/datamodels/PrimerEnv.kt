package io.primer.sample.datamodels


enum class PrimerEnv(val environment: String) {
    Production("production"),
    Sandbox("sandbox"),
    Staging("staging"),
    Dev("dev"),
}

fun String.type(): PrimerEnv = PrimerEnv.values().first { it.environment == this }
