package org.jetbrains.kotlinx.lincheck.runner

import org.jetbrains.kotlinx.lincheck.execution.*

sealed class IterationResult {
    abstract val isError: Boolean
}

internal object SuccessfulIterationResult : IterationResult() {
    override val isError: Boolean get() = false
}

internal class IncorrectIterationResult(val scenario: ExecutionResult, val result: ExecutionResult) : IterationResult() {
    override val isError: Boolean get() = true
}

internal class FailedIterationResult(val failedInvocationResult: InvocationResult) : IterationResult() {
    init {
        require(failedInvocationResult.isError) { "The provided invocation result should be a failure" }
    }
    override val isError: Boolean get() = true
}