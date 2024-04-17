package pro.respawn.flowmvi.sample.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.qualifier
import pro.respawn.flowmvi.android.StoreViewModel
import pro.respawn.flowmvi.android.subscribe
import pro.respawn.flowmvi.sample.R
import pro.respawn.flowmvi.sample.databinding.ActivityXmlBinding
import pro.respawn.flowmvi.sample.features.xmlactivity.XmlActivityAction
import pro.respawn.flowmvi.sample.features.xmlactivity.XmlActivityAction.ShowIncrementedSnackbar
import pro.respawn.flowmvi.sample.features.xmlactivity.XmlActivityContainer
import pro.respawn.flowmvi.sample.features.xmlactivity.XmlActivityIntent
import pro.respawn.flowmvi.sample.features.xmlactivity.XmlActivityIntent.ClickedIncrementCounter
import pro.respawn.flowmvi.sample.features.xmlactivity.XmlActivityState
import pro.respawn.flowmvi.sample.features.xmlactivity.XmlActivityState.DisplayingCounter
import pro.respawn.kmmutils.common.fastLazy

private typealias ViewModel = StoreViewModel<XmlActivityState, XmlActivityIntent, XmlActivityAction>

internal class XmlActivity : ComponentActivity() {

    private val vm by viewModel<ViewModel>(qualifier<XmlActivityContainer>())
    private val binding by fastLazy { ActivityXmlBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        subscribe(vm.store, ::consume, ::render)
        with(binding) {
            btnIncrement.setOnClickListener { vm.store.intent(ClickedIncrementCounter) }
        }
    }

    private fun render(state: XmlActivityState) = with(binding) {
        when (state) {
            is DisplayingCounter -> {
                tvCounter.isVisible = true
                btnIncrement.isVisible = true
                tvCounter.text = getString(R.string.counter_template, state.counter)

                tvError.isVisible = false
                progress.isVisible = false
            }
            is XmlActivityState.Error -> {
                tvError.isVisible = true
                tvError.text = state.e?.message ?: getString(R.string.error_message)

                progress.isVisible = false
                tvCounter.isVisible = false
                btnIncrement.isVisible = false
            }
            is XmlActivityState.Loading -> {
                progress.isVisible = true

                tvError.isVisible = false
                tvCounter.isVisible = false
                btnIncrement.isVisible = false
            }
        }
    }

    private fun consume(action: XmlActivityAction) {
        when (action) {
            ShowIncrementedSnackbar -> Snackbar.make(
                binding.root,
                getString(R.string.incremented_counter_message),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
}
