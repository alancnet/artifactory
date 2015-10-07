/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
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

package org.artifactory.storage.fs.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * @author Yoav Landman
 */
class ThreadDumper {

    private static final Logger log = LoggerFactory.getLogger(ThreadDumper.class);

    private static final String JAVA6_FIND_DEADLOCKS_METHOD_NAME = "findDeadlockedThreads";
    private static final String JAVA5_FIND_DEADLOCKS_METHOD_NAME = "findMonitorDeadlockedThreads";
    private static String TAB = "    ";

    private ThreadMXBean tmbean;
    //Default for JDK 6+ VM

    //Whether can dump ownable synchronizer locks
    private boolean canDumpSyncLocks = true;

    private boolean ready;


    public ThreadDumper() {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            tmbean = ManagementFactory
                    .newPlatformMXBeanProxy(server, ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
            ObjectName oname = new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME);
            //See if we can dump deadlock info by testing if the method exists
            MBeanOperationInfo[] mopis = server.getMBeanInfo(oname).getOperations();

            //Look for the java 6+ method
            boolean found = false;
            for (MBeanOperationInfo op : mopis) {
                if (op.getName().equals(JAVA6_FIND_DEADLOCKS_METHOD_NAME)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                //Fallback to java 5 - monitor locks cannot be dumped
                canDumpSyncLocks = false;
            }
        } catch (Exception e) {
            log.error("Could not initialize the debugging thread dumper.", e);
            return;
        }
        ready = true;
    }


    /**
     * Prints out the thread dump information
     */
    public CharSequence dumpThreads() {
        assertReady();
        StringBuilder dump = new StringBuilder();
        if (canDumpSyncLocks && tmbean.isObjectMonitorUsageSupported() && tmbean.isSynchronizerUsageSupported()) {
            //Lock info if both object monitor usage and synchronizer usage are supported (java 6)
            dump.append("Full Java thread dump with locks info").append('\n');
            ThreadInfo[] tinfos = tmbean.dumpAllThreads(true, true);
            for (ThreadInfo ti : tinfos) {
                printThreadInfo(ti, dump);
                LockInfo[] syncs = ti.getLockedSynchronizers();
                printLockInfo(syncs, dump);
            }
        } else {
            //Java 5
            dump.append("Full Java thread dump").append('\n');
            long[] tids = tmbean.getAllThreadIds();
            ThreadInfo[] tinfos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
            for (ThreadInfo ti : tinfos) {
                printThreadInfo(ti, dump);
            }
        }
        dump.append('\n');
        return dump;
    }

    /**
     * Checks if any threads are deadlocked. If any, print the thread dump information.
     */
    public boolean findDeadlock(StringBuilder dump) {
        assertReady();
        long[] tids;
        if (canDumpSyncLocks && tmbean.isSynchronizerUsageSupported()) {
            tids = tmbean.findDeadlockedThreads();
            if (tids == null) {
                return false;
            }
            dump.append("Deadlock found:-").append('\n');
            ThreadInfo[] infos = tmbean.getThreadInfo(tids, true, true);
            for (ThreadInfo ti : infos) {
                printThreadInfo(ti, dump);
                printLockInfo(ti.getLockedSynchronizers(), dump);
                dump.append("");
            }
        } else {
            tids = tmbean.findMonitorDeadlockedThreads();
            if (tids == null) {
                return false;
            }
            ThreadInfo[] infos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
            for (ThreadInfo ti : infos) {
                printThreadInfo(ti, dump);
                printMonitorInfo(ti.getLockedMonitors(), dump);
            }
        }
        return true;
    }

    private void printThreadInfo(ThreadInfo ti, StringBuilder dump) {
        // print thread information
        printThread(ti, dump);

        // print stack trace with locks
        StackTraceElement[] stacktrace = ti.getStackTrace();
        MonitorInfo[] monitors = ti.getLockedMonitors();
        for (int i = 0; i < stacktrace.length; i++) {
            StackTraceElement ste = stacktrace[i];
            dump.append(TAB).append("at ").append(ste.toString()).append('\n');
            for (MonitorInfo mi : monitors) {
                if (mi.getLockedStackDepth() == i) {
                    dump.append(TAB).append("  - locked ").append(mi).append('\n');
                }
            }
        }
        dump.append("");
    }

    private void printThread(ThreadInfo ti, StringBuilder dump) {
        StringBuilder sb = new StringBuilder("\"" + ti.getThreadName() + "\"" + " Id="
                + ti.getThreadId() + " in " + ti.getThreadState());
        if (ti.getLockName() != null) {
            sb.append(" on lock=").append(ti.getLockName());
        }
        if (ti.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (ti.isInNative()) {
            sb.append(" (running in native)");
        }
        dump.append(sb).append('\n');
        if (ti.getLockOwnerName() != null) {
            dump.append(TAB).append(" owned by ").append(ti.getLockOwnerName()).append(" Id=")
                    .append(ti.getLockOwnerId()).append('\n');
        }
    }

    private void printLockInfo(LockInfo[] locks, StringBuilder dump) {
        dump.append(TAB).append("Locked synchronizers: count = ").append(locks.length).append('\n');
        for (LockInfo li : locks) {
            dump.append(TAB).append("  - ").append(li).append('\n');
        }
        dump.append('\n');
    }

    private void printMonitorInfo(MonitorInfo[] monitors, StringBuilder dump) {
        dump.append(TAB).append("Locked monitors: count = ").append(monitors.length).append('\n');
        for (MonitorInfo mi : monitors) {
            dump.append(TAB).append("  - ").append(mi).append(" locked at ").append('\n');
            dump.append(TAB).append("      ").append(mi.getLockedStackDepth()).append(" ")
                    .append(mi.getLockedStackFrame()).append('\n');
        }
    }

    private void assertReady() {
        if (!ready) {
            throw new IllegalStateException("Thread dumper was not initialize properly.");
        }
    }
}