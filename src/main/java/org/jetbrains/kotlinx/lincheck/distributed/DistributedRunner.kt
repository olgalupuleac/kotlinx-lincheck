package org.jetbrains.kotlinx.lincheck.distributed

import org.jetbrains.kotlinx.lincheck.runner.InvocationResult
import org.jetbrains.kotlinx.lincheck.runner.Runner
import org.jetbrains.kotlinx.lincheck.strategy.Strategy
import java.lang.reflect.Method

open class DistributedRunner(strategy: Strategy,
                             testClass: Class<*>,
                             validationFunctions: List<Method>?,
                             waits: List<IntArray>?) : Runner(strategy, testClass,
        validationFunctions) {

    override fun run(): InvocationResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}