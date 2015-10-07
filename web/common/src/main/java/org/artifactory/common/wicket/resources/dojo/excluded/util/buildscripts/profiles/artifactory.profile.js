dependencies = {
    layers:  [
        {
            name: "artifactory-dojo.js",
            dependencies: [
                "dojo.parser",
                "dojo.dnd.Source",              // DragDropSelection, OrderedListPanel
                "dijit.layout.ContentPane",     // TreeBrowsePanel
                "dijit.layout.BorderContainer", // TreeBrowsePanel
                "dijit.Tooltip",                // HelpBubble
                "dijit.Menu",                   // ActionsMenuPanel
                "dijit.form.ComboBox",          // ComboBoxBehavior
                "dijit.form.FilteringSelect"    // FilteringSelectBehavior
            ]
        }
    ]
};