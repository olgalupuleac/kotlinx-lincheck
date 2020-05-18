package org.jetbrains.kotlinx.lincheck.test.distributed

import org.jetbrains.kotlinx.lincheck.LinChecker
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.distributed.DistributedOptions
import org.jetbrains.kotlinx.lincheck.distributed.Environment
import org.jetbrains.kotlinx.lincheck.distributed.MessageOrder
import org.jetbrains.kotlinx.lincheck.distributed.Process
import org.jetbrains.kotlinx.lincheck.test.AbstractLincheckTest
import org.junit.Test
import java.lang.Exception
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

sealed class CommandResult()

class GetResult(val result: Int?) : CommandResult()
object PutResult : CommandResult()
class ContainsResult(val result: Boolean) : CommandResult()

class KVStorageCentralSimple(private val environment: Environment) : Process {
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    @Volatile
    private var command: CommandResult? = null
    private val storage = HashMap<Int, Int>()

    override fun onMessage(srcId: Int, message: String) {
        println("on message: ${environment.processId} $message")
        val tokens = message.split(" ")
        if (environment.processId == 1) {
            when (tokens[0]) {
                "contains" -> environment.send(srcId, "contains ${storage
                        .contains(tokens[1].toInt())}")
                "get" -> environment.send(srcId, "get ${storage[tokens[1].toInt()]}")
                "put" -> {
                    storage[tokens[1].toInt()] = tokens[2].toInt()
                    environment.send(srcId, "put")
                }
            }
        } else {
            lock.withLock {
                println("lock ${environment.processId} inner lock")
                when (tokens[0]) {
                    "contains" ->
                        command = ContainsResult(tokens[1].toBoolean())
                    "get" ->
                        command = GetResult(tokens[1].toIntOrNull())
                    "put" -> command = PutResult
                }
                condition.signal()
            }.also { println("lock ${environment.processId} inner unlock") }
        }
    }

    @Operation
    fun contains(key: Int): Boolean {
        println("${environment.processId}: contains $key")
        if (environment.processId == 1) {
            return storage.contains(key)
        }
        command = null
        environment.send(1, "contains $key")
        var res: Boolean = false
        lock.withLock {
            println("lock ${environment.processId} inner lock await")
            while (command == null) condition.await()
            res = (command as ContainsResult).result
        }.also { println("lock ${environment.processId} inner unlock") }
        return res
    }

    @Operation
    fun put(key: Int, value: Int) {
        println("${environment.processId}: put $key $value")
        if (environment.processId == 1) {
            storage[key] = value
            return
        }
        command = null
        environment.send(1, "put $key $value")
        lock.withLock {
            while (command == null) condition.await()
            assert(command is PutResult)
        }
    }

    @Operation
    fun get(key: Int): Int? {
        println("${environment.processId}: get $key")
        if (environment.processId == 1) {
            return storage[key]
        }
        command = null
        environment.send(1, "get $key")
        var res: Int? = null
        lock.withLock {
            while (command == null) condition.await()
            res = (command as GetResult).result
        }
        return res
    }
}

class SingleNode() {
    private val storage = HashMap<Int, Int>()
    @Operation
    fun contains(key: Int): Boolean {
        return storage.contains(key)
    }

    @Operation
    fun put(key: Int, value: Int) {
        storage[key] = value
    }

    @Operation
    fun get(key: Int): Int? {
        return storage[key]
    }
}

class TestClass() : AbstractLincheckTest() {
    @Test
    fun testSimple() {
            LinChecker.check(KVStorageCentralSimple::class
                    .java, DistributedOptions().requireStateEquivalenceImplCheck
            (false).sequentialSpecification(SingleNode::class.java).threads
            (4).invocationsPerIteration(10).iterations(100))

    }

    @Test
    fun testDelay() {
        LinChecker.check(KVStorageCentralSimple::class
                .java, DistributedOptions().requireStateEquivalenceImplCheck
        (false).sequentialSpecification(SingleNode::class.java).threads
        (4).invocationsPerIteration(10).iterations(100).delay(2))

    }

    @Test
    fun testAsynchronous() {
        LinChecker.check(KVStorageCentralSimple::class
                .java, DistributedOptions().requireStateEquivalenceImplCheck
        (false).sequentialSpecification(SingleNode::class.java).threads
        (4).invocationsPerIteration(10).iterations(100).delay(2).messageOrder
        (MessageOrder.ASYNCHRONOUS))
    }

    @Test
    fun testNetworkUnreliableShouldFail() {
        LinChecker.check(KVStorageCentralSimple::class
                .java, DistributedOptions().requireStateEquivalenceImplCheck
        (false).sequentialSpecification(SingleNode::class.java).threads
        (4).invocationsPerIteration(10).iterations(100).networkReliability(0.8))
    }
}