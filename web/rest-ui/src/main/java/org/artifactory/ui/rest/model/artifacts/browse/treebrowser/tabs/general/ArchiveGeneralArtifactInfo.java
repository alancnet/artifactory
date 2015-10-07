package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general;

import org.artifactory.rest.common.service.IgnoreSpecialFields;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonFilter;

/**
 * @author Chen Keinan
 */
@JsonTypeName("archive")
@JsonFilter("exclude fields")
@IgnoreSpecialFields(value = {"repoKey", "path"})
public class ArchiveGeneralArtifactInfo extends FileGeneralArtifactInfo {
}
