package org.jetbrains.kotlinx.lincheck.distributed

/**
 * Environment interface for communication with other processes.
 */
interface Environment {
    /**
     * Identifier of this process (from 1 to [nProcesses]).
     */
    val processId: Int

    /**
     * The total number of processes in the system.
     */
    val nProcesses: Int

    /**
     * Sends the specified [message] to the process [destId] (from 1 to [nProcesses]).
     */
    fun send(destId: Int, message: String)

    /**
     * Sends the specified [message] to all processes except itself (from 1 to
     * [nProcesses]).
     */
    fun broadcast(message: String) {
        for (i in 1..nProcesses) {
            if (i != processId) {
                send(i, message)
            }
        }
    }
}