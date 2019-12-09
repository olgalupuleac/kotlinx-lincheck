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
package org.jetbrains.kotlinx.lincheck.tests.custom

import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.strategy.IterationResult
import org.jetbrains.kotlinx.lincheck.test.AbstractLinCheckTest
import kotlin.reflect.KClass

@Param(name = "key", gen = IntGen::class, conf = "1:5")
abstract class AbstractSetTest(private val set: Set, vararg expectedErrors: KClass<out IterationResult>) : AbstractLinCheckTest(*expectedErrors) {
    @Operation
    fun add(@Param(name = "key") key: Int): Boolean  = set.add(key)

    @Operation
    fun remove(@Param(name = "key") key: Int): Boolean = set.remove(key)

    @Operation
    operator fun contains(@Param(name = "key") key: Int): Boolean = set.contains(key)

    override fun extractState(): Any = (1..5).map { set.contains(it) }
}

class SpinLockSetCorrectTest : AbstractSetTest(SpinLockBasedSet())
class ReentrantLockSetCorrectTest : AbstractSetTest(ReentrantLockBasedSet())
class SynchronizedLockSetCorrectTest : AbstractSetTest(SynchronizedBlockBasedSet())
class SynchronizedMethodSetCorrectTest : AbstractSetTest(SynchronizedMethodBasedSet())