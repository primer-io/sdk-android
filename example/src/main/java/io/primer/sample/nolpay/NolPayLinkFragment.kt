package io.primer.sample.nolpay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import io.primer.android.components.manager.nolPay.linkCard.component.NolPayLinkCardComponent
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCardStep
import io.primer.android.components.manager.nolPay.PrimerHeadlessUniversalCheckoutNolPayManager
import io.primer.sample.R
import io.primer.sample.databinding.FragmentNolPayLinkBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NolPayLinkFragment : Fragment() {

    private lateinit var binding: FragmentNolPayLinkBinding
    private lateinit var linkCardComponent: NolPayLinkCardComponent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentNolPayLinkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.parentNavFragment) as NavHostFragment

        linkCardComponent =
            PrimerHeadlessUniversalCheckoutNolPayManager().provideNolPayLinkCardComponent(
               this
            )
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                linkCardComponent.componentError.collectLatest {
                    Snackbar.make(requireView(), it.description, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                linkCardComponent.componentStep.collectLatest { nolPayLinkStep ->
                    when (nolPayLinkStep) {
                        is NolPayLinkCardStep.CollectPhoneData ->
                            navHostFragment.findNavController()
                                .navigate(R.id.action_NolLinkCardScanTagFragment_to_NolPayPhoneInputFragment)

                        is NolPayLinkCardStep.CollectOtpData -> navHostFragment.findNavController()
                            .navigate(R.id.action_NolPayPhoneInputFragment_to_NolPayLinkOtpFragment)

                        is NolPayLinkCardStep.CollectTagData -> Unit
                        is NolPayLinkCardStep.CardLinked -> {
                            Snackbar.make(
                                requireView(),
                                "Successfully linked!",
                                Snackbar.LENGTH_SHORT
                            )
                                .show()
                            findNavController().navigate(R.id.action_NolLinkFragment_to_NolFragment)
                        }
                    }
                }
            }
        }


        linkCardComponent.start()
    }
}
