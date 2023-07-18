package pro.respawn.flowmvi.sample.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.qualifier
import pro.respawn.flowmvi.MVIView
import pro.respawn.flowmvi.android.StoreViewModel
import pro.respawn.flowmvi.android.subscribe
import pro.respawn.flowmvi.sample.databinding.ActivityBasicBinding
import pro.respawn.flowmvi.sample.view.BasicAction.ShowSnackbar
import pro.respawn.flowmvi.sample.view.BasicIntent.ClickedFab
import pro.respawn.flowmvi.sample.view.BasicState.DisplayingCounter

class BasicActivity :
    ComponentActivity(),
    // Or use lambdas when calling subscribe()
    MVIView<BasicState, BasicIntent, BasicAction> {

    private var _b: ActivityBasicBinding? = null
    private val binding get() = requireNotNull(_b)

    // If your viewModel implements MVIProvider, you can just use by viewModel() on store variable
    override val provider: StoreViewModel<BasicState, BasicIntent, BasicAction> by viewModel(
        qualifier = BasicProvider.qualifier,
    ) { parametersOf("I am a parameter") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _b = ActivityBasicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Subscribe to the store
        // for fragments, call this in onViewCreated()
        subscribe()

        binding.apply {
            fab.setOnClickListener { send(ClickedFab) }
        }
    }

    // Each time state changes, render it here
    override fun render(state: BasicState) = with(binding) {
        when (state) {
            is DisplayingCounter -> {
                with(tvCounter) {
                    isVisible = true
                    text = state.counter.toString()
                }
                with(tvParam) {
                    isVisible = true
                    text = state.param
                }
                fab.show()
                progress.hide()
            }
            is BasicState.Loading -> {
                tvCounter.isVisible = false
                tvParam.isVisible = false
                progress.show()
                fab.hide()
            }
        }
    }

    // Handle any side effects of the UI layer here
    override fun consume(action: BasicAction) {
        when (action) {
            is ShowSnackbar -> {
                Snackbar.make(binding.root, getString(action.res), Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
