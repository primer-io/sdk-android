
<h1 align="center"><img src="./assets/primer-logo.png?raw=true" height="24px"> Primer iOS SDK</h1>

<div align="center">
  <h3 align="center">

[Primer's](https://primer.io) Official Universal Checkout iOS SDK

  </h3>
</div>

<br/>

<div align="center"><img src="./assets/checkout-banner.gif?raw=true"  width="50%"/></div>

<br/>

[![Maven Central](https://img.shields.io/maven-central/v/io.primer/android.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.primer%22%20AND%20a:%22android%22) [![CircleCI](https://circleci.com/gh/primer-io/primer-sdk-android.svg?style=svg&circle-token=fdbf8380fcad091297915de921787f7297946cd3)](https://app.circleci.com/pipelines/github/primer-io/primer-sdk-android)

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
- [Client session creation](https://primer.io/docs/accept-payments/manage-client-sessions/#create-a-client-session)
- [API reference](https://apiref.primer.io/docs)
- [Changelogs](https://primer.io/docs/changelog/sdk-changelog/android)
- [Detailed Android Documentation](https://www.notion.so/primerio/Android-SDK-3eb4caa37d2e4832944fb2d97cd9cd92)


# 💡 Support

For any support or integration related queries, feel free to [Contact Us](mailto:https://support@primer.io).


## 🚀 Quick start

Take a look at our [Quick Start Guide](https://primer.io/docs/get-started/android) for accepting your first payment with Universal Checkout.

<br/>

# 🧱 Installation

## Prerequisites
- android-studio
- `nodejs` >= 12 + `yarn` (for local server)


Add the following to your `app/build.gradle` file:

```kotlin{:copy}
repositories {
  mavenCentral()
}

dependencies {
  implementation 'io.primer:android:latest.version'
}
```
For more details about SDK versions, please see our [changelog](https://www.notion.so/primerio/Android-SDK-8b4bd28444eb4af283678c9f2b5f46fe).

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

- 🔑 Generate a client token by [creating a client session](https://primer.io/docs/accept-payments/manage-client-sessions) in your backend.
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


**Note:** Check the [SDK API Reference](https://www.notion.so/primerio/API-Reference-f62b4be8f24642989e63c25a8fb5f0ba_) for more options to customize your SDK.


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

## Running Virtual device with your local server

Android virtual devices proxy `10.0.2.2` to your machine's localhost. There's a proxy script in the `/proxy` to help adapt the URLs returned from the server to account for this:

```bash
# Copy the .env.example
cp .env.example .env

# Add your API key
nano .env

# set up and run the proxy
cd proxy
yarn install
node server.js

# All the requests from the virtual device should now go to your running server
```

## Debugging
Logcat has a habit of misbehaving, so you might need to attach the debugger and set breakpoints to find out what's really going on.


# Contributing guidelines:

[Contributing doc](Contributing.md)
