package org.jetbrains.kotlinx.lincheck.distributed

import org.jetbrains.kotlinx.lincheck.runner.InvocationResult
import org.jetbrains.kotlinx.lincheck.runner.Runner
import org.jetbrains.kotlinx.lincheck.strategy.Strategy

open class DistributedRunner(strategy: Strategy,
                             testClass: Class<*>) : Runner(strategy,
        testClass) {

    override fun run(): InvocationResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }



}