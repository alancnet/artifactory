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

package org.artifactory.common.wicket.component.file.browser.panel;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.file.path.PathAutoCompleteTextField;
import org.artifactory.common.wicket.component.file.path.PathHelper;
import org.artifactory.common.wicket.component.file.path.PathMask;
import org.artifactory.common.wicket.component.links.BaseTitledLink;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.contributor.ResourcePackage;
import org.artifactory.common.wicket.model.DelegatedModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yoav Aharoni
 */
public class FileBrowserPanel extends BaseModalPanel {
    private PathHelper pathHelper;
    private String chRoot;

    private PathAutoCompleteTextField pathAutoCompleteTextField;
    private DropDownChoice breadcrumbs;
    private ListView filesList;

    protected FileBrowserPanel(IModel model, PathHelper pathHelper) {
        setWidth(570);
        setHeight(345);
        if (model != null) {
            setDefaultModel(model);
        }

        this.pathHelper = pathHelper;
        init();
    }

    protected void init() {
        setChRoot(pathHelper.getWorkingDirectoryPath());

        setOutputMarkupId(true);
        add(new AttributeModifier("class", "fileBrowser"));

        add(new ResourcePackage(FileBrowserPanel.class)
                .addJavaScript()
                .addCss("style/filebrowser.css")
        );

        // add the border
        TitledBorder border = new TitledBorder("border");
        add(border);

        // add component
        pathAutoCompleteTextField = new BrowserAutoCompleteTextField("fileAutocomplete");
        border.add(pathAutoCompleteTextField);

        DropDownChoice roots = new RootsDropDownChoice("roots");
        border.add(roots);

        breadcrumbs = new BreadCrumbsDropDownChoice("breadcrumbs");
        border.add(breadcrumbs);

        filesList = new FilesListView("filesList");
        border.add(filesList);

        // add init script
        Label label = new Label("initScript", new InitScriptModel());
        label.setEscapeModelStrings(false);
        add(label);

        // add  buttons
        border.add(new GotoParentButton("gotoParentButton"));
        add(new OkButton("ok"));
        add(new CancelButton("cancel"));
    }

    protected void onOkClicked(AjaxRequestTarget target) {
    }

    protected void onCancelClicked(AjaxRequestTarget target) {
    }

    protected void onFileSelected(File file, AjaxRequestTarget target) {
        if (file.isDirectory()) {
            // is folder
            if (getMask().includeFolders()) {
                setDefaultModelObject(file);
                onOkClicked(target);
            } else {
                setCurrentFolder(enforceRootFolder(file).getAbsolutePath());
                target.add(this);
            }
            return;
        }

        // is file
        setDefaultModelObject(file);
        onOkClicked(target);
    }

    @Override
    public String getTitle() {
        return getString("file.browser.title", null);
    }

    public PathMask getMask() {
        return pathAutoCompleteTextField.getMask();
    }

    public void setMask(PathMask mask) {
        pathAutoCompleteTextField.setMask(mask);
        filesList.setDefaultModelObject(pathHelper.getFiles("/", mask));
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public File getModelObject() {
        return (File) getDefaultModelObject();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setModelObject(File file) {
        setDefaultModelObject(file);
    }

    @SuppressWarnings({"unchecked", "UnusedDeclaration"})
    public IModel<File> getModel() {
        return (IModel<File>) getDefaultModel();
    }

    public void setChRoot(String chRoot) {
        if (chRoot == null) {
            final String uiRoot = ConstantValues.uiChroot.getString();
            if (StringUtils.isBlank(uiRoot)) {
                this.chRoot = null;
            } else {
                this.chRoot = pathHelper.getCanonicalPath(new File(uiRoot));
            }
        } else {
            this.chRoot = pathHelper.getCanonicalPath(new File(chRoot));
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public String getChRoot() {
        return chRoot;
    }

    public String getCurrentFolder() {
        return StringUtils.defaultString(pathHelper.getWorkingDirectoryPath(), "/");
    }

    public void setCurrentFolder(String root) {
        pathHelper.setWorkingDirectoryPath(root);
        filesList.setDefaultModelObject(pathHelper.getFiles("/", getMask()));
        breadcrumbs.setDefaultModelObject(new File(getCurrentFolder()));
    }

    private static class OkButton extends BaseTitledLink {
        private OkButton(String id) {
            super(id, "OK");
            add(new AttributeModifier("onclick", "FileBrowser.get().ok();"));
        }
    }

    private class CancelButton extends TitledAjaxLink {
        private CancelButton(String id) {
            super(id, "Cancel");
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            FileBrowserPanel.this.setDefaultModelObject(null);
            onCancelClicked(target);
        }
    }

    private class GotoParentButton extends AjaxLink {
        private GotoParentButton(String id) {
            super(id);
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            File currentFolder = new File(getCurrentFolder());
            File parentFolder = currentFolder.getParentFile();
            String root = chRoot;
            if (root == null) {
                root = new File("/").getAbsolutePath();
            }
            if (parentFolder == null || root.equals(currentFolder.getAbsolutePath())) {
                return;
            }

            setCurrentFolder(parentFolder.getAbsolutePath());
            FileBrowserPanel.this.setDefaultModelObject(parentFolder);
            target.add(FileBrowserPanel.this);
        }
    }

    private class FilesListView extends ListView<File> {
        private FilesListView(String id) {
            super(id, pathHelper.getFiles("/", getMask()));
        }

        @Override
        protected void populateItem(ListItem item) {
            File file = (File) item.getDefaultModelObject();
            item.add(new FileLink("fileNameLabel", file));
            item.add(new AttributeModifier("class", file.isDirectory() ? "folder" : "file"));

        }
    }

    private class FileLink extends Label {
        private FileLink(String id, final File file) {
            super(id, file.getName());

            add(new AttributeModifier("onclick",
                    "FileBrowser.get().onFileClick(this, event);"));

            if (file.isDirectory()) {
                add(new AjaxEventBehavior("ondblclick") {
                    @Override
                    protected void onEvent(AjaxRequestTarget target) {
                        setCurrentFolder(file.getAbsolutePath());
                        FileBrowserPanel.this.setDefaultModelObject(file);
                        target.add(FileBrowserPanel.this);
                    }
                });
            } else {
                add(new AjaxEventBehavior("ondblclick") {
                    @Override
                    protected void onEvent(AjaxRequestTarget target) {
                        onFileSelected(file, target);
                    }
                });
            }

        }
    }

    private class InitScriptModel extends AbstractReadOnlyModel {
        @Override
        public Object getObject() {
            return "new FileBrowser('" + getMarkupId() + "', '" +
                    pathAutoCompleteTextField.getMarkupId() + "');";
        }
    }

    private class BreadCrumbsDropDownChoice extends DropDownChoice<File> {
        private BreadCrumbsDropDownChoice(String id) {
            super(id, new Model<>(new File(getCurrentFolder())), new BreadCrumbsModel(),
                    new ChoiceRenderer<File>("name", "path"));

            add(new AjaxFormComponentUpdatingBehavior("onchange") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    File root = getModelObject();
                    setCurrentFolder(root.getAbsolutePath());
                    FileBrowserPanel.this.setDefaultModelObject(root);

                    target.add(FileBrowserPanel.this);
                }
            });
        }

        @SuppressWarnings({"RefusedBequest", "unchecked"})
        @Override
        protected void appendOptionHtml(AppendingStringBuffer buffer, File choice, int index, String selected) {
            IChoiceRenderer<? super File> renderer = getChoiceRenderer();
            Object objectValue = renderer.getDisplayValue(choice);
            String displayValue = "";
            if (objectValue != null) {
                IConverter converter = getConverter(objectValue.getClass());
                displayValue = converter
                        .convertToString(objectValue, getLocale());
            }

            if ("".equals(displayValue)) {
                displayValue = getString("file.browser.breadCrumbs.root", null);
            }
            buffer.append("\n<option ");
            if (isSelected(choice, index, selected)) {
                buffer.append("selected=\"selected\" ");
            }

            buffer.append("style=\"padding-left: " + (index * 10 + 18) + "px; " +
                    "background-position: " + index * 10 + "px\" ");
            index++;

            buffer.append("value=\"");
            buffer.append(Strings.escapeMarkup(renderer.getIdValue(choice, index)));
            buffer.append("\">");

            CharSequence escaped = escapeOptionHtml(displayValue);
            buffer.append(escaped);
            buffer.append("</option>");
        }
    }

    private File enforceRootFolder(File root) {
        if (chRoot == null) {
            return root;
        }
        if (!pathHelper.getCanonicalPath(root).startsWith(chRoot)) {
            return new File(chRoot);
        }
        return root;
    }

    private class BreadCrumbsModel extends AbstractReadOnlyModel<List<File>> {
        @Override
        public List<File> getObject() {
            String root = chRoot;
            if (root == null) {
                root = new File("/").getAbsolutePath();
            }
            List<File> breadCrumbs = new ArrayList<>();
            for (File folder = new File(getCurrentFolder()); folder != null;
                 folder = folder.getParentFile()) {
                if (root.equals(folder.getAbsolutePath())) {
                    breadCrumbs.add(0, folder);
                    break;
                }
                breadCrumbs.add(0, folder);
            }
            return breadCrumbs;
        }
    }

    private class BrowserAutoCompleteTextField extends PathAutoCompleteTextField {
        private BrowserAutoCompleteTextField(String id) {
            super(id, new DelegatedModel<File>(FileBrowserPanel.this), FileBrowserPanel.this.pathHelper);

            add(new AjaxFormComponentUpdatingBehavior("onselection") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    File file = (File) pathAutoCompleteTextField.getDefaultModelObject();

                    // if pathAutoCompleteTextField is empty default to current root
                    if (file == null) {
                        file = pathHelper.getAbsoluteFile("");
                    }
                    onFileSelected(file, target);
                }
            });

        }
    }

    private class RootsDropDownChoice extends DropDownChoice<File> {
        private RootsDropDownChoice(String id) {
            super(id);
            if (chRoot != null) {
                // allow only chRoot
                ArrayList<File> files = new ArrayList<>(1);
                File rootFile = new File(chRoot);
                files.add(rootFile);
                setChoices(files);
                setChoiceRenderer(new RootOnlyChoiceRenderer());
                setDefaultModel(new Model<>(rootFile));
            } else {
                // allow changing root
                List<File> roots = Arrays.asList(File.listRoots());
                setChoices(roots);
                setVisible(!roots.isEmpty());
                File defaultRoot = new File(FilenameUtils.getPrefix(getCurrentFolder())).getAbsoluteFile();
                setDefaultModel(new Model<>(defaultRoot));
            }

            add(new AjaxFormComponentUpdatingBehavior("onchange") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    File root = getModelObject();
                    setCurrentFolder(root.getAbsolutePath());
                    FileBrowserPanel.this.setDefaultModelObject(root);
                    target.add(FileBrowserPanel.this);
                }
            });
        }
    }

    private static class RootOnlyChoiceRenderer implements IChoiceRenderer<File> {
        @Override
        public Object getDisplayValue(File object) {
            return "/";
        }

        @Override
        public String getIdValue(File object, int index) {
            return "0";
        }
    }
}
