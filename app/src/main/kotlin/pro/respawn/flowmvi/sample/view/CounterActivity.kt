package pro.respawn.flowmvi.sample.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import pro.respawn.flowmvi.android.subscribe
import pro.respawn.flowmvi.api.ActionConsumer
import pro.respawn.flowmvi.api.StateConsumer
import pro.respawn.flowmvi.sample.CounterAction
import pro.respawn.flowmvi.sample.CounterAction.ShowErrorMessage
import pro.respawn.flowmvi.sample.CounterAction.ShowLambdaMessage
import pro.respawn.flowmvi.sample.CounterState
import pro.respawn.flowmvi.sample.CounterState.DisplayingCounter
import pro.respawn.flowmvi.sample.R
import pro.respawn.flowmvi.sample.compose.ComposeActivity
import pro.respawn.flowmvi.sample.databinding.ActivityBasicBinding

class CounterActivity :
    ComponentActivity(),
    StateConsumer<CounterState>,
    ActionConsumer<CounterAction> {

    private var _b: ActivityBasicBinding? = null
    private val binding get() = requireNotNull(_b)
    private val container by viewModel<LambdaViewModel> { parametersOf("I am a parameter") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _b = ActivityBasicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Subscribe to the store
        // for fragments, call this in onViewCreated()
        subscribe(container.store)

        binding.apply {
            btnIncrement.setOnClickListener { container.onClickCounter() }
            btnToCompose.setOnClickListener {
                startActivity(Intent(this@CounterActivity, ComposeActivity::class.java))
            }
        }
    }

    // Handle any side effects of the UI layer here
    override fun consume(action: CounterAction) {
        when (action) {
            is ShowErrorMessage -> Snackbar.make(
                binding.root,
                getString(R.string.error_message, action.message),
                Snackbar.LENGTH_SHORT
            ).show()
            is ShowLambdaMessage -> Snackbar.make(binding.root, R.string.lambda_message, Snackbar.LENGTH_SHORT).show()
            is CounterAction.GoBack -> finish()
        }
    }

    // be sure to use ALL views at least once when rendering each state
    override fun render(state: CounterState) = with(binding) {
        when (state) {
            is DisplayingCounter -> {
                with(tvCounter) {
                    isVisible = true
                    text = getString(R.string.counter_template, state.counter)
                }
                with(tvParam) {
                    isVisible = true
                    text = state.param
                }
                with(tvTimer) {
                    isVisible = true
                    text = getString(R.string.timer_template, state.timer)
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
}
