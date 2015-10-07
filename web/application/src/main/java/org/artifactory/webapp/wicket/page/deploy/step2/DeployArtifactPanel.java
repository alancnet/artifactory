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

package org.artifactory.webapp.wicket.page.deploy.step2;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.artifact.ArtifactInfo;
import org.artifactory.api.artifact.UnitInfo;
import org.artifactory.api.maven.MavenArtifactInfo;
import org.artifactory.api.maven.MavenService;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.DeployService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.repo.exception.maven.BadPomException;
import org.artifactory.api.request.ArtifactoryRequestBase;
import org.artifactory.common.wicket.ajax.NoAjaxIndicatorDecorator;
import org.artifactory.common.wicket.behavior.collapsible.CollapsibleBehavior;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.file.path.RepoPathAutoCompleteTextField;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.panel.feedback.UnescapedFeedbackMessage;
import org.artifactory.common.wicket.component.panel.titled.TitledActionPanel;
import org.artifactory.common.wicket.panel.editor.TextEditorPanel;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.common.wicket.util.ComponentPersister;
import org.artifactory.common.wicket.util.CookieUtils;
import org.artifactory.descriptor.repo.LocalRepoAlphaComparator;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.maven.MavenModelUtils;
import org.artifactory.md.Properties;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.util.ExceptionUtils;
import org.artifactory.util.Files;
import org.artifactory.util.PathUtils;
import org.artifactory.util.StringInputStream;
import org.artifactory.webapp.wicket.page.deploy.DeployArtifactPage;
import org.artifactory.webapp.wicket.page.deploy.step1.UploadArtifactPanel;
import org.artifactory.webapp.wicket.util.TreeUtils;
import org.artifactory.webapp.wicket.util.validation.DeployTargetPathValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static org.artifactory.common.wicket.util.ComponentPersister.setPersistent;

/**
 * @author Yoav Aharoni
 */
public class DeployArtifactPanel extends TitledActionPanel {
    private static final Logger log = LoggerFactory.getLogger(DeployArtifactPanel.class);

    public DeployArtifactPanel(String id, File file) {
        super(id);
        add(new DeployArtifactForm(file));
    }

    private class DeployArtifactForm extends SecureForm {
        private static final String TARGET_REPO = "targetRepo";

        @SpringBean
        private RepositoryService repoService;

        @SpringBean
        private DeployService deployService;

        @SpringBean
        private MavenService mavenService;

        private DeployModel model;
        private RepoPathAutoCompleteTextField pathField;
        private FormComponent targetRepo;

        private DeployArtifactForm(File file) {
            super("form");
            model = new DeployModel();
            model.file = file;
            model.mavenArtifactInfo = guessArtifactInfo();
            model.pomXml = mavenService.getPomModelString(file);
            model.deployPom = (isPomArtifact() || !isPomExists(getPersistentTargetRepo())) && !hasClassifier();
            model.repos = getRepos();
            model.targetRepo = getPersistentTargetRepo();

            setDefaultModel(new CompoundPropertyModel<>(model));

            add(new Label("file.name"));
            addTargetRepoDropDown();
            addPathField();
            addDeployMavenCheckbox();

            WebMarkupContainer artifactInfoContainer = newMavenArtifactContainer();
            add(artifactInfoContainer);

            artifactInfoContainer.add(newPomEditContainer());

            addDefaultButton(new DeployLink("deploy"));
            addButton(new CancelLink("cancel"));
        }

        //will be first initialize from cookie value, fall back to default

        private LocalRepoDescriptor getPersistentTargetRepo() {
            String cookieName = buildCookieName();
            String cookie = CookieUtils.getCookie(cookieName);
            int value = 0;
            if (cookie != null) {
                try {
                    value = Integer.parseInt(cookie);
                } catch (NumberFormatException e) {
                    log.debug("Failed to parse cookie for upload target repo, will use default repo");
                }
            }
            List<LocalRepoDescriptor> repos = getRepos();
            return value < repos.size() ? repos.get(value) : repos.get(0);
        }

        private String buildCookieName() {
            StringBuilder name = new StringBuilder(DeployArtifactPanel.this.getId());
            name.append(".").append(this.getId()).append(".").append(TARGET_REPO);
            return name.toString();
        }

        private List<LocalRepoDescriptor> getRepos() {
            List<LocalRepoDescriptor> repos = repoService.getDeployableRepoDescriptors();
            if (repos.isEmpty()) {
                throw new UnauthorizedInstantiationException(DeployArtifactPage.class);
            }
            Collections.sort(repos, new LocalRepoAlphaComparator());
            return repos;
        }

        private Component newPomEditContainer() {
            MarkupContainer pomEditContainer = new WebMarkupContainer("pomEditContainer");
            pomEditContainer.setOutputMarkupPlaceholderTag(true);
            pomEditContainer.add(newGeneratePomCheckBox());
            pomEditContainer.add(newPomEditorPanel());
            return pomEditContainer;
        }

        private Component newPomEditorPanel() {
            final String helpMessage =
                    "View the resulting POM and handle possible discrepancies: fix bad coordinates, remove " +
                            "unwanted repository references, etc. Use with caution!";

            TextEditorPanel pomEditPanel = new TextEditorPanel("pomEditPanel", "POM Editor", helpMessage) {
                @Override
                protected IModel<String> newTextModel() {
                    return new PropertyModel<>(model, "pomXml");
                }

                @Override
                public boolean isVisible() {
                    return model.deployPom;
                }
            };
            pomEditPanel.add(new CollapsibleBehavior().setUseAjax(true));
            pomEditPanel.addTextAreaBehavior(new OnPomXmlChangeBehavior());
            return pomEditPanel;
        }

        private Component newGeneratePomCheckBox() {
            FormComponent checkbox = new StyledCheckbox("deployPom");
            checkbox.setOutputMarkupId(true);
            checkbox.setVisible(!isPomArtifact() && !hasClassifier());
            checkbox.setLabel(Model.of("Also Deploy Jar's Internal POM/Generate Default POM"));
            checkbox.add(new OnGeneratePomChangeBehavior());
            return checkbox;
        }

        private WebMarkupContainer newMavenArtifactContainer() {
            WebMarkupContainer artifactInfoContainer = new WebMarkupContainer("artifactInfo") {
                @Override
                public boolean isVisible() {
                    return model.isMavenArtifact;
                }
            };
            artifactInfoContainer.setOutputMarkupPlaceholderTag(true);
            artifactInfoContainer.add(newGavcField("artifactInfo.groupId", true, new OnGavcChangeBehavior()));
            artifactInfoContainer.add(newGavcField("artifactInfo.artifactId", true, new OnGavcChangeBehavior()));
            artifactInfoContainer.add(newGavcField("artifactInfo.version", true, new OnGavcChangeBehavior()));
            artifactInfoContainer.add(newGavcField("artifactInfo.classifier", false, new OnClassifierChangeBehavior()));
            artifactInfoContainer.add(newGavcField("artifactInfo.type", true, new OnPackTypeChangeBehavior()));
            return artifactInfoContainer;
        }

        private Component newGavcField(String id, boolean required, Behavior behavior) {
            FormComponent textField = new TextField(id);
            textField.setRequired(required);
            textField.setOutputMarkupId(true);
            textField.add(behavior);
            return textField;
        }

        private void addPathField() {
            pathField = new RepoPathAutoCompleteTextField("targetPath", repoService) {
                @Override
                public boolean isEnabled() {
                    return !model.isMavenArtifact;
                }
            };
            pathField.setRepoKey(((LocalRepoDescriptor) targetRepo.getDefaultModelObject()).getKey());
            pathField.getDefaultModel();
            pathField.setRequired(true);
            pathField.setOutputMarkupId(true);
            pathField.add(new DeployTargetPathValidator());
            add(pathField);

            add(new HelpBubble("targetPath.help", new ResourceModel("targetPath.help")));
        }

        private void addDeployMavenCheckbox() {
            StyledCheckbox autoCalculatePath = new StyledCheckbox("isMavenArtifact");
            setPersistent(autoCalculatePath);
            autoCalculatePath.add(new OnDeployTypeChangeBehavior());
            add(autoCalculatePath);
            add(new HelpBubble("isMavenArtifact.help", new ResourceModel("isMavenArtifact.help")));
        }

        private void addTargetRepoDropDown() {
            targetRepo = new DropDownChoice<>(TARGET_REPO, model.repos);
            targetRepo.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    pathField.setRepoKey(((LocalRepoDescriptor) get(TARGET_REPO).getDefaultModelObject()).getKey());
                }
            });
            setPersistent(targetRepo);
            targetRepo.setRequired(true);
            add(targetRepo);

            add(new HelpBubble("targetRepo.help", new ResourceModel("targetRepo.help")));
        }

        private MarkupContainer getPomEditorContainer() {
            return (MarkupContainer) get("artifactInfo:pomEditContainer");
        }

        /**
         * Try to guess the properties from pom/jar content.
         *
         * @return artifact info
         */
        private MavenArtifactInfo guessArtifactInfo() {
            try {
                // if (pom or jar) get pom xml as string
                return mavenService.getMavenArtifactInfo(model.file);
            } catch (Exception e) {
                String msg = "Unable to analyze uploaded file content. Cause: " + e.getMessage();
                log.debug(msg, e);
                error(msg);
            }
            return new MavenArtifactInfo();
        }

        private boolean isPomArtifact() {
            UnitInfo artifactInfo = model.getArtifactInfo();
            if (!artifactInfo.isMavenArtifact()) {
                return false;
            }
            MavenArtifactInfo mavenArtifactInfo = (MavenArtifactInfo) artifactInfo;
            String packagingType = mavenArtifactInfo.getType();
            return MavenArtifactInfo.POM.equalsIgnoreCase(packagingType);
        }

        private boolean hasClassifier() {
            UnitInfo artifactInfo = model.getArtifactInfo();
            if (!artifactInfo.isMavenArtifact()) {
                return false;
            }
            MavenArtifactInfo mavenArtifactInfo = (MavenArtifactInfo) artifactInfo;
            return StringUtils.isNotBlank(mavenArtifactInfo.getClassifier());
        }

        private boolean isPomExists(LocalRepoDescriptor repo) {
            try {
                String path =
                        MavenModelUtils.mavenModelToArtifactInfo(MavenModelUtils.toMavenModel(model.mavenArtifactInfo))
                                .getPath();
                String pomPath = PathUtils.stripExtension(path) + ".pom";
                return repoService.exists(InternalRepoPathFactory.create(repo.getKey(), pomPath));
            } catch (RepositoryRuntimeException e) {
                cleanupResources();
                throw e;
            }
        }

        private void cleanupResources() {
            log.debug("Cleaning up deployment resources.");
            if (model.mavenArtifactInfo != null) {
                model.mavenArtifactInfo = new MavenArtifactInfo();
            }
            Files.removeFile(model.file);
            model.pomXml = "";
        }

        private class OnGavcChangeBehavior extends AjaxFormComponentUpdatingBehavior {

            private OnGavcChangeBehavior() {
                super("onchange");
            }

            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return new NoAjaxIndicatorDecorator();
            }

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                UnitInfo artifactInfo = model.getArtifactInfo();
                model.deployPom = isPomArtifact() || !isPomExists(getPersistentTargetRepo());
                if (model.deployPom && model.isMavenArtifact) {
                    model.pomChanged = true;
                    MavenArtifactInfo mavenArtifactInfo = (MavenArtifactInfo) artifactInfo;
                    org.apache.maven.model.Model mavenModel;
                    if (model.pomXml != null) {
                        mavenModel = MavenModelUtils.stringToMavenModel(model.pomXml);
                        // update the model built from the xml with the values from the maven artifact info
                        mavenModel.setGroupId(mavenArtifactInfo.getGroupId());
                        mavenModel.setArtifactId(mavenArtifactInfo.getArtifactId());
                        mavenModel.setVersion(mavenArtifactInfo.getVersion());
                        mavenModel.setPackaging(mavenArtifactInfo.getType());
                    } else {
                        // generate a model from the maven artifact info
                        mavenModel = MavenModelUtils.toMavenModel(mavenArtifactInfo);
                    }
                    model.pomXml = MavenModelUtils.mavenModelToString(mavenModel);
                }
                target.add(getPomEditorContainer().get("deployPom"));
                target.add(getPomEditorContainer());
                target.add(get("targetPath"));
                AjaxUtils.refreshFeedback();
            }
        }

        private class OnDeployTypeChangeBehavior extends AjaxFormComponentUpdatingBehavior {
            private OnDeployTypeChangeBehavior() {
                super("onclick");
            }

            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return new NoAjaxIndicatorDecorator();
            }

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                boolean isMavenArtifact = Boolean.parseBoolean(get("isMavenArtifact").getDefaultModelObjectAsString());
                if (!isMavenArtifact || hasClassifier()) {
                    model.deployPom = false;
                }
                target.add(get("artifactInfo"));
                target.add(get("targetPath"));
            }
        }

        private class OnPomXmlChangeBehavior extends AjaxFormComponentUpdatingBehavior {
            private OnPomXmlChangeBehavior() {
                super("onchange");
            }

            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return new NoAjaxIndicatorDecorator();
            }

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                model.pomChanged = true;
                if (StringUtils.isEmpty(model.pomXml)) {
                    return;
                }
                try {
                    InputStream pomInputStream = IOUtils.toInputStream(model.pomXml);
                    model.mavenArtifactInfo = MavenModelUtils.mavenModelToArtifactInfo(pomInputStream);
                    model.mavenArtifactInfo.setType(PathUtils.getExtension(model.getTargetPathFieldValue()));
                } catch (Exception e) {
                    error("Failed to parse input pom");
                    AjaxUtils.refreshFeedback();
                }
                model.deployPom = isPomArtifact() || !isPomExists(getPersistentTargetRepo());
                target.add(get("artifactInfo:artifactInfo.groupId"));
                target.add(get("artifactInfo:artifactInfo.artifactId"));
                target.add(get("artifactInfo:artifactInfo.version"));
                target.add(get("targetPath"));
                target.add(getPomEditorContainer());
                AjaxUtils.refreshFeedback();
            }
        }

        private class OnGeneratePomChangeBehavior extends AjaxFormComponentUpdatingBehavior {
            private OnGeneratePomChangeBehavior() {
                super("onclick");
            }

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (model.deployPom) {
                    model.pomChanged = false;
                    if (StringUtils.isBlank(model.pomXml)) {
                        model.pomXml = mavenService.getPomModelString(model.file);
                    }
                }
                target.add(getPomEditorContainer());
            }
        }

        private class OnPackTypeChangeBehavior extends OnGavcChangeBehavior {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                Component deployPomCheckbox = getPomEditorContainer().get("deployPom");
                boolean shouldDeployPom = !isPomArtifact() && !hasClassifier();
                deployPomCheckbox.setVisible(shouldDeployPom);
                model.deployPom = shouldDeployPom;
                target.add(getPomEditorContainer());
                super.onUpdate(target);
            }
        }

        private class OnClassifierChangeBehavior extends OnGavcChangeBehavior {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                MarkupContainer pomEditorContainer = getPomEditorContainer();
                pomEditorContainer.setVisible(!hasClassifier());
                pomEditorContainer.get("deployPom").setVisible(!hasClassifier());
                super.onUpdate(target);
            }
        }

        private void finish(AjaxRequestTarget target) {
            cleanupResources();
            Component uploadPanel = new UploadArtifactPanel();
            DeployArtifactPanel.this.replaceWith(uploadPanel);
            target.add(uploadPanel);
        }

        @Override
        protected void onBeforeRender() {
            super.onBeforeRender();
            ComponentPersister.loadChildren(this);
        }

        private class DeployLink extends TitledAjaxSubmitLink {
            private DeployLink(String id) {
                super(id, "Deploy Artifact", DeployArtifactForm.this);
                setOutputMarkupId(true);
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                ComponentPersister.saveChildren(DeployArtifactForm.this);

                Properties props = parseMatrixParams();
                try {
                    //Make sure not to override a good pom.
                    boolean deployPom = model.deployPom && model.isMavenArtifact;
                    if (deployPom) {
                        if (isPomArtifact()) {
                            deployPom(props);
                        } else {
                            deployFileAndPom(props);
                        }
                    } else {
                        deployFile(props);
                    }
                    String repoKey = model.targetRepo.getKey();
                    String artifactPath = model.getTargetPathFieldValue();
                    StringBuilder successMessagesBuilder = new StringBuilder();
                    successMessagesBuilder.append("Successfully deployed ");
                    String repoPathUrl = TreeUtils.getRepoPathUrl(model.targetRepo, artifactPath);
                    String escapedArtifactPath = StringEscapeUtils.escapeXml(artifactPath);
                    if (StringUtils.isNotBlank(repoPathUrl)) {
                        successMessagesBuilder.append("<a href=\"").append(repoPathUrl).append("\">").
                                append(escapedArtifactPath).append(" into ").
                                append(repoKey).append("</a>.");
                    } else {
                        successMessagesBuilder.append(escapedArtifactPath).append(" into ").append(repoKey).append(".");
                    }
                    info(new UnescapedFeedbackMessage(successMessagesBuilder.toString()));
                    AjaxUtils.refreshFeedback(target);
                    finish(target);
                } catch (Exception e) {
                    Throwable cause = ExceptionUtils.getRootCause(e);
                    if ((cause instanceof BadPomException) || (cause instanceof RepoRejectException)) {
                        log.warn("Failed to deploy artifact: {}", e.getMessage());
                    } else {
                        log.warn("Failed to deploy artifact.", e);
                    }
                    error(e.getMessage());
                    AjaxUtils.refreshFeedback(target);
                }
            }

            // this form supports properties with format similar to matrix params
            private Properties parseMatrixParams() {
                Properties props;
                props = (Properties) InfoFactoryHolder.get().createProperties();
                String targetPathFieldValue = model.getTargetPathFieldValue();
                int matrixParamStart = targetPathFieldValue.indexOf(Properties.MATRIX_PARAMS_SEP);
                if (matrixParamStart > 0) {
                    ArtifactoryRequestBase.processMatrixParams(props, targetPathFieldValue.substring(matrixParamStart));
                    model.targetPath = targetPathFieldValue.substring(0, matrixParamStart);
                }
                return props;
            }

            private void deployPom(Properties props) throws Exception {
                if (model.pomChanged) {
                    savePomXml();
                }
                deployFile(props);
            }

            private void savePomXml() throws Exception {
                StringInputStream input = null;
                FileOutputStream output = null;
                try {
                    input = new StringInputStream(model.pomXml);
                    output = new FileOutputStream(model.file);
                    IOUtils.copy(input, output);
                } finally {
                    IOUtils.closeQuietly(input);
                    IOUtils.closeQuietly(output);
                }
            }

            private void deployFileAndPom(Properties props) throws IOException, RepoRejectException {
                ModuleInfo moduleInfo = repoService.getItemModuleInfo(
                        InternalRepoPathFactory.create(model.targetRepo.getKey(), model.getTargetPathFieldValue()));
                mavenService.validatePomContent(model.pomXml, model.getTargetPathFieldValue(),
                        moduleInfo, model.targetRepo.isSuppressPomConsistencyChecks());
                deployService.deploy(model.targetRepo, model.getArtifactInfo(), model.file, model.pomXml, true, false,
                        props);
            }

            private void deployFile(Properties props) throws RepoRejectException {
                deployService.deploy(model.targetRepo, model.getArtifactInfo(), model.file, props);
            }
        }

        private class CancelLink extends TitledAjaxLink {
            private CancelLink(String id) {
                super(id, "Cancel");
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                finish(target);
            }
        }
    }

    private static class DeployModel implements Serializable {
        private List<LocalRepoDescriptor> repos;
        private File file;
        private LocalRepoDescriptor targetRepo;
        private String targetPath;
        private boolean pomChanged = false;
        private String pomXml;
        private boolean isMavenArtifact = true;
        private boolean deployPom;
        private MavenArtifactInfo mavenArtifactInfo;

        /**
         * Do not use this method to retrieve the actual value of the field, since this method determines the value of
         * the field (based on the model) before returning value.<br> To simply return the value of the field use
         * org.artifactory.webapp.wicket.page.deploy.step2.DeployArtifactPanel.DeployModel#getTargetPathFieldValue()
         *
         * @return Target path
         */
        public String getTargetPath() {
            /**
             * If the item should be deployed as a maven artifact, is a pom, or contains a pom, prepare the path in the
             * Maven format, otherwise, the path should just be the file name so we avoid awkward deployment paths for
             * non-Maven artifacts
             */
            if (isMavenArtifact || MavenArtifactInfo.POM.equalsIgnoreCase(mavenArtifactInfo.getType()) ||
                    StringUtils.isNotBlank(MavenModelUtils.getPomFileAsStringFromJar(file))) {
                targetPath = mavenArtifactInfo.getPath();
            } else {
                targetPath = file.getName();
            }
            return targetPath;
        }

        /**
         * Simply returns the actual value of the field
         *
         * @return Target path field value
         */
        public String getTargetPathFieldValue() {
            return targetPath;
        }

        public UnitInfo getArtifactInfo() {
            if (isMavenArtifact) {
                return mavenArtifactInfo;
            } else {
                return new ArtifactInfo(targetPath);
            }
        }
    }
}