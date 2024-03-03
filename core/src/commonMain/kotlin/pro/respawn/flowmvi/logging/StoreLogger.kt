package pro.respawn.flowmvi.logging

/**
 * Store logger can be invoked by any code in the store. [log] prints a message to the logger instance
 * configured when building the store.
 */
public fun interface StoreLogger {

    /**
     * Print a message to the log.
     * @param level Log priority. Only used on some platforms where levels are supported, otherwise prints an emoji in
     * the message body
     * @param tag Tag. Only used on Android right now - the only platform supporting tags. Other platforms simply
     * prepend the tag to the message.
     * @param message lazy message. Should only be evaluated if actual printing happens
     */
    public fun log(level: StoreLogLevel, tag: String?, message: () -> String)
}
