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
import org.jetbrains.kotlinx.lincheck.strategy.IncorrectIterationResult
import org.jetbrains.kotlinx.lincheck.strategy.IterationResult
import org.jetbrains.kotlinx.lincheck.test.AbstractLinCheckTest
import kotlin.reflect.KClass

abstract class AbstractQueueTest(queueConstructor: (Int) -> Queue, vararg expectedErrors: KClass<out IterationResult>) : AbstractLinCheckTest(*expectedErrors) {
    private val queue = queueConstructor(3)

    @Operation(handleExceptionsAsResult = [Queue.FullQueueException::class])
    fun put(@Param(gen = IntGen::class) args: Int) = queue.put(args)

    @Operation(handleExceptionsAsResult = [Queue.EmptyQueueException::class])
    fun get(): Int = queue.get()

    override fun extractState(): Any = queue
}

class QueueCorrectTest : AbstractQueueTest(::QueueSynchronized)
class QueueWrong1Test : AbstractQueueTest(::QueueWrong1, IncorrectIterationResult::class)
class QueueWrong2Test : AbstractQueueTest(::QueueWrong2, IncorrectIterationResult::class)
class QueueWrong3Test : AbstractQueueTest(::QueueWrong3, IncorrectIterationResult::class)