package pro.respawn.flowmvi.api

public interface Recoverable {

    public suspend fun recover(e: Exception)
}
