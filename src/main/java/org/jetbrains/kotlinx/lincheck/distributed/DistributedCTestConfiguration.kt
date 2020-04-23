package org.jetbrains.kotlinx.lincheck.distributed

import org.jetbrains.kotlinx.lincheck.CTestConfiguration
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressStrategy

/*
 * #%L
 * Lincheck
 * %%
 * Copyright (C) 2015 - 2018 Devexperts, LLC
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

import org.jetbrains.kotlinx.lincheck.execution.*
import org.jetbrains.kotlinx.lincheck.strategy.*
import org.jetbrains.kotlinx.lincheck.verifier.Verifier

/**
 * Configuration for [stress][StressStrategy] strategy.
 */
class DistributedCTestConfiguration(testClass: Class<*>, iterations: Int,
                                  threads: Int, actorsPerThread: Int,
                                    actorsBefore: Int, actorsAfter: Int,
                               generatorClass: Class<out ExecutionGenerator>,
                                    verifierClass: Class<out Verifier>,
                               val invocationsPerIteration: Int,
                                    val addWaits: Boolean,
                                    requireStateEquivalenceCheck: Boolean,
                                    minimizeFailedScenario: Boolean,
                               sequentialSpecification: Class<*>) :
        CTestConfiguration(testClass, iterations, threads, actorsPerThread, actorsBefore, actorsAfter, generatorClass, verifierClass,
        requireStateEquivalenceCheck,
        minimizeFailedScenario, sequentialSpecification) {

    override fun createStrategy(testClass: Class<*>, scenario: ExecutionScenario, verifier: Verifier): Strategy {
        return DistributedStrategy(this, testClass, scenario, verifier)
    }

    companion object {
        val DEFAULT_INVOCATIONS = 10000
    }
}
