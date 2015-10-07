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

package org.artifactory.ivy;

import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.parser.ParserSettings;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorParser;
import org.apache.ivy.plugins.repository.BasicResource;
import org.apache.ivy.plugins.repository.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;

/**
 * Ivy parser. validates the descriptor and parses it into an object.
 *
 * @author Tomer Cohen
 */
public class IvyParser extends XmlModuleDescriptorParser {
    private final ParserSettings settings = new IvySettings();

    public ModuleDescriptor getModuleDescriptorForStringContent(InputStream input, long contentLength)
            throws IOException, ParseException {
        Resource resource = new BasicResource("ivyBasicResource", true, contentLength, new Date().getTime(), true);
        return getModuleDescriptor(settings, input, resource, true);
    }

    public ModuleDescriptor getModuleDescriptor(ParserSettings settings, InputStream input,
            Resource res, boolean validate) throws ParseException, IOException {
        Parser parser = newParser(settings);
        parser.setValidate(validate);
        parser.setResource(res);
        parser.setInput(input);
        parser.parse();
        return parser.getModuleDescriptor();
    }
}

