package pro.respawn.flowmvi.debugger.server.ui

import pro.respawn.kmmutils.inputforms.Form
import pro.respawn.kmmutils.inputforms.Rule
import pro.respawn.kmmutils.inputforms.ValidationError
import pro.respawn.kmmutils.inputforms.ValidationStrategy.FailFast
import pro.respawn.kmmutils.inputforms.default.Rules
import pro.respawn.kmmutils.inputforms.default.Rules.AsciiOnly
import pro.respawn.kmmutils.inputforms.default.Rules.NonEmpty
import pro.respawn.kmmutils.inputforms.dsl.checks

private val PortRange = 0..10000

val PortForm = Form(
    FailFast,
    NonEmpty,
    Rules.DigitsOnly,
    Rule { { it.toIntOrNull() in PortRange } checks { ValidationError.NotInRange(it, PortRange) } }
)

val HostForm = Form(
    FailFast,
    NonEmpty,
    AsciiOnly,
)
