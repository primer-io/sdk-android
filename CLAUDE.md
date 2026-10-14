# CLAUDE.md

Guidance for AI coding agents (and new engineers) working in this repo. Kept lean on
purpose — anything the code, README, or CI already states does not belong here.

## What this is

Primer's official **Universal Checkout SDK for Android** — a highly modularised Kotlin SDK
(38 Gradle modules) that lets merchants accept payments across many payment methods with
3DS, vaulting, and PCI-compliant card capture. Published to Maven Central as `io.primer:android`.
Source of truth is GitLab (`primer-io/checkout/primer-sdk-android`); CI mirrors it to the
public GitHub repo `primer-io/sdk-android`.

This is the stable **2.x** line. Two integration surfaces share one payments core:

- **Drop-in** (`:drop-in`, entry `io.primer.android.Primer`) — complete prebuilt UI (Views/Fragments).
- **Headless** (`:headless-core`) — programmatic control with a custom UI; vaulting via
  `:headless-vault-manager`.

## Structure

38 modules following Clean Architecture (`data` / `domain` / `presentation`). Highlights:

```
arch-core/            # Base architecture, custom DI, networking, validation framework
payments-core/        # Core payment processing
checkout-orchestrator/  # Payment-flow orchestration & decision handling
errors-core/          # Result/Either error handling
logging/              # Structured logging
drop-in/              # Prebuilt drop-in UI (Views/Fragments)
headless-core/        # Headless SDK entry point
headless-vault-manager/  # Stored/vaulted payment methods
payment-methods-core/     # Interfaces every payment-method module implements
payment-methods-core-ui/  # Shared payment-method UI
threeds/ processor-3ds/   # 3DS 2.0 support
<name>-sdk/           # Third-party wrapper modules (klarna-sdk, stripe-sdk, ...): the vendor SDK plus a thin
                      # bridging layer, published at the SDK version under the historical coordinates
                      # (io.primer:klarna-android, io.primer:stripe-android, ...)
<payment-method>/     # One module per method: google-pay, paypal, klarna, stripe-ach, ipay88, ...
example/              # Bundled sample app (io.primer.sample) — dev/demo only, not shipped
bom/                  # BOM for cross-module version alignment
tooling/              # Shared Gradle scripts (android-common, code-analysis, coverage, publish)
```

## Setup & run

- Android Studio, **JDK 17**, Android SDK (compileSdk 36, minSdk 23).
- Copy `secrets.defaults.properties` → `secrets.properties` for local keys; never commit real keys.

```bash
./gradlew clean assembleDebug          # build everything (debug)
./gradlew :drop-in:assembleDebug        # build just the drop-in SDK
./gradlew publishToMavenLocal           # publish locally for a consuming app
```

To run it, launch the bundled **`:example`** app (`io.primer.sample`) from Android Studio on a
device/emulator — press Run.

## Tests

JUnit 5 + MockK. Unit tests live alongside each module under `src/test/`, mirroring the source
package. Shared fixtures are in `:arch-core` testFixtures; payment-flow tests use MockWebServer.

```bash
./gradlew test                              # all unit tests
./gradlew :drop-in:testDebugUnitTest         # a single module's tests
./gradlew test --tests "ClassName"           # single class
./gradlew connectedAndroidTest               # instrumentation tests (device/emulator + orchestrator)
./gradlew koverHtmlReport                    # aggregate coverage report
```

UI/E2E tests run on LambdaTest against the `:example` app, driven by the external Appium suite
(`primer-io/acceptance/mobile/mobile-appium-tests`) — see the `UI/E2E Tests` workflows (nightly
cron + manual `workflow_dispatch`). They are not a per-PR gate.

## Before you merge (acceptance gates)

CI (GitLab, on every MR) runs and **must pass**:

```bash
./gradlew clean lint                # Android Lint (per-module baselines)
./gradlew clean detekt              # Detekt static analysis (tooling/code-analysis/detekt.yml)
```

Retired on purpose in July 2026 (do not reintroduce without asking): the excluded-optional-dependencies
build, the local publish check and the AAR analyze job. When you touch a glue module or a `<name>-sdk`
wrapper module, verify manually instead: `./gradlew publishToMavenLocal`, then build and run Primer Studio
against Maven Local with and without the wrapper artifact.

## Conventions & guardrails

- **Kotlin official** style; coroutines + `Flow`/`StateFlow` throughout. No external DI framework —
  manual DI via `SdkComponent` / `DISdkComponent`.
- New payment methods are separate modules implementing `:payment-methods-core` interfaces;
  register in `:drop-in`/`:headless-core` and add to `:bom`.
- **Optional payment-method dependencies** (Klarna, iPay88, 3DS, Stripe, Nol Pay) must stay optional:
  glue modules depend on the `<name>-sdk` wrapper modules `compileOnly` and detect them at runtime
  through the `*SdkClassValidator` classes. The SDK has to compile and run when a merchant excludes them.
- Validation uses the composable `ValidationRule` / `ValidationRulesChain` framework in `:arch-core`.
- **PCI / security:** never log or persist card data or PII; network calls use certificate pinning;
  keep per-module ProGuard rules intact.

## Where to find more

- `README.md` — features, installation, quick start · `CONTRIBUTING.md` — PR/commit conventions.
- Public docs: <https://primer.io/docs> · Android changelog:
  <https://primer.io/docs/changelog/sdk-changelog/android>
- Branch model: **`master`** = stable **2.x** (this branch), published to Maven Central;
  **`v3`** = 3.0.0-beta CheckoutComponents (Jetpack Compose), where active development happens.
