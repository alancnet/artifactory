package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.recalculateindex;

import org.artifactory.addon.AddonsManager;
import org.artifactory.api.context.ContextHelper;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * @author Chen Keinan
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = GemsIndexCalculator.class, name = "Gems"),
        @JsonSubTypes.Type(value = NpmIndexCalculator.class, name = "Npm"),
        @JsonSubTypes.Type(value = DebianIndexCalculator.class, name = "Debian"),
        @JsonSubTypes.Type(value = YumIndexCalculator.class, name = "YUM"),
        @JsonSubTypes.Type(value = NuGetIndexCalculator.class, name = "NuGet"),
        @JsonSubTypes.Type(value = PypiIndexCalculator.class, name = "Pypi"),
        @JsonSubTypes.Type(value = BowerIndexCalculator.class, name = "Bower")})
public abstract class BaseIndexCalculator {

    protected AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);

    private String repoKey;

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public abstract void calculateIndex() throws Exception;

}
