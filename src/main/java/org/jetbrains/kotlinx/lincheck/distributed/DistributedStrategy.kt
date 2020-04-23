package org.jetbrains.kotlinx.lincheck.distributed

import org.jetbrains.kotlinx.lincheck.execution.ExecutionScenario
import org.jetbrains.kotlinx.lincheck.runner.CompletedInvocationResult
import org.jetbrains.kotlinx.lincheck.runner.ParallelThreadsRunner
import org.jetbrains.kotlinx.lincheck.runner.Runner
import org.jetbrains.kotlinx.lincheck.strategy.IncorrectResultsFailure
import org.jetbrains.kotlinx.lincheck.strategy.LincheckFailure
import org.jetbrains.kotlinx.lincheck.strategy.Strategy
import org.jetbrains.kotlinx.lincheck.strategy.toLincheckFailure
import org.jetbrains.kotlinx.lincheck.verifier.Verifier
import java.util.*

class DistributedStrategy(testCfg: DistributedCTestConfiguration,
                          testClass: Class<*>,
                          scenario: ExecutionScenario,
                          private val verifier: Verifier
) : Strategy(scenario) {
    private val random = Random(0)
    private val invocations = testCfg.invocationsPerIteration
    private val runner: Runner
    private val waits: MutableList<IntArray>?

    init {
        // Create waits if needed
        waits = if (testCfg.addWaits) ArrayList() else null
        if (testCfg.addWaits) {
            for (actorsForThread in scenario.parallelExecution) {
                waits!!.add(IntArray(actorsForThread.size))
            }
        }
        // Create runner
        runner = ParallelThreadsRunner(this, testClass, waits)
    }

    override fun run(): LincheckFailure? {
        try {
            // Run invocations
            for (invocation in 0 until invocations) {
                // Specify waits if needed
                if (waits != null) {
                    val maxWait = (invocation.toFloat() * MAX_WAIT / invocations).toInt() + 1
                    for (waitsForThread in waits) {
                        for (i in waitsForThread.indices) {
                            waitsForThread[i] = random.nextInt(maxWait)
                        }
                    }
                }
                when (val ir = runner.run()) {
                    is CompletedInvocationResult -> {
                        if (!verifier.verifyResults(scenario, ir.results))
                            return IncorrectResultsFailure(scenario, ir.results)
                    }
                    else -> return ir.toLincheckFailure(scenario)
                }
            }
            return null
        } finally {
            runner.close()
        }
    }
}

private const val MAX_WAIT = 1000
