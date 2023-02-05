package pro.respawn.flowmvi.sample.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import pro.respawn.flowmvi.MVIView
import pro.respawn.flowmvi.android.subscribe
import pro.respawn.flowmvi.sample.databinding.ActivityBasicBinding
import pro.respawn.flowmvi.sample.view.BasicActivityAction.ShowSnackbar
import pro.respawn.flowmvi.sample.view.BasicActivityIntent.ClickedFab
import pro.respawn.flowmvi.sample.view.BasicActivityState.DisplayingContent

class BasicActivity :
    ComponentActivity(),
    // Or use lambdas when calling subscribe()
    MVIView<BasicActivityState, BasicActivityIntent, BasicActivityAction> {

    private var _b: ActivityBasicBinding? = null
    private val binding get() = requireNotNull(_b)

    private val vm by viewModel<NoBaseClassViewModel>()

    // If your viewModel implements MVIProvider, you can just use by viewModel() on provider variable
    override val provider get() = vm.store

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _b = ActivityBasicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Subscribe to the provider
        // for fragments, call this in onViewCreated()
        subscribe()

        binding.apply {
            fab.setOnClickListener { send(ClickedFab) }
        }
    }

    // Each time state changes, render it here
    override fun render(state: BasicActivityState) = with(binding) {
        when (state) {
            is DisplayingContent -> {
                counter.text = state.counter.toString()
            }
        }
    }

    // Handle any side effects of the UI layer here
    override fun consume(action: BasicActivityAction) {
        when (action) {
            is ShowSnackbar -> {
                Snackbar.make(binding.root, getString(action.res), Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
