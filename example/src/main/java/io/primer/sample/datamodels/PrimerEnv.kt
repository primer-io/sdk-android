package io.primer.sample.datamodels


enum class PrimerEnv(val environment: String) {
    Production("production"),
    Sandbox("sandbox"),
    Staging("staging"),
    Dev("dev"),
    SandboxE2ETest("sandbox_e2e_tests"),
}

fun String.type(): PrimerEnv = PrimerEnv.entries.first { it.environment == this }
