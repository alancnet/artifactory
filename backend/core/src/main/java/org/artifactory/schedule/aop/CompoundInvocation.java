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

package org.artifactory.schedule.aop;

import com.google.common.collect.ImmutableList;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Used mainly for stacking async invocations on the same async thread (shared asyncs).
 * <p/>
 * E.g. indexing a remotely downloaded jar and updating its download count.
 *
 * @author Yoav Landman
 */
public class CompoundInvocation implements MethodInvocation {

    private List<MethodInvocation> invocations = new ArrayList<>();
    private MethodInvocation currentInvocation;
    private AsyncAdvice advice;

    void setAdvice(AsyncAdvice advice) {
        this.advice = advice;
    }

    void add(MethodInvocation invocation) {
        invocations.add(invocation);
    }

    @Override
    public Object proceed() throws Throwable {
        List<MethodInvocation> tmpInvocations = ImmutableList.copyOf(invocations);
        invocations.clear();
        for (MethodInvocation invocation : tmpInvocations) {
            currentInvocation = invocation;
            advice.doInvoke(invocation);
        }
        return null;
    }

    public ImmutableList<MethodInvocation> getInvocations() {
        return ImmutableList.copyOf(invocations);
    }

    /**
     * @return Returns the latest executed method (useful when something bad happens)
     */
    Method getLatestMethod() {
        return currentInvocation != null ? currentInvocation.getMethod() : null;
    }

    boolean isEmpty() {
        return invocations.isEmpty();
    }

    void clear() {
        invocations.clear();
        currentInvocation = null;
    }

    @Override
    public Object getThis() {
        throw new UnsupportedOperationException("Not supported in compound invocation");
    }

    @Override
    public AccessibleObject getStaticPart() {
        throw new UnsupportedOperationException("Not supported in compound invocation");
    }

    @Override
    public Method getMethod() {
        throw new UnsupportedOperationException("Not supported in compound invocation");
    }

    @Override
    public Object[] getArguments() {
        throw new UnsupportedOperationException("Not supported in compound invocation");
    }
}