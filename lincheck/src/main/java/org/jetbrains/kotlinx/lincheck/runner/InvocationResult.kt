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
package org.jetbrains.kotlinx.lincheck.runner

import org.jetbrains.kotlinx.lincheck.execution.*

/**
 * Represents results for invocations, see [Runner.run].
 * Marked as error (see [isError]) if this invocation has been failed.
 */
internal sealed class InvocationResult {
    abstract val isError: Boolean
}

/**
 * The invocation completed successfully with correct results.
 */
internal object SuccessfulInvocationResult: InvocationResult() {
    override val isError get() = false
}

/**
 * The invocation completed successfully, but the [results] are incorrect.
 */
internal data class IncorrectInvocationResult(
    val results: ExecutionResult
) : InvocationResult() {
    override val isError get() = true
}

/**
 * Indicates that the invocation has get into deadlock or livelock.
 */
internal data class DeadlockInvocationResult(
    val threadDump: Map<Thread, Array<StackTraceElement>>
) : InvocationResult() {
    override val isError get() = true
}

/**
 * The invocation has finished with an unexpected exception.
 */
internal class UnexpectedExceptionInvocationResult(
    val exception: Throwable
) : InvocationResult() {
    override val isError get() = true
}