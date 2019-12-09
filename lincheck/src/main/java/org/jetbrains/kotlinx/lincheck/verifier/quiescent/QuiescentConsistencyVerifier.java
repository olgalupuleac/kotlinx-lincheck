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
package org.jetbrains.kotlinx.lincheck.verifier.quiescent;

import org.jetbrains.kotlinx.lincheck.*;
import org.jetbrains.kotlinx.lincheck.execution.*;
import org.jetbrains.kotlinx.lincheck.verifier.*;
import org.jetbrains.kotlinx.lincheck.verifier.linearizability.*;

import java.util.*;

public class QuiescentConsistencyVerifier extends CachedVerifier {
    private final LinearizabilityVerifier linearizabilityVerifier;

    public QuiescentConsistencyVerifier(Class<?> sequentialSpecification) {
        this.linearizabilityVerifier = new LinearizabilityVerifier(sequentialSpecification);
    }

    private static ExecutionScenario convertScenario(ExecutionScenario scenario) {
        List<List<Actor>> newParallelExecution = convertAccordingToScenario(scenario.parallelExecution, scenario.parallelExecution);
        return new ExecutionScenario(scenario.initExecution, newParallelExecution, scenario.postExecution);
    }

    private static <T> List<List<T>> convertAccordingToScenario(List<List<Actor>> parallelExecution, List<List<T>> toConvert) {
        List<List<T>> res = new ArrayList<>();
        for (int t = 0; t < parallelExecution.size(); t++)
            res.add(new ArrayList<>());
        for (int t = 0; t < parallelExecution.size(); t++) {
            int nActors = parallelExecution.get(t).size();
            for (int i = 0; i < nActors; i++) {
                Actor actor = parallelExecution.get(t).get(i);
                T val = toConvert.get(t).get(i);
                if (isQuiescentConsistent(actor) && nActors > 1)
                    res.add(Collections.singletonList(val));
                else
                    res.get(t).add(val);
            }
        }
        return res;
    }

    private static boolean isQuiescentConsistent(Actor actor) {
        return actor.getMethod().isAnnotationPresent(QuiescentConsistent.class);
    }

    @Override
    public boolean verifyResultsImpl(ExecutionScenario scenario, ExecutionResult results) {
        return linearizabilityVerifier.verifyResultsImpl(convertScenario(scenario),
            new ExecutionResult(
                results.initResults,
                convertAccordingToScenario(scenario.parallelExecution, results.parallelResults),
                results.postResults
            )
        );
    }

    @Override
    public void checkStateEquivalenceImplementation() {
        linearizabilityVerifier.checkStateEquivalenceImplementation();
    }
}
