package org.jetbrains.kotlinx.lincheck.distributed

import org.jetbrains.kotlinx.lincheck.Options

class DistributedOptions : Options<DistributedOptions,
        DistributedCTestConfiguration>() {
    override fun createTestConfigurations(testClass: Class<*>?): DistributedCTestConfiguration {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}