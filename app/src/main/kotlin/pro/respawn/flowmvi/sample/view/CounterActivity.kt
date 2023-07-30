package pro.respawn.flowmvi.sample.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import pro.respawn.flowmvi.MVIView
import pro.respawn.flowmvi.android.subscribe
import pro.respawn.flowmvi.dsl.LambdaIntent
import pro.respawn.flowmvi.sample.compose.ComposeActivity
import pro.respawn.flowmvi.sample.databinding.ActivityBasicBinding
import pro.respawn.flowmvi.sample.provider.CounterAction
import pro.respawn.flowmvi.sample.provider.CounterAction.ShowSnackbar
import pro.respawn.flowmvi.sample.provider.CounterState
import pro.respawn.flowmvi.sample.provider.CounterState.DisplayingCounter
import pro.respawn.flowmvi.sample.provider.LambdaViewModel

// Or use lambdas when calling subscribe()
class CounterActivity :
    ComponentActivity(),
    MVIView<CounterState, LambdaIntent<CounterState, CounterAction>, CounterAction> {

    private var _b: ActivityBasicBinding? = null
    private val binding get() = requireNotNull(_b)

    // If your viewModel implements MVIProvider, you can just use by viewModel() on store variable
    override val provider by viewModel<LambdaViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _b = ActivityBasicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Subscribe to the store
        // for fragments, call this in onViewCreated()
        subscribe()

        binding.apply {
            btnIncrement.setOnClickListener { provider.onClickCounter() }
            btnToCompose.setOnClickListener {
                startActivity(Intent(this@CounterActivity, ComposeActivity::class.java))
            }
        }
    }

    // Each time state changes, render it here
    override fun render(state: CounterState) = with(binding) {
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
                with(tvTimer) {
                    isVisible = true
                    text = state.timer.toString()
                }
                progress.hide()
                tvError.isVisible = false
                btnIncrement.isVisible = true
                btnToCompose.isVisible = true
            }
            is CounterState.Loading -> {
                tvCounter.isVisible = false
                tvParam.isVisible = false
                tvTimer.isVisible = false
                progress.show()
                tvError.isVisible = false
                btnIncrement.isVisible = false
                btnToCompose.isVisible = false
            }
            is CounterState.Error -> {
                tvCounter.isVisible = false
                tvParam.isVisible = false
                tvTimer.isVisible = false
                progress.hide()
                tvError.isVisible = true
                btnIncrement.isVisible = false
                btnToCompose.isVisible = false
            }
        }
    }

    // Handle any side effects of the UI layer here
    override fun consume(action: CounterAction) {
        when (action) {
            is ShowSnackbar -> {
                Snackbar.make(binding.root, getString(action.res), Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
