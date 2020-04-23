package org.jetbrains.kotlinx.lincheck.distributed

interface Process {
    fun onMessage(srcId : Int, message : String)
}