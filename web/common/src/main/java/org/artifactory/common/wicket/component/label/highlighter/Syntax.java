package org.artifactory.common.wicket.component.label.highlighter;

import com.google.common.collect.ImmutableMap;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.artifactory.mime.MimeType;

import static java.lang.String.format;

/**
 * @author Yoav Aharoni
 */
public enum Syntax {
    actionscript3("shBrushAS3.js"),
    shell("shBrushBash.js"),
    shellForDistManagement("shBrushBashForDistManagement.js"),
    coldfusion("shBrushColdFusion.js"),
    csharp("shBrushCSharp.js"),
    cpp("shBrushCpp.js"),
    css("shBrushCss.js"),
    delphi("shBrushDelphi.js"),
    pascal("shBrushDelphi.js"),
    diff("shBrushDiff.js"), // or patch
    erlang("shBrushErlang.js"),
    groovy("shBrushGroovy.js"),
    javascript("shBrushJScript.js"),
    java("patchedBrushJava.js"),
    javafx("shBrushJavaFX.js"),
    perl("shBrushPerl.js"),
    php("shBrushPhp.js"),
    plain("shBrushPlain.js"),
    powershell("shBrushPowerShell.js"),
    python("shBrushPython.js"),
    ruby("shBrushRuby.js"),
    scala("shBrushScala.js"),
    sql("shBrushSql.js"),
    vb("shBrushVb.js"),
    xml("shBrushXml.js");

    private static final ImmutableMap<String, Syntax> syntaxByName;

    static {
        ImmutableMap.Builder<String, Syntax> builder = ImmutableMap.builder();
        for (Syntax syntax : Syntax.values()) {
            builder.put(syntax.name(), syntax);
        }
        syntaxByName = builder.build();
    }

    private final ResourceReference jsReference;

    private Syntax(String jsFile) {
        String cssPath = format("resources/scripts/%s", jsFile);
        jsReference = new JavaScriptResourceReference(Syntax.class, cssPath);
    }

    String getBrush() {
        return name();
    }

    ResourceReference getJsReference() {
        return jsReference;
    }

    public static Syntax fromContentType(MimeType mimeType) {
        if (mimeType.getSyntax() != null) {
            return syntaxByName.get(mimeType.getSyntax());
        }
        return null;
    }
}
