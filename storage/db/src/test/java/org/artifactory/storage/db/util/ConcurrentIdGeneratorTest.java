/*
* Artifactory is a binaries repository manager.
* Copyright (C) 2013 JFrog Ltd.
*
* Artifactory is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Artifactory is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.artifactory.storage.db.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.testng.Assert.assertTrue;

/**
* @author mamo
*/
public class ConcurrentIdGeneratorTest extends IdGeneratorBaseTest {
    private static final Logger log = LoggerFactory.getLogger(ConcurrentIdGeneratorTest.class);

    // Use only prime numbers to make sure things don't overlaps
    public static final int POOL_SIZE = 2;
    public static final int COUNT = 3 * POOL_SIZE;
    public static final int STEP = 2;
    public static final int BLOCKS_INC = 2;

    static {
        System.setProperty(ConstantValues.dbIdGeneratorFetchAmount.getPropertyName(), String.valueOf(STEP));
    }

    @Test
    public void concurrentIdGenerator() throws ExecutionException, InterruptedException {
        ExecuteManyIdNext executeManyIdNext = new ExecuteManyIdNext(idGenerator, getCurrentInTableId());
        executeManyIdNext.doIt();
        executeManyIdNext.checkUniqueness();
        executeManyIdNext.checkContinuity();
        executeManyIdNext.checkIncrementDelta(getCurrentInTableId(), STEP);
    }

    public interface ContextProvider {
        ArtifactoryContext getContext();
    }

    public static class ExecuteManyIdNext {
        final ContextProvider contextProvider;
        IdGenerator idGenerator;
        final long startId;
        private final ExecutorService executorService;
        private final ExecutorCompletionService<Map<Long, Long>> completionService;
        private final Map<Long, Long> indices;
        private long skipDiffs;

        ExecuteManyIdNext(IdGenerator idGenerator, long startId) {
            this.idGenerator = idGenerator;
            this.startId = startId;
            this.contextProvider = null;
            executorService = Executors.newFixedThreadPool(POOL_SIZE);
            completionService = new ExecutorCompletionService<>(executorService);
            indices = new ConcurrentHashMap<>();
        }

        public ExecuteManyIdNext(ContextProvider contextProvider, long startId) {
            this.startId = startId;
            this.idGenerator = null;
            this.contextProvider = contextProvider;
            executorService = Executors.newFixedThreadPool(POOL_SIZE);
            completionService = new ExecutorCompletionService<>(executorService);
            indices = new ConcurrentHashMap<>();
        }

        public void doIt() {
            for (int i = 0; i < COUNT; i++) {
                completionService.submit(new CallNextId(this));
            }
        }

        public void checkUniqueness() throws InterruptedException, ExecutionException {
            for (int i = 0; i < COUNT; i++) {
                Map<Long, Long> nextIds = completionService.take().get();
                for (Long nextId : nextIds.keySet()) {
                    assertTrue(indices.put(nextId, nextId) == null, "Index should be unique " + nextId);
                }
            }

        }

        public long checkContinuity() {
            skipDiffs = 0L;
            int skips = 0;
            ImmutableList<Long> sortedIndices = Ordering.natural().immutableSortedCopy(indices.keySet());
            for (int i = 1; i < sortedIndices.size(); i++) {
                long diff = sortedIndices.get(i) - sortedIndices.get(i - 1);
                if (diff != 1) {
                    log.error("skip index #{}: {}", i, diff);
                    skips++;
                    skipDiffs += diff;
                }
            }
            if (skips > 2) {
                Assert.fail("too many skips in unique ids");
            }
            return skipDiffs;
        }

        /**
         * Let ACTUAL be the total number of ids, provided by the database
         * Let BOTTOM be the total number of ids, requested by the test.
         * Let STEP be the size of single block (of idis), provided by the database
         * The test ensure that ACTUAL minus BOTTOM is less than STEP.
         * <p/>
         * Note that the skipDiffs is the number of ids, requested by Artifactory during the test, those requests are
         * included in the ACTUAL but not in the BOTTOM therefore we should consider the in the skipDiffs calculation:
         * <p/>
         * idis provided by the database and the
         *
         * @param currentInTableId
         * @param acceptDelta
         */
        public void checkIncrementDelta(Long currentInTableId, int acceptDelta) {
            // calculate the idis provided by the database during the test
            long actual = currentInTableId - startId;
            // calculate the number of idis requested by the test
            long bottom = (long) COUNT * (long) BLOCKS_INC;
            long delta = actual - bottom;
            String msg = "Actual=" + actual + " bottom=" + bottom + " delta=" + delta + " acceptDelta=" + acceptDelta + " skips=" + skipDiffs;
            log.info(msg);
            assertTrue(delta >= 0L && delta < (acceptDelta + skipDiffs),
                    "Index should have been incremented. Got " + msg);
        }
    }

    static class CallNextId implements Callable<Map<Long, Long>> {
        private final ExecuteManyIdNext parent;

        CallNextId(ExecuteManyIdNext parent) {
            this.parent = parent;
        }

        @Override
        public Map<Long, Long> call() throws Exception {
            Map<Long, Long> result = Maps.newHashMap();
            try {
                IdGenerator localIdGenerator;
                if (parent.contextProvider != null) {
                    ArtifactoryContext context = parent.contextProvider.getContext();
                    ArtifactoryHome.bind(context.getArtifactoryHome());
                    localIdGenerator = context.beanForType(IdGenerator.class);
                } else {
                    localIdGenerator = parent.idGenerator;
                }
                for (int j = 0; j < BLOCKS_INC; j++) {
                    long nextId = localIdGenerator.nextId();
                    if (nextId % 100_000 == 0) {
                        log.info("nextId = " + nextId);
                    }

                    result.put(nextId, nextId);
                }
            } finally {
                if (parent.contextProvider != null) {
                    ArtifactoryHome.unbind();
                }
            }
            return result;
        }
    }
}
