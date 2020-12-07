package io.primer.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.primer.android.logging.Logger
import io.primer.android.model.Model
import io.primer.android.model.json
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.viewmodel.BaseViewModel
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationViewModel
import io.primer.android.viewmodel.ViewStatus
import kotlinx.serialization.serializer

internal class CheckoutSheetActivity : AppCompatActivity() {
  private val log = Logger("checkout-activity")
  private lateinit var model: Model
  private lateinit var viewModel: PrimerViewModel
  private lateinit var tokenizationViewModel: TokenizationViewModel
  private lateinit var sheet: CheckoutSheetFragment

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Unmarshal the configuration from the intent
    initializeModel()

    viewModel = initializeViewModel(PrimerViewModel::class.java)
    tokenizationViewModel = initializeViewModel(TokenizationViewModel::class.java)

    sheet = CheckoutSheetFragment.newInstance()

    attachViewModelListeners()

    // Open the bottom sheet
    openSheet()
  }

  private inline fun <reified T> unmarshal(name: String): T {
    val serialized = intent.getStringExtra(name)
    return json.decodeFromString<T>(serializer(), serialized!!)
  }

  private fun initializeModel() {
    model = Model(
      unmarshal("config"),
      unmarshal("paymentMethods")
    )
  }

  private fun <T: BaseViewModel> initializeViewModel(modelCls: Class<T>): T {
    val vm = ViewModelProvider(this).get(modelCls)

    vm.initialize(model)

    return vm
  }


//  private fun <T: BaseViewModel> initializeViewModel(factory: ViewModelProvider.Factory, modelCls: Class<T>): T {
//    val vm = ViewModelProvider(this, factory).get(modelCls)
//
//    vm.initialize(model)
//
//    return vm
//  }

  private fun attachViewModelListeners() {
    viewModel.viewStatus.observe(this, {
      val fragment = when (it) {
        ViewStatus.INITIALIZING -> InitializingFragment.newInstance()
        ViewStatus.SELECT_PAYMENT_METHOD -> SelectPaymentMethodFragment.newInstance()
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

    viewModel.sheetDismissed.observe(this, { dismissed ->
      log("Sheet dismissed changed: $dismissed")

      if (dismissed) {
        finish()
      }
    })
  }

  private fun openFragment(fragment: Fragment) {
    openFragment(NewFragmentBehaviour { fragment })
  }

  private fun openFragment(behaviour: NewFragmentBehaviour) {
    behaviour.execute(sheet)
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
