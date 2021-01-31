package io.primer.android

import android.content.Context
import android.content.Intent
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.logging.Logger
import io.primer.android.model.DeferredToken
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.CheckoutExitReason
import io.primer.android.model.json
import kotlinx.serialization.serializer
import org.koin.dsl.koinApplication

class UniversalCheckout private constructor(
  private val context: Context,
  authTokenProvider: ClientTokenProvider,
  private val theme: UniversalCheckoutTheme? = null,
) : EventBus.EventListener {
  private val log = Logger("primer")
  private val token = DeferredToken(authTokenProvider)
  private var paymentMethods: List<PaymentMethod> = ArrayList()

  private var listener: EventListener? = null
  private var subscription: EventBus.SubscriptionHandle? = null


  interface EventListener {
    fun onCheckoutEvent(e: CheckoutEvent)
  }

  internal enum class UXMode {
    CHECKOUT, ADD_PAYMENT_METHOD, STANDALONE_PAYMENT_METHOD,
  }

  private fun loadPaymentMethods(paymentMethods: List<PaymentMethod>) {
    this.paymentMethods = paymentMethods
  }

  private fun show(
    listener: EventListener,
    uxMode: UXMode? = null,
    amount: Int? = null,
    currency: String? = null,
  ) {
    subscription?.unregister()

    this.listener = listener
    this.subscription = EventBus.subscribe(this)

    WebviewInteropRegister.init(context.packageName)

    token.observe {
      val config = CheckoutConfig.create(
        clientToken = it,
        uxMode = uxMode ?: UXMode.CHECKOUT,
        amount = amount,
        currency = currency,
        theme = theme,
      )

      val intent = Intent(context, CheckoutSheetActivity::class.java)

      intent.putExtra("config", json.encodeToString(serializer(), config))
      intent.putExtra("paymentMethods", json.encodeToString(serializer(), paymentMethods))

      context.startActivity(intent)
    }
  }

  private fun destroy() {
    this.subscription?.unregister()
    this.subscription = null
  }

  override fun onEvent(e: CheckoutEvent) {
    if (e.public) {
      listener?.onCheckoutEvent(e)
    }
  }

  companion object {
    private var instance: UniversalCheckout? = null

    /**
     * Initializes the Primer SDK with the Application context and a client token Provider
     */
    fun initialize(context: Context, authTokenProvider: ClientTokenProvider, theme: UniversalCheckoutTheme? = null) {
      destroy()
      instance = UniversalCheckout(context, authTokenProvider, theme = theme)
    }

    fun showSavedPaymentMethods(listener: UniversalCheckout.EventListener) {
      return show(listener, UXMode.ADD_PAYMENT_METHOD)
    }

    fun showCheckout(listener: EventListener, amount: Int, currency: String) {
      return show(listener, UXMode.CHECKOUT, amount = amount, currency = currency)
    }

    fun showStandalone(listener: EventListener, paymentMethod: PaymentMethod) {
      instance?.loadPaymentMethods(listOf(paymentMethod))
      show(listener, UXMode.STANDALONE_PAYMENT_METHOD)
    }

    /**
     * Initializes the Primer SDK with the Application context. This method assumes that
     * the context also implements the IClientTokenProvider interface
     */
    fun initialize(context: Context, theme: UniversalCheckoutTheme? = null) {
      initialize(context, context as ClientTokenProvider, theme)
    }

    /**
     * Load the provided payment methods for use with the SDK
     */
    fun loadPaymentMethods(paymentMethods: List<PaymentMethod>) {
      instance?.loadPaymentMethods(paymentMethods)
    }

    /**
     * Dismiss the checkout
     */
    fun dismiss() {
      EventBus.broadcast(CheckoutEvent.DismissInternal(CheckoutExitReason.DISMISSED_BY_CLIENT))
    }

    /**
     * Toggle the loading screen
     */
    fun showProgressIndicator(visible: Boolean) {
      EventBus.broadcast(CheckoutEvent.ToggleProgressIndicator(visible))
    }

    /**
     * Show a success screen then dismiss
     */
    fun showSuccess(autoDismissDelay: Int = 3000) {
      EventBus.broadcast(CheckoutEvent.ShowSuccess(autoDismissDelay))
    }

    /**
     * Destroy the primer checkout and release any resources
     */
    fun destroy() {
      instance?.destroy()
      instance = null
    }


    /**
     * Show the checkout sheet and attach a listener which will receive callback events
     */
    private fun show(
      listener: EventListener,
      uxMode: UXMode,
      amount: Int? = null,
      currency: String? = null,
    ) {
      instance?.show(listener, uxMode, amount, currency)
    }
  }
}