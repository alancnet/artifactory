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

package org.artifactory.rest.resource.traffic;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.artifactory.api.rest.constant.TrafficRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.list.StringList;
import org.artifactory.traffic.TrafficService;
import org.artifactory.traffic.TransferUsage;
import org.artifactory.traffic.entry.TrafficEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.List;

/**
 * @author Noam Tenne
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(TrafficRestConstants.PATH_ROOT)
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
public class TrafficResource {

    @Context
    private HttpServletResponse httpResponse;

    @Autowired
    private TrafficService trafficService;

    @GET
    @Path(TrafficRestConstants.STREAM_ROOT)
    @Produces(MediaType.TEXT_PLAIN)
    public String getTrafficLogFilesStream(
            @QueryParam(TrafficRestConstants.PARAM_START_DATE) long startLong,
            @QueryParam(TrafficRestConstants.PARAM_END_DATE) long endLong) throws IOException {
        Calendar from = Calendar.getInstance();
        from.setTimeInMillis(startLong);
        Calendar to = Calendar.getInstance();
        to.setTimeInMillis(endLong);
        validateDateRange(from, to);
        List<TrafficEntry> trafficEntryList = trafficService.getEntryList(from, to);
        writeEntriesToStream(trafficEntryList);

        return "";
    }

    @GET
    @Path(TrafficRestConstants.PATH_FILTER)
    @Produces(MediaType.APPLICATION_JSON)
    public TransferUsage getTransferUsage(
            @QueryParam(TrafficRestConstants.PARAM_START_DATE) long startLong,
            @QueryParam(TrafficRestConstants.PARAM_END_DATE) long endLong,
            @QueryParam(TrafficRestConstants.PARAM_FILTER) StringList ipsToFilter) throws IOException {
        Calendar from = Calendar.getInstance();
        from.setTimeInMillis(startLong);
        Calendar to = Calendar.getInstance();
        to.setTimeInMillis(endLong);
        validateDateRange(from, to);
        TransferUsage transferUsage = trafficService.getUsageWithFilter(from, to, ipsToFilter);
        return transferUsage;
    }

    private void validateDateRange(Calendar startDate, Calendar endDate) {
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("The start date cannot be later than the end date");
        }
    }

    private void writeEntriesToStream(List<TrafficEntry> trafficEntryList) throws IOException {
        if (!trafficEntryList.isEmpty()) {
            try (OutputStreamWriter writer = new OutputStreamWriter(httpResponse.getOutputStream(), Charsets.UTF_8)) {
                for (TrafficEntry trafficEntry : trafficEntryList) {
                    String lineToWrite = (trafficEntry.toString() + "\n");
                    IOUtils.write(lineToWrite, writer);
                }
            }
        }
    }
}