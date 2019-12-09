package org.jetbrains.kotlinx.lincheck.test;

/*
 * #%L
 * Lincheck
 * %%
 * Copyright (C) 2015 - 2018 Devexperts, LLC
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

import org.jctools.queues.atomic.*;
import org.jetbrains.annotations.*;
import org.jetbrains.kotlinx.lincheck.*;
import org.jetbrains.kotlinx.lincheck.annotations.Operation;
import org.jetbrains.kotlinx.lincheck.annotations.*;
import org.jetbrains.kotlinx.lincheck.strategy.stress.*;
import org.jetbrains.kotlinx.lincheck.verifier.*;
import org.junit.*;

import java.util.*;

@OpGroupConfig(name = "producer", nonParallel = true)
@OpGroupConfig(name = "consumer", nonParallel = true)
@StressCTest(iterations = 50, actorsPerThread = 3)
public class NonParallelOpGroupTest extends VerifierState {
    private SpscLinkedAtomicQueue<Integer> queue = new SpscLinkedAtomicQueue<>();

    @Operation(group = "producer")
    public void offer(Integer x) {
        queue.offer(x);
    }

    @Operation(group = "consumer")
    public Integer poll() {
        return queue.poll();
    }

    @NotNull
    @Override
    protected Object extractState() {
        List<Integer> elements = new ArrayList<>();
        while (true) {
            Integer el = poll();
            if (el == null) break;
            elements.add(el);
        }
        return elements;
    }

    @Test
    public void test() {
        LinChecker.check(NonParallelOpGroupTest.class);
    }
}
