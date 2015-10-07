package org.artifactory.storage.db.build.service;

import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.artifactory.api.build.BuildRunComparators;
import org.artifactory.build.BuildRun;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * Tests the behavior of {@link org.artifactory.api.build.BuildRunComparators} class factory
 */
@Test
public class BuildRunComparatorsTest {

    public void testDateComparator() throws Exception {
        Calendar instance1 = Calendar.getInstance();
        instance1.set(2014, Calendar.JANUARY, 1);
        Calendar instance2 = Calendar.getInstance();
        instance2.set(2014, Calendar.FEBRUARY, 1);

        BuildRun b1 = new BuildRunImpl("Alice", "1", instance1.getTime());
        BuildRun b2 = new BuildRunImpl("Bob", "2", instance2.getTime());

        Comparator<BuildRun> dateComparator = BuildRunComparators.getBuildStartDateComparator();
        Assert.assertEquals(dateComparator.compare(b1, b2), -1);
        Assert.assertEquals(dateComparator.compare(b2, b1), 1);
        Assert.assertEquals(dateComparator.compare(b1, b1), 0);
    }

    public void testNumberComparator() throws Exception {
        BuildRun b1 = new BuildRunImpl("Alice", "1", new Date());
        BuildRun b2 = new BuildRunImpl("Bob", "2", new Date());
        ArrayList<BuildRun> buildRuns = Lists.newArrayList(b1, b2);

        //Also implicit check of the comparator factory, in this case should be Number compare.
        Comparator<BuildRun> numberComparator = BuildRunComparators.getComparatorFor(buildRuns);
        Assert.assertEquals(numberComparator.compare(b1, b2), -1);
        Assert.assertEquals(numberComparator.compare(b2, b1), 1);
        Assert.assertEquals(numberComparator.compare(b1, b1), 0);
    }

    public void testStringComparator() throws Exception {
        BuildRun b1 = new BuildRunImpl("Alice", "11a", new Date());
        BuildRun b2 = new BuildRunImpl("Bob", "11b", new Date());
        ArrayList<BuildRun> buildRuns = Lists.newArrayList(b1, b2);

        //Also implicit check of the comparator factory, in this case should be String compare.
        Comparator<BuildRun> numberComparator = BuildRunComparators.getComparatorFor(buildRuns);
        Assert.assertEquals(numberComparator.compare(b1, b2), -1);
        Assert.assertEquals(numberComparator.compare(b2, b1), 1);
        Assert.assertEquals(numberComparator.compare(b1, b1), 0);
    }
}