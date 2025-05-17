package pro.respawn.flowmvi.plugins.delegate

public sealed interface DelegationMode {
    public class Immediate : DelegationMode
    public data class WhileSubscribed(val minSubs: Int = 1) : DelegationMode

    public companion object {

        public val Default: DelegationMode = WhileSubscribed()
    }
}
