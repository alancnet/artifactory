package org.artifactory.schedule;

/**
 * @author Chen Keinan
 */
public enum ScheduleJobEnum {

    DUMMY_JOB ("org.artifactory.schedule.DummyJob"),
    ARCHIVE_INDEXER_JOB("org.artifactory.search.archive.ArchiveIndexerImpl$ArchiveIndexJob");

    public String jobName;

    ScheduleJobEnum(String keyName) {
        this.jobName = keyName;
         }
    }
