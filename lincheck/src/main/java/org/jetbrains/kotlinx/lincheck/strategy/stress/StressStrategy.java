package org.jetbrains.kotlinx.lincheck.strategy.stress;

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

import org.jetbrains.kotlinx.lincheck.*;
import org.jetbrains.kotlinx.lincheck.execution.*;
import org.jetbrains.kotlinx.lincheck.runner.*;
import org.jetbrains.kotlinx.lincheck.strategy.*;

import java.util.*;
import java.util.concurrent.atomic.*;

/**
 * This strategy
 */
public class StressStrategy extends Strategy {
    private static final int MAX_WAIT = 1000;
    private final Random random = new Random(0);

    private final int invocations;
    private final Runner runner;

    private final List<int[]> waits;
    private final AtomicInteger uninitializedThreads = new AtomicInteger(0); // for threads synchronization

    public StressStrategy(Class<?> testClass, ExecutionScenario scenario,
                          StressCTestConfiguration testCfg, Reporter reporter) {
        super(scenario, reporter);
        this.invocations = testCfg.invocationsPerIteration;
        // Create waits if needed
        waits = testCfg.addWaits ? new ArrayList<>() : null;
        if (testCfg.addWaits) {
            for (List<Actor> actorsForThread : scenario.parallelExecution) {
                waits.add(new int[actorsForThread.size()]);
            }
        }
        // Create runner
        runner = new ParallelThreadsRunner(scenario, this, testClass, waits) {
            @Override
            public void onStart(int iThread) {
                super.onStart(iThread);
                uninitializedThreads.decrementAndGet(); // this thread has finished initialization
                while (uninitializedThreads.get() != 0) {} // wait for other threads to start
            }
        };
    }

    @Override
    public List<InvocationResult> run() throws InterruptedException {
        try {
            List<InvocationResult> results = new ArrayList<>();
            // Run invocations
            for (int invocation = 1; invocation <= invocations; invocation++) {
                // Specify waits if needed
                if (waits != null) {
                    int maxWait = (int) ((float) invocation * MAX_WAIT / invocations) + 1;
                    for (int[] waitsForThread : waits) {
                        for (int i = 0; i < waitsForThread.length; i++) {
                            waitsForThread[i] = random.nextInt(maxWait);
                        }
                    }
                }
                uninitializedThreads.set(scenario.parallelExecution.size()); // reinit synchronization
                InvocationResult r = runner.run();
                results.add(r);
                if (r.isError()) break;
            }
            return results;
        } finally {
            runner.close();
        }
    }
}
