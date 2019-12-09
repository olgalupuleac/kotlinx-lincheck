/*-
 * #%L
 * Lincheck
 * %%
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
package org.jetbrains.kotlinx.lincheck.test

import org.jetbrains.kotlinx.lincheck.*
import org.jetbrains.kotlinx.lincheck.strategy.IterationResult
import org.jetbrains.kotlinx.lincheck.strategy.SuccessIterationResult
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * An abstraction for testing all lincheck strategies
 */
abstract class AbstractLinCheckTest(vararg expectedErrors: KClass<out IterationResult>) : VerifierState() {
    private val expectedErrors = if (expectedErrors.isEmpty()) listOf(SuccessIterationResult::class) else expectedErrors.toList()
    private val reporter = Reporter(LoggingLevel.INFO)

          override fun extractState() = error("Not implemented")

    open fun <O: Options<O, *>> O.customizeOptions(): O = this

    private fun runTest(createOptions: () -> Options<*, *>) {
        val result = LinChecker.checkDetailed(this.javaClass, createOptions()).values.first().last()
        assertTrue(reporter.generateReport(result), expectedErrors.any { expectedError -> result::class.isSubclassOf(expectedError)})
    }

    @Test(timeout = 100_000)
    fun testWithStressStrategy() = runTest {
        StressOptions().iterations(30).customizeOptions()
    }
}
