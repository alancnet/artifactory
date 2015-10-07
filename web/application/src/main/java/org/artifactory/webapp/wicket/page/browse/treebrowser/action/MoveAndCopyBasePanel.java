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

package org.artifactory.webapp.wicket.page.browse.treebrowser.action;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.Strings;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.search.SavedSearchResults;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.StatusEntry;
import org.artifactory.common.wicket.ajax.ConfirmationAjaxCallDecorator;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.component.modal.links.ModalCloseLink;
import org.artifactory.common.wicket.component.panel.feedback.UnescapedFeedbackMessage;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.descriptor.repo.LocalRepoAlphaComparator;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.RepoLayoutUtils;
import org.artifactory.webapp.wicket.application.ArtifactoryWebSession;
import org.artifactory.webapp.wicket.page.logs.SystemLogsPage;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This panel displays a list of local repositories the user can select to move a path or paths to.
 *
 * @author Yossi Shaul
 */
public abstract class MoveAndCopyBasePanel extends Panel {

    @SpringBean
    private RepositoryService repoService;

    @SpringBean
    private AuthorizationService authorizationService;

    private DropDownChoice<LocalRepoDescriptor> targetRepos;

    private LocalRepoDescriptor selectedTreeItemRepo;

    protected RepoPath sourceRepoPath;

    protected String targetPath;

    protected boolean enableCustomTargetPath;

    protected MoveAndCopyPathPanel targetPathPanel;

    protected StyledCheckbox enableCustomTargetPathCheckBox;

    public MoveAndCopyBasePanel(String id) {
        this(id, null);
    }

    public MoveAndCopyBasePanel(String id, RepoPath sourceRepoPath) {
        super(id);
        if (sourceRepoPath != null) {
            this.sourceRepoPath = sourceRepoPath;
            selectedTreeItemRepo = repoService.localRepoDescriptorByKey(sourceRepoPath.getRepoKey());
        }
    }

    protected void init() {
        Form form = new SecureForm("form");
        form.setOutputMarkupId(true);
        add(form);

        List<LocalRepoDescriptor> localRepos = getDeployableLocalRepoDescriptors();
        Collections.sort(localRepos, new LocalRepoAlphaComparator());
        ChoiceRenderer<LocalRepoDescriptor> choiceRenderer = new ChoiceRenderer<LocalRepoDescriptor>() {
            @Override
            public Object getDisplayValue(LocalRepoDescriptor selectedTarget) {
                StringBuilder displayValueBuilder = new StringBuilder();
                if (!areLayoutsCompatible(selectedTarget)) {
                    displayValueBuilder.append("! ");
                }
                displayValueBuilder.append(selectedTarget.getKey());
                return displayValueBuilder.toString();
            }
        };
        targetRepos = new DropDownChoice<>("targetRepos", new Model<LocalRepoDescriptor>(),
                localRepos, choiceRenderer);
        targetRepos.setLabel(Model.of("Target Repository"));
        targetRepos.setRequired(true);
        targetRepos.setOutputMarkupId(true);
        form.add(targetRepos);

        StringBuilder targetRepoHelp = new StringBuilder("Selects the target repository for the transferred items.");
        if (sourceRepoPath != null) {
            targetRepoHelp.append("\nRepositories starting with an exclamation mark ('!') indicate that not all ").
                    append("tokens\ncan be mapped between the layouts of the source repository and the marked ").
                    append("repository.\nPath translations may not work as expected.");
        }
        HelpBubble targetRepoHelpBubble = new HelpBubble("targetRepos.help", targetRepoHelp.toString());
        form.add(targetRepoHelpBubble);


        enableCustomTargetPathCheckBox = new StyledCheckbox("enableCustomTargetPathCheckBox",
                new Model<>(enableCustomTargetPath));
        enableCustomTargetPathCheckBox.add(new AjaxFormComponentUpdatingBehavior("onclick") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                targetPathPanel.setVisible(enableCustomTargetPathCheckBox.getModelObject());
                List newChoices = targetRepos.getChoices();

                //Add source repo to custom path list but only if its not a cache repo
                LocalRepoDescriptor sourceRepoDescriptor = repoService.localRepoDescriptorByKey(
                        sourceRepoPath.getRepoKey());
                if (enableCustomTargetPathCheckBox.getModelObject() && sourceRepoDescriptor != null) {
                    newChoices.add(sourceRepoDescriptor);
                } else {
                    newChoices.remove(repoService.localRepoDescriptorByKey(sourceRepoPath.getRepoKey()));
                }
                Collections.sort(newChoices, new LocalRepoAlphaComparator());
                targetRepos.setChoices(newChoices);
                if (target != null) {
                    target.add(targetPathPanel.getParent());
                    target.add(targetRepos);
                    AjaxUtils.refreshFeedback(target);
                }
            }
        });
        enableCustomTargetPathCheckBox.setLabel(Model.of(String.format("%s to a custom path",
                getOperationType().getOpName())));
        enableCustomTargetPathCheckBox.setOutputMarkupId(true);

        //Panel and checkbox only visible on copy/move path panels
        enableCustomTargetPathCheckBox.setVisible(!isSearchResultsOperation());
        form.add(enableCustomTargetPathCheckBox);

        form.add(new HelpBubble("enableCustomTargetPathCheckBox.help",
                new ResourceModel("enableCustomTargetPathCheckBox" + getOperationType().opName + ".help"))
                .setVisible(!isSearchResultsOperation()));

        targetPathPanel = new MoveAndCopyPathPanel("targetPathPanel", new PropertyModel<>(this, "targetPath"),
                getOperationType());
        if (sourceRepoPath != null) {
            setTargetPath(sourceRepoPath.getPath()); //Only for copy/move path panel
        }
        targetPathPanel.setOutputMarkupId(true);
        form.add(targetPathPanel);

        Label dryRunResult = new Label("dryRunResult", "");
        dryRunResult.setVisible(false);
        dryRunResult.setEscapeModelStrings(false);
        form.add(dryRunResult);

        form.add(new ModalCloseLink("cancel"));

        TitledAjaxSubmitLink actionButton = createSubmitButton(form, "action");
        form.add(actionButton);

        TitledAjaxSubmitLink dryRunButton = createDryRunButton(form, "dryRun");
        form.add(dryRunButton);
    }

    private boolean areLayoutsCompatible(LocalRepoDescriptor selectedTarget) {
        RepoLayout sourceLayout = (selectedTreeItemRepo != null) ? selectedTreeItemRepo.getRepoLayout() : null;
        RepoLayout targetRepo = (selectedTarget != null) ? selectedTarget.getRepoLayout() : null;

        return ((sourceLayout == null) || (targetRepo == null) ||
                RepoLayoutUtils.layoutsAreCompatible(sourceLayout, targetRepo));
    }

    protected String getSelectedTargetRepository() {
        return targetRepos.getDefaultModelObjectAsString();
    }

    /**
     * Returns true if copying/moving to a custom path, otherwise layout translation is enforced
     *
     * @return if the move/copy action should suppress layout translation
     */
    protected Boolean getSuppressLayout() {
        if (enableCustomTargetPathCheckBox.getModelObject()) {
            return true;
        } else {
            return false;
        }
    }

    protected String getTargetPath() {
        return targetPath;
    }

    protected void setTargetPath(String repoPath) {
        targetPath = repoPath;
    }

    protected boolean isTargetPathSpecified() {
        return targetPath != null;
    }

    protected RepoPath getTargetRepoPath() {
        return InternalRepoPathFactory.create(getSelectedTargetRepository(), targetPath);
    }

    protected abstract void refreshPage(AjaxRequestTarget target, boolean isError);


    /**
     * @return local repo descriptors the user has permission to deploy on, excluding the source repository
     */
    protected List<LocalRepoDescriptor> getDeployableLocalRepoDescriptors() {
        List deployableRepoDescriptors = repoService.getDeployableRepoDescriptors();
        if (sourceRepoPath != null) {
            deployableRepoDescriptors.remove(repoService.localRepoDescriptorByKey(sourceRepoPath.getRepoKey()));
        }
        return deployableRepoDescriptors;
    }

    protected abstract MoveMultiStatusHolder executeDryRun();

    protected abstract MoveMultiStatusHolder moveOrCopy(boolean dryRun, boolean failFast);

    /**
     * Returns whether this is a move or copy operation
     *
     * @return COPY_OPERATION or MOVE_OPERATION according to the operation this panel performs
     */
    protected abstract OperationType getOperationType();

    protected boolean isSearchResultsOperation() {
        return getOperationType() == OperationType.COPY_RESULTS_OPERATION
                || getOperationType() == OperationType.MOVE_RESULTS_OPERATION;
    }

    protected TitledAjaxSubmitLink createDryRunButton(Form form, String wicketId) {
        return new TitledAjaxSubmitLink(wicketId, "Dry Run", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                if (!isSearchResultsOperation()) {
                    if (!sourceAndTargetAreValid()) {
                        AjaxUtils.refreshFeedback(target);
                        return;
                    }
                }

                MoveMultiStatusHolder status = executeDryRun();
                StringBuilder result = new StringBuilder();
                if (!status.isError() && !status.hasWarnings()) {
                    result.append(("<div class='info'>Dry run completed successfully with no errors.</div>"));
                } else {
                    result.append(("<div class='title'>Dry run completed with the following errors/warnings:</div>"));
                    if (status.isError()) {
                        List<StatusEntry> errors = status.getErrors();
                        for (StatusEntry error : errors) {
                            result.append("<div class='notice'>Error: ")
                                    .append(Strings.escapeMarkup(error.getMessage(), false, false))
                                    .append("</div>");
                        }
                    }
                    if (status.hasWarnings()) {
                        List<StatusEntry> warnings = status.getWarnings();
                        for (StatusEntry warning : warnings) {
                            result.append("<div class='notice'>Warning: ")
                                    .append(Strings.escapeMarkup(warning.getMessage(), false, false))
                                    .append("</div>");
                        }
                    }
                }

                Component dryRunResult = form.get("dryRunResult");
                dryRunResult.setDefaultModelObject(result.toString());
                dryRunResult.setVisible(true);
                target.add(MoveAndCopyBasePanel.this);
                target.add(form);
                AjaxUtils.refreshFeedback(target);
                ModalHandler.bindHeightTo("dryRunResult");
            }
        };
    }

    protected TitledAjaxSubmitLink createSubmitButton(Form form, String wicketId) {
        final String opName = getOperationType().getOpName();
        final String opText = getOperationType().getOpText();
        return new TitledAjaxSubmitLink(wicketId, opName, form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {

                //Validation of source path is not needed for move/copy results
                if (!isSearchResultsOperation()) {
                    if (!sourceAndTargetAreValid()) {
                        AjaxUtils.refreshFeedback(target);
                        return;
                    }
                }
                MoveMultiStatusHolder status = moveOrCopy(false, false);
                String logs;
                if (authorizationService.isAdmin()) {
                    String systemLogsPage = WicketUtils.absoluteMountPathForPage(SystemLogsPage.class);
                    logs = "<a href=\"" + systemLogsPage + "\">log</a>";
                } else {
                    logs = "log";
                }
                String multipleFailsMessage = "%s %s have been produced during the " + opName.toLowerCase()
                        + ". Please " + "review the " + logs + " for further information.";
                if (!status.isError() && !status.hasWarnings()) {
                    String copyMoveSuccessMessage = "Successfully " + opText + " %s to '" + getTargetRepoPath() + "'.";
                    if (isSearchResultsOperation()) {
                        Session.get().info(String.format(copyMoveSuccessMessage, "search results"));
                    } else {
                        Session.get().info(String.format(copyMoveSuccessMessage, "'" + sourceRepoPath + "'"));
                    }
                    AjaxUtils.refreshFeedback(target);
                    ModalHandler.closeCurrent(target);
                } else {
                    if (status.hasWarnings()) {
                        List<StatusEntry> warnings = status.getWarnings();
                        Session.get().warn(new UnescapedFeedbackMessage(String.format(multipleFailsMessage,
                                warnings.size(), "warnings")));
                        AjaxUtils.refreshFeedback(target);
                        ModalHandler.closeCurrent(target);
                    }
                    if (status.isError()) {
                        List<StatusEntry> errors = status.getErrors();
                        if (errors.size() > 1) {
                            error(new UnescapedFeedbackMessage(String.format(multipleFailsMessage, errors.size(),
                                    "errors")));
                        } else {
                            String message = status.getStatusMsg();
                            Throwable exception = status.getException();
                            if (exception != null) {
                                message = exception.getMessage();
                            }
                            String copyMoveErrorMessage = "Failed to " + opName.toLowerCase() + " '%s': " + message;
                            if (isSearchResultsOperation()) {
                                error(String.format(copyMoveErrorMessage, "search results"));
                            } else {
                                error(String.format(copyMoveErrorMessage, "'" + sourceRepoPath + "'"));
                            }
                        }
                        AjaxUtils.refreshFeedback(target);
                    }
                }
                refreshPage(target, status.isError());
            }

            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                if (getOperationType() == OperationType.MOVE_OPERATION //Confirmation dialog only for move panels
                        || getOperationType() == OperationType.MOVE_RESULTS_OPERATION) {

                    // add confirmation dialog when clicked
                    String confirmationMessage = "Are you sure you wish to move %s?";
                    String message = isSearchResultsOperation() ?
                            String.format(confirmationMessage, "all search results")
                            : String.format(confirmationMessage, "'" + sourceRepoPath + "'");
                    return new ConfirmationAjaxCallDecorator(message);
                } else {
                    return null;
                }
            }
        };
    }

    /**
     * Returns the contained repo paths of the given search result
     *
     * @param resultName Name of search result to query
     * @return List of repo paths associated with the given search result
     */
    protected Set<RepoPath> getResultPaths(String resultName) {
        SavedSearchResults searchResults = ArtifactoryWebSession.get().getResults(resultName);

        // collect all the repo paths that needs to be handled
        Set<RepoPath> pathsToReturn = new HashSet<>();
        for (FileInfo fileInfo : searchResults.getResults()) {
            pathsToReturn.add(fileInfo.getRepoPath());
        }
        return pathsToReturn;
    }

    protected boolean sourceAndTargetAreValid() {
        if (getSelectedTargetRepository().equalsIgnoreCase(sourceRepoPath.getRepoKey()) && !isTargetPathSpecified()) {
            error("Target path must be specified if source and target repositories are the same");
            return false;
        }
        return true;
    }

    protected enum OperationType {
        COPY_OPERATION("Copy", "Copied"),
        COPY_RESULTS_OPERATION("Copy", "copied"),
        MOVE_OPERATION("Move", "Moved"),
        MOVE_RESULTS_OPERATION("Move", "moved");
        private String opName;
        private String opText;

        private OperationType(String opName, String opText) {
            this.opName = opName;
            this.opText = opText;
        }

        protected String getOpName() {
            return this.opName;
        }

        protected String getOpText() {
            return this.opText;
        }
    }
}