package org.jetbrains.kotlinx.lincheck.distributed

import org.jetbrains.kotlinx.lincheck.Options
import org.jetbrains.kotlinx.lincheck.chooseSequentialSpecification

enum class MessageOrder {
    SYNCHRONOUS,
    CASUAL_CONSISTENCY,
    FIFO,
    ASYNCHRONOUS
}

class DistributedOptions : Options<DistributedOptions,
        DistributedCTestConfiguration>() {
    var networkReliability: Double = 1.0
    var messageOrder: MessageOrder = MessageOrder.SYNCHRONOUS
    var maxNumberOfFailedNodes: Int = 0
    var supportRecovery: Boolean = true
    var invocationsPerIteration: Int = DistributedCTestConfiguration.DEFAULT_INVOCATIONS
    var addWaits : Boolean = true

    override fun createTestConfigurations(testClass: Class<*>): DistributedCTestConfiguration {
        return DistributedCTestConfiguration(testClass, iterations, threads,
                actorsPerThread, executionGenerator,
                verifier, invocationsPerIteration, addWaits, networkReliability,
                messageOrder, maxNumberOfFailedNodes, supportRecovery,
                requireStateEquivalenceImplementationCheck, minimizeFailedScenario,
                chooseSequentialSpecification(sequentialSpecification, testClass))
    }

    fun addWaits(value: Boolean): DistributedOptions {
        addWaits = value
        return this
    }


    fun networkReliability(networkReliability: Double): DistributedOptions {
        this.networkReliability = networkReliability
        return this
    }

    fun messageOrder(messageOrder: MessageOrder): DistributedOptions {
        this.messageOrder = messageOrder
        return this
    }

    fun maxNumberOfFailedNodes(maxNumOfFailedNodes: Int): DistributedOptions {
        if (maxNumOfFailedNodes > threads) {
            throw IllegalArgumentException("Maximum number of failed nodes " +
                    "is more than total number of nodes")
        }
        this.maxNumberOfFailedNodes = maxNumOfFailedNodes
        return this
    }

    fun supportRecovery(supportRecovery: Boolean): DistributedOptions {
        this.supportRecovery = true
        return this
    }

    fun invocationsPerIteration(invocations: Int): DistributedOptions {
        this.invocationsPerIteration = invocations
        return this
    }
}