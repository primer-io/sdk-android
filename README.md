
<h1 align="center"><img src="./assets/primer-logo.png?raw=true" height="24px"> Primer Android SDK</h1>

<div align="center">
  <h3 align="center">

[Primer's](https://primer.io) Official Universal Checkout Android SDK

  </h3>
</div>

<br/>

<div align="center"><img src="./assets/checkout-banner.gif?raw=true"  width="50%"/></div>

<br/>

<p align="center">
  <a href="https://android-arsenal.com/api?level=24"><img alt="API" src="https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://search.maven.org/search?q=g:%22io.primer%22%20AND%20a:%22android%22"><img src="https://img.shields.io/maven-central/v/io.primer/android"/></a>
  <a href="LICENSE.md"><img src="https://img.shields.io/github/license/primer-io/sdk-android"/></a>
</p>

<br/>

# 💪 Features of the Android SDK

<p>💳 &nbsp; Create great payment experiences with our highly customizable Universal Checkout</p>
<p>🧩 &nbsp; Connect and configure any new payment method without a single line of code</p>
<p>✅ &nbsp; Dynamically handle 3DS 2.0 across processors and be SCA ready</p>
<p>♻️ &nbsp; Store payment methods for recurring and repeat payments</p>
<p>🔒 &nbsp; Always PCI compliant without redirecting customers</p>


# 📚 Documentation

Consider looking at the following resources:

- [Documentation](https://primer.io/docs)
- [Client session creation](https://primer.io/docs/checkout/client-session#create-a-client-session)
- [API reference](https://primer.io/docs/api-reference/get-started/overview)
- [Changelogs](https://primer.io/docs/changelog/sdk-changelog/android)
- [Detailed Android Documentation](https://primer.io/docs/checkout/drop-in/overview#android)


# 💡 Support

For any support or integration related queries, feel free to [Contact Us](mailto:https://support@primer.io).


## 🚀 Quick start

Take a look at our [Quick Start Guide](https://primer.io/docs/checkout/drop-in/overview#android) for accepting your first payment with Universal Checkout.

<br/>

# 🧱 Installation

## Prerequisites
- android-studio


Add the following to your `app/build.gradle` file:

Using [bill of materials (BOM)](https://docs.gradle.org/6.2/userguide/platforms.html#sub:bom_import)
available to help you keep Primer artifacts up to date and be sure about version compatibility.

```kotlin{:copy}
repositories {
  mavenCentral()
}
dependencies {
   // define a BOM and its version
   implementation(platform("io.primer:bom:latest.version"))

   // define any required Primer artifacts without version
   implementation("io.primer:android")
   implementation("io.primer:3ds-android") // in case you are using 3DS SDK
}
```

For more details about SDK versions, please see our [changelog](https://primer.io/docs/changelogs/android-sdk).

It is highly recommended adding following settings to your `app/build.gradle` file:

```kotlin{:copy}
android {
    kotlinOptions {
        freeCompilerArgs += '-Xjvm-default=all'
    }
}
```

# 👩‍💻 Usage

## 📋 Prerequisites

- 🔑 Generate a client token by [creating a client session](https://primer.io/docs/checkout/client-session#create-a-client-session) in your backend.
- 🎉 _That's it!_

## 🔍 &nbsp;Initializing the SDK

Prepare the PrimerCheckoutListener that will handle the callbacks that happen during the lifecycle.
Import the Primer SDK and set its listener as shown in the following example:
```kotlin{:copy}
class CheckoutActivity : AppCompatActivity() {

    private val listener = object : PrimerCheckoutListener {

        override fun onCheckoutCompleted(checkoutData: PrimerCheckoutData) {
            // Primer checkout completed with checkoutData
            // show an order confirmation screen, fulfil the order...
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configureCheckout()
    }

    private fun configureCheckout() {
        // Initialize the SDK with the default settings.
        Primer.instance.configure(listener = listener)
    }
}
```


**Note:** Check the [SDK API Reference](https://primer.io/docs/sdk/android/2.x.x) for more options to customize your SDK.


## 🔍 &nbsp;Rendering the checkout

Now you can use the client token that you generated on your backend.
Call the `showUniversalCheckout` function (as shown below) to present Universal Checkout.

```kotlin{:copy}
class CheckoutActivity : AppCompatActivity() {
 
    // other code goes here
 
    private fun setupObservers() {
        viewModel.clientToken.observe(this) { clientToken ->
            showUniversalCheckout(clientToken)
        }
    }
 
    private fun showUniversalCheckout(clientToken: String) {
        Primer.instance.showUniversalCheckout(this, clientToken)
    }
}
```
You should now be able to see Universal Checkout! The user can now interact with Universal Checkout, and the SDK will create the payment.
The payment’s data will be returned on `onCheckoutCompleted(checkoutData)`.

**Note:** There are more options which can be passed to Universal Checkout. Please refer to the section below for more information.

# Running

To run the example, simply press the play button from Android Studio to launch on a virtual device.

# ProGuard

If you use ProGuard or R8, you do not need to manually add any rules, as they are automatically embedded in the artifacts.
Please let us know if you find any issues.

# Contributing guidelines:

[Contributing doc](CONTRIBUTING.md)

# License

This repository is available under the [BSD-3](LICENSE.md).
