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

package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.zipentry;

import org.apache.wicket.markup.html.panel.Panel;
import org.artifactory.common.wicket.component.LabeledValue;
import org.artifactory.common.wicket.component.border.fieldset.FieldSetBorder;
import org.artifactory.fs.ZipEntryInfo;

/**
 * Displays general item information. Placed inside the general info panel when viewing archived item.
 *
 * @author Yossi Shaul
 */
public class ZipEntryPanel extends Panel {

    public ZipEntryPanel(String id, ZipEntryInfo zipEntry) {
        super(id);

        FieldSetBorder infoBorder = new FieldSetBorder("infoBorder");
        add(infoBorder);

        infoBorder.add(new LabeledValue("name", "Name: ", zipEntry.getName()));
        infoBorder.add(new LabeledValue("path", "Path: ", zipEntry.getPath()));
        infoBorder.add(new LabeledValue("size", "Size: ", zipEntry.getSize() + ""));
        infoBorder.add(new LabeledValue("compressedSize", "Compressed Size: ", zipEntry.getCompressedSize() + ""));
        infoBorder.add(new LabeledValue("time", "Modification Time: ", zipEntry.getTime() + ""));
        infoBorder.add(new LabeledValue("crc", "CRC: ", zipEntry.getCrc() + ""));
        infoBorder.add(new LabeledValue("comment", "Comment: ", zipEntry.getComment()));
    }

}