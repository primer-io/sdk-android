package io.primer.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.logging.Logger
import io.primer.android.model.Model
import io.primer.android.model.dto.CheckoutDismissInfo
import io.primer.android.model.json
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.ui.fragments.*
import io.primer.android.ui.fragments.CheckoutSheetFragment
import io.primer.android.ui.fragments.InitializingFragment
import io.primer.android.ui.fragments.SelectPaymentMethodFragment
import io.primer.android.viewmodel.BaseViewModel
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationViewModel
import io.primer.android.viewmodel.ViewStatus
import kotlinx.serialization.serializer

internal class CheckoutSheetActivity : AppCompatActivity() {
  private val log = Logger("checkout-activity")
  private var subscription: EventBus.SubscriptionHandle? = null
  private lateinit var model: Model
  private lateinit var viewModel: PrimerViewModel
  private lateinit var tokenizationViewModel: TokenizationViewModel
  private lateinit var sheet: CheckoutSheetFragment

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Unmarshal the configuration from the intent
    model = Model(
      unmarshal("config"),
      unmarshal("paymentMethods")
    )

    viewModel =
      initializeViewModel(PrimerViewModel.ProviderFactory(model), PrimerViewModel::class.java)
    tokenizationViewModel = initializeViewModel(
      TokenizationViewModel.ProviderFactory(model),
      TokenizationViewModel::class.java
    )

    sheet = CheckoutSheetFragment.newInstance()

    attachViewModelListeners()

    attachEventListeners()

    // Open the bottom sheet
    openSheet()
  }

  private inline fun <reified T> unmarshal(name: String): T {
    val serialized = intent.getStringExtra(name)
    return json.decodeFromString(serializer(), serialized!!)
  }

  private fun <T : BaseViewModel> initializeViewModel(
    factory: ViewModelProvider.Factory,
    modelCls: Class<T>
  ): T {
    val vm = ViewModelProvider(this, factory).get(modelCls)

    vm.initialize()

    return vm
  }

  private fun attachViewModelListeners() {
    viewModel.viewStatus.observe(this, {
      val fragment = when (it) {
        ViewStatus.INITIALIZING -> InitializingFragment.newInstance()
        ViewStatus.SELECT_PAYMENT_METHOD -> SelectPaymentMethodFragment.newInstance()
        ViewStatus.VIEW_VAULTED_PAYMENT_METHODS -> VaultedPaymentMethodsFragment.newInstance()
        else -> null
      }

      if (fragment != null) {
        openFragment(fragment)
      }
    })

    viewModel.selectedPaymentMethod.observe(this, { pm ->
      if (pm != null) {
        val behaviour = pm.selectedBehaviour

        if (behaviour is NewFragmentBehaviour) {
          openFragment(behaviour)
        }
      }
    })
  }

  private fun attachEventListeners() {
    subscription = EventBus.subscribe {
      when (it) {
        is CheckoutEvent.DismissInternal -> {
          EventBus.broadcast(CheckoutEvent.Dismissed(CheckoutDismissInfo(it.data)))
          finish()
        }
        is CheckoutEvent.ShowSuccess -> {
          openFragment(SuccessFragment.newInstance())
        }
      }
    }
  }

  private fun openFragment(fragment: Fragment) {
    openFragment(NewFragmentBehaviour({ fragment }))
  }

  private fun openFragment(behaviour: NewFragmentBehaviour) {
    behaviour.execute(sheet)
  }

  override fun onDestroy() {
    super.onDestroy()
    subscription?.unregister()
    subscription = null
  }

  private fun openSheet() {
    supportFragmentManager.let {
      log("Showing checkout sheet")
      sheet.apply {
        show(it, tag)
      }
    }
  }
}
