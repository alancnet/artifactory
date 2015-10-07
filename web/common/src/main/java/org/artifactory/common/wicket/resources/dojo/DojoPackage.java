package org.artifactory.common.wicket.resources.dojo;

import org.artifactory.common.wicket.contributor.ResourcePackage;

/**
 * @author Yoav Aharoni
 */
public class DojoPackage extends ResourcePackage {
    public DojoPackage() {
        // add config
        addJavaScriptTemplate();

        // add dojo js
        //if (isDebug()) {
        addJavaScript("dojo/dojo.js.uncompressed.js");
        addJavaScript("dojo/artifactory-dojo.js.uncompressed.js");
        //} else {
        //    addJavaScript("dojo/dojo.js");
        //    addJavaScript("dojo/artifactory-dojo.js");
        //}

        // add themes
        addCss("dojo/resources/dojo.css");
        addCss("dijit/themes/tundra/tundra.css");
    }

    public boolean isDebug() {
        return false;
        //        return Application.get().getDebugSettings().isAjaxDebugModeEnabled();
    }
}
