/*-
 * #%L
 * libtest
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
package org.jetbrains.kotlinx.lincheck.tests.custom

import org.jetbrains.kotlinx.lincheck.LinChecker
import org.jetbrains.kotlinx.lincheck.annotations.*
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.paramgen.*
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressCTest
import org.jetbrains.kotlinx.lincheck.verifier.*
import org.junit.Test

@StressCTest(iterations = 30)
@Param(name = "value", gen = IntGen::class, conf = "1:3")
class DequeLinearizabilityTest : VerifierState() {
    private val deque = LockFreeDeque<Int>();

    @Operation
    fun pushLeft(@Param(name = "value") value: Int) = deque.pushLeft(value)

    @Operation
    fun pushRight(@Param(name = "value") value: Int) = deque.pushRight(value)

    @Operation
    fun popLeft(): Int? = deque.popLeft()

    @Operation
    fun popRight(): Int? = deque.popRight()

    override fun extractState(): Any {
        val elements = ArrayList<Int>()
        while (true) {
            val x = popLeft() ?: break
            elements += x
        }
        return elements
    }

    @Test
    fun test() = LinChecker.check(DequeLinearizabilityTest::class.java)
}
