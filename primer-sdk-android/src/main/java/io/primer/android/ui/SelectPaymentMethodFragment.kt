package io.primer.android.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.viewmodel.PrimerViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [SelectPaymentMethodFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SelectPaymentMethodFragment : Fragment() {
  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_select_payment_method, container, false)
  }

  private val log = Logger("checkout-fragment")
  private lateinit var viewModel: PrimerViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewModel = ViewModelProvider(this.requireActivity()).get(PrimerViewModel::class.java)

    viewModel.paymentMethods.observe(this, { items ->
      val container: ViewGroup = findViewById(R.id.primer_sheet_payment_methods_list)

      items.forEach { pm ->
        val button = pm.createButton(container)

        button.setOnClickListener {
          viewModel.setSelectedPaymentMethod(pm)
        }
      }
    })

    viewModel.amount.observe(this, {
      findViewById<SelectPaymentMethodTitle>(R.id.primer_sheet_title_layout).setAmount(it)
    })

    viewModel.uxMode.observe(this, {
      findViewById<SelectPaymentMethodTitle>(R.id.primer_sheet_title_layout).setUXMode(it)
    })
  }

  private fun <T: View> findViewById(id: Int): T {
    return requireView().findViewById(id)
  }

  companion object {
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SelectPaymentMethodFragment.
     */
    @JvmStatic
    fun newInstance() = SelectPaymentMethodFragment()
  }
}