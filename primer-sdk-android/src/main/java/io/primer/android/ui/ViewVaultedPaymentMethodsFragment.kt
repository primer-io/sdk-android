package io.primer.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.primer.android.R
import io.primer.android.viewmodel.PrimerViewModel

class ViewVaultedPaymentMethodsFragment: Fragment() {
  private lateinit var viewModel: PrimerViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewModel = PrimerViewModel.getInstance(requireActivity())

    viewModel.vaultedPaymentMethods.observe(viewLifecycleOwner, {

    })
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_view_vaulted_payment_methods, container, false)
  }

  companion object {
    fun newInstance() : ViewVaultedPaymentMethodsFragment {
      return ViewVaultedPaymentMethodsFragment()
    }
  }
}