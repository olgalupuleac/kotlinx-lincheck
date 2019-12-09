package org.jetbrains.kotlinx.lincheck.strategy;

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
import org.jetbrains.kotlinx.lincheck.strategy.randomswitch.*;
import org.jetbrains.kotlinx.lincheck.strategy.stress.*;
import org.objectweb.asm.*;

import java.util.*;

/**
 * Implementation of this class describes how to run the generated execution.
 * <p>
 * Note that strategy could run execution several times. For strategy creating
 * {@link #createStrategy} method is used. It is impossible to add new strategy
 * without any code change.
 */
public abstract class Strategy {
    protected final ExecutionScenario scenario;
    protected final Reporter reporter;

    protected Strategy(ExecutionScenario scenario, Reporter reporter) {
        this.scenario = scenario;
        this.reporter = reporter;
    }

    public ClassVisitor createTransformer(ClassVisitor cv) {
        throw new UnsupportedOperationException(getClass() + " runner does not transform classes");
    }

    public boolean needsTransformation() {
        return false;
    }

    /**
     * Creates {@link Strategy} based on {@code testCfg} type.
     */
    public static Strategy createStrategy(CTestConfiguration testCfg, Class<?> testClass,
                                          ExecutionScenario scenario, Reporter reporter) {
        if (testCfg instanceof StressCTestConfiguration) {
            return new StressStrategy(testClass, scenario,
                (StressCTestConfiguration) testCfg, reporter);
        } else if (testCfg instanceof RandomSwitchCTestConfiguration) {
            return new RandomSwitchStrategy(testClass, scenario,
                (RandomSwitchCTestConfiguration) testCfg, reporter);
        }
        throw new IllegalArgumentException("Unknown strategy configuration type: " + testCfg.getClass());
    }

    public abstract List<InvocationResult> run() throws Exception;
}