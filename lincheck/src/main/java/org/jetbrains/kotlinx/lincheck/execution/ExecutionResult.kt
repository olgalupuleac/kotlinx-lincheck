package org.jetbrains.kotlinx.lincheck.execution

import org.jetbrains.kotlinx.lincheck.*

/**
 * This class represents a result corresponding to
 * the specified [scenario][ExecutionScenario] execution.
 *
 * All the result parts should have the same dimensions as the scenario.
 */
data class ExecutionResult(
    /**
     * Results of the initial sequential part of the execution.
     * @see ExecutionScenario.initExecution
     */
    val initResults: List<Result>,
    /**
     * Results of the parallel part of the execution.
     * @see ExecutionScenario.parallelExecution
     */
    val parallelResults: List<List<Result>>,
    /**
     * Results of the last sequential part of the execution.
     * @see ExecutionScenario.postExecution
     */
    val postResults: List<Result>
)