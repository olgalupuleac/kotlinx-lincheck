/*
 * #%L
 * Lincheck
 * %%
 * Copyright (C) 2015 - 2018 Devexperts, LLC
 * Copyright (C) 2019 JetBrains s.r.o.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package org.jetbrains.kotlinx.lincheck

import org.jetbrains.kotlinx.lincheck.annotations.*
import org.jetbrains.kotlinx.lincheck.execution.*
import org.jetbrains.kotlinx.lincheck.runner.*
import org.jetbrains.kotlinx.lincheck.strategy.*
import org.jetbrains.kotlinx.lincheck.verifier.*
import java.util.*

/**
 * This class runs concurrent tests.
 * See [.check] and [.check] methods for details.
 */
private class LinChecker (private val testClass: Class<*>, options: Options<*, *>?) {
    private val testConfigurations: List<CTestConfiguration>
    private val testStructure: CTestStructure
    private val reporter: Reporter
    /**
     * @throws AssertionError if algorithm or data structure is not correct
     */
    @Throws(AssertionError::class)
    fun check() {
        val resultsByConfiguration = checkDetailed()
        for (results in resultsByConfiguration.values) {
            val lastResult = results[results.size - 1]
            if (lastResult.isError) throw AssertionError(reporter.generateReport(lastResult))
        }
    }

    init {
        testStructure = CTestStructure.getFromTestClass(testClass)
        val logLevel: LoggingLevel
        if (options != null) {
            logLevel = options.logLevel
            testConfigurations = listOf(options.createTestConfigurations(testClass))
        } else {
            logLevel = logLevelFromAnnotation
            testConfigurations = CTestConfiguration.createFromTestClass(testClass)
        }
        reporter = Reporter(logLevel)
    }

    /**
     * @return TestReport with information about concurrent test run.
     */
    @Throws(AssertionError::class)
    fun checkDetailed(): Map<CTestConfiguration, List<IterationResult>> {
        check(!testConfigurations!!.isEmpty()) { "No Lin-Check test configuration to run" }
        val resultsByConfiguration: MutableMap<CTestConfiguration, List<IterationResult>> = HashMap()
        for (testConfiguration in testConfigurations) {
            try {
                val results: List<IterationResult> = checkDetailedImpl(testConfiguration)
                resultsByConfiguration[testConfiguration] = results
                if (results[results.size - 1].isError) return resultsByConfiguration
            } catch (e: Exception) { // an Exception in LinCheck
                throw IllegalStateException(e)
            }
        }
        return resultsByConfiguration
    }

    /**
     * Generates the specified in the configuration number of scenarios, invokes them,
     * and returns the corresponding invocation results.
     */
    @Throws(Exception::class)
    private fun generateScenariosAndInvoke(testCfg: CTestConfiguration): Map<ExecutionScenario, List<InvocationResult>> {
        val exGen = createExecutionGenerator(testCfg.generatorClass, testCfg)
        val allResults: MutableMap<ExecutionScenario, List<InvocationResult>> = HashMap()
        // Run iterations
        for (iteration in 1..testCfg.iterations) {
            val scenario = exGen.nextExecution()
            validateScenario(scenario)
            val strategy = Strategy.createStrategy(testCfg, testClass, scenario, reporter)
            val scenarioResults = strategy.run()
            allResults[scenario] = scenarioResults
            // Check whether an error is already detected
            if (scenarioResults.stream().anyMatch(InvocationResult::isError)) break
        }
        return allResults
    }

    // Tries to minimize the specified failing scenario to make the error easier to understand.
// The algorithm is greedy: it tries to remove one actor from the scenario and checks
// whether a test with the modified one fails with error as well. If it fails,
// then the scenario has been successfully minimized, and the algorithm tries to minimize it again, recursively.
// Otherwise, if no actor can be removed so that the generated test fails, the minimization is completed.
// Thus, the algorithm works in the linear time of the total number of actors.
    @Throws(AssertionError::class, Exception::class)
    private fun minimizeScenario(scenario: ExecutionScenario, testCfg: CTestConfiguration, currentReport: IterationResult): IterationResult {
        reporter.logScenarioMinimization(scenario)
        for (i in scenario.parallelExecution.indices) {
            for (j in scenario.parallelExecution[i].indices) {
                val newScenario = copyScenario(scenario)
                newScenario.parallelExecution[i].removeAt(j)
                if (newScenario.parallelExecution[i].isEmpty()) newScenario.parallelExecution.removeAt(i) // remove empty thread
                val result = minimizeNewScenarioAttempt(newScenario, testCfg)
                if (result.isError) return result
            }
        }
        for (i in scenario.initExecution.indices) {
            val newScenario = copyScenario(scenario)
            newScenario.initExecution.removeAt(i)
            val result = minimizeNewScenarioAttempt(newScenario, testCfg)
            if (result.isError) return result
        }
        for (i in scenario.postExecution.indices) {
            val newScenario = copyScenario(scenario)
            newScenario.postExecution.removeAt(i)
            val result = minimizeNewScenarioAttempt(newScenario, testCfg)
            if (result.isError) return result
        }
        return currentReport
    }

    @Throws(AssertionError::class, Exception::class)
    private fun minimizeNewScenarioAttempt(newScenario: ExecutionScenario, testCfg: CTestConfiguration): IterationResult {
        try {
            val result: IterationResult = runScenario(newScenario, testCfg)
            if (result.isError) return minimizeScenario(newScenario, testCfg, result)
        } catch (e: IllegalArgumentException) { // Ignore incorrect scenarios
        }
        return SuccessIterationResult(newScenario, testCfg.iterations)
    }

    private fun copyScenario(scenario: ExecutionScenario): ExecutionScenario {
        val initExecution: List<Actor> = ArrayList(scenario.initExecution)
        val parallelExecution: MutableList<List<Actor>> = ArrayList()
        for (i in scenario.parallelExecution.indices) {
            parallelExecution.add(ArrayList(scenario.parallelExecution[i]))
        }
        val postExecution: List<Actor> = ArrayList(scenario.postExecution)
        return ExecutionScenario(initExecution, parallelExecution, postExecution)
    }

    private fun validateScenario(scenario: ExecutionScenario) {
        if (scenario.hasSuspendableActors()) {
            require(!scenario.initExecution.stream().anyMatch(Actor::isSuspendable)) { "Generated execution scenario for the test class with suspendable methods contains suspendable actors in initial part" }
            require(!(scenario.parallelExecution.stream().anyMatch { actors: List<Actor?> -> actors.stream().anyMatch(Actor::isSuspendable) } && scenario.postExecution.size > 0)) { "Generated execution scenario for the test class with suspendable methods has non-empty post part" }
        }
    }

    private fun createVerifier(verifierClass: Class<out Verifier>, scenario: ExecutionScenario, sequentialSpecification: Class<*>) =
        verifierClass.getConstructor(ExecutionScenario::class.java, Class::class.java).newInstance(scenario, sequentialSpecification)

    private fun createExecutionGenerator(generatorClass: Class<out ExecutionGenerator>, testConfiguration: CTestConfiguration) =
        generatorClass.getConstructor(CTestConfiguration::class.java, CTestStructure::class.java).newInstance(testConfiguration, testStructure)

    private val logLevelFromAnnotation = testClass.getAnnotation(LogLevel::class.java)?.value ?: DEFAULT_LOG_LEVEL

    // We use this companion object for backwards compatibility.
    companion object {
        /**
         * Runs the specified concurrent tests. If [options] is null, the provided on
         * the testing class `@...CTest` annotations are used to specify the test parameters.
         *
         * @throws AssertionError if any of the tests fails.
         */
        @JvmStatic
        @JvmOverloads
        fun <O : Options<O, *>?> check(testClass: Class<*>, options: O? = null) {
            LinChecker(testClass, options).check()
        }
    }
}


/**
 * This is a short-cut for the following code:
 * ```
 *  val options = ...
 *  LinChecker.check(testClass, options)
 * ```
 */
fun <O : Options<O, *>> O.check(testClass: Class<*>) = LinChecker.check(testClass, this)