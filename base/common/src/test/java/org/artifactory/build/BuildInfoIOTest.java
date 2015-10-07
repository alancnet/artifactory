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

package org.artifactory.build;

import org.artifactory.api.jackson.JacksonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.jfrog.build.api.Build;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Different parsing\generation tests for the build info JSON
 *
 * @author Noam Y. Tenne
 */
@Test
public class BuildInfoIOTest {

    /**
     * Generate a simple object and make sure it has no null fields
     */
    public void generateFromObject() throws IOException {
        Build build = new Build();
        StringWriter out = new StringWriter();
        JsonGenerator generator = JacksonFactory.createJsonGenerator(out);
        generator.writeObject(build);
        String result = out.getBuffer().toString();

        System.out.println("result = " + result);

        Assert.assertFalse(result.contains("null"), "Result should not contain null fields");
    }
}