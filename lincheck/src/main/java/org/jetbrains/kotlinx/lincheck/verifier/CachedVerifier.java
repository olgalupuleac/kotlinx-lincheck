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

package org.jetbrains.kotlinx.lincheck.verifier;

import org.jetbrains.kotlinx.lincheck.execution.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * This verifier cached the already verified results in a hash table,
 * and look into this hash table at first. In case of many invocations
 * with the same scenario, this optimization improves the verification
 * phase significantly.
 */
public abstract class CachedVerifier implements Verifier {
    private final ConcurrentHashMap<ExecutionScenario, Map<ExecutionResult, Boolean>> previousResults = new ConcurrentHashMap<>();

    @Override
    public final boolean verifyResults(ExecutionScenario scenario, ExecutionResult results) {
        Map<ExecutionResult, Boolean> previousScenarioResults = previousResults.computeIfAbsent(scenario, s -> new HashMap<>());
        return previousScenarioResults.computeIfAbsent(results, r -> verifyResultsImpl(scenario, r));
    }

    public abstract boolean verifyResultsImpl(ExecutionScenario scenario, ExecutionResult results);
}
