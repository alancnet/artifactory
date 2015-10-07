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

package org.artifactory.webapp.wicket.page.config.general;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.IResourceListener;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.StringValidator;
import org.artifactory.addon.AddonsManager;
import org.artifactory.common.wicket.ajax.NoAjaxIndicatorDecorator;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.panel.logo.BaseLogoPanel;
import org.artifactory.common.wicket.panel.upload.LogoFileUploadForm;
import org.artifactory.common.wicket.panel.upload.UploadListener;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.util.HttpUtils;
import org.artifactory.webapp.wicket.application.ArtifactoryApplication;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;
import org.artifactory.webapp.wicket.resource.ImageFileResource;
import org.artifactory.webapp.wicket.util.validation.UriValidator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

/**
 * @author Tomer Cohen
 */
public class CustomizingPanel extends BaseCustomizingPanel implements UploadListener, IResourceListener {
    public static final int MAX_FOOTER_LENGTH = 64;

    @SpringBean
    private AddonsManager addonsManager;

    private LogoFileUploadForm fileUploadLogo;
    private boolean deleteLogo = false;
    private boolean isFakeLogo = false;

    public CustomizingPanel(String id, IModel model) {
        super(id, model);
        add(new CssClass("general-settings-panel"));

        fileUploadLogo = new LogoFileUploadForm("logoPath", this);
        add(fileUploadLogo);

        TextField<String> urlLogo = new TextField<>("logo");
        urlLogo.add(new UriValidator("http", "https"));
        urlLogo.add(new UrlChangedBehavior());
        urlLogo.setOutputMarkupId(true);
        fileUploadLogo.add(urlLogo);

        TextField<String> footer = new TextField<>("footer");

        fileUploadLogo.add(new ResetLink("reset", fileUploadLogo));
        footer.add(StringValidator.maximumLength(MAX_FOOTER_LENGTH));
        footer.add(new AttributeModifier("maxlength", MAX_FOOTER_LENGTH));
        footer.setOutputMarkupId(true);
        add(footer);

        fileUploadLogo.add(new SchemaHelpBubble(("logo.help")));
        fileUploadLogo.add(new HelpBubble("logoFile.help", "Upload a logo image file."));
        add(new SchemaHelpBubble("footer.help"));

        fileUploadLogo.add(new PreviewLogoPanel("logoPreview"));
    }

    @Override
    public void cleanup() {
        fileUploadLogo.removeUploadedFile();
    }

    @Override
    public void onException() {
        error("Failed to upload logo");
    }

    @Override
    public void info(String message) {
        super.info(message);
    }

    @Override
    public void onResourceRequested() {
        final File uploadedFile = getUploadedFile();
        if (uploadedFile != null) {
            new ImageFileResource(uploadedFile).onResourceRequested();
        }
    }

    @Override
    public void onFileSaved(File file) {
        deleteLogo = false;
        getDescriptor().setLogo(null);
        info("Logo Uploaded Successfully. Please remember to save your changes.");
    }

    @Override
    public boolean shouldDeleteLogo() {
        return deleteLogo && getDescriptor().getLogo() == null && fileUploadLogo.getUploadedFile() == null;
    }

    @Override
    public File getUploadedFile() {
        return fileUploadLogo.getUploadedFile();
    }

    @Override
    public String getTitle() {
        return "Look and Feel Settings";
    }

    private MutableCentralConfigDescriptor getDescriptor() {
        return (MutableCentralConfigDescriptor) getDefaultModelObject();
    }

    private static class PreviewLogoPanel extends BaseLogoPanel {
        @SpringBean
        private AddonsManager addonsManager;

        public PreviewLogoPanel(String id) {
            super(id);
            setOutputMarkupPlaceholderTag(true);
        }

        @Override
        protected Class<? extends Page> getLinkPage() {
            return null;
        }

        @Override
        protected String getLogoUrl() {
            CustomizingPanel parent = findParent(CustomizingPanel.class);
            MutableCentralConfigDescriptor descriptor;
            if (parent != null) {
                descriptor = parent.getDescriptor();
                String logo = descriptor.getLogo();
                if (logo != null) {
                    return getImageLogoUrlIfImageIsValid(logo, parent);
                }
            }
            if (parent != null) {
                if (parent.deleteLogo) {
                    return null;
                }
                if (parent.getUploadedFile() != null) {
                    return parent.urlFor(IResourceListener.INTERFACE) + "&" + System.currentTimeMillis();
                }
            }
            final ArtifactoryApplication application = ArtifactoryApplication.get();
            if (application.isLogoExists()) {
                return HttpUtils.getWebappContextUrl(
                        WicketUtils.getHttpServletRequest()) + "logo?" + application.getLogoModifyTime();
            }
            return null;
        }

        private String getImageLogoUrlIfImageIsValid(String logoUrl, CustomizingPanel parent) {
            String currentLogoUrl;
            if (parent.isFakeLogo) {
                parent.getUploadedFile();
                currentLogoUrl = null;
            } else {
                currentLogoUrl = logoUrl;
            }
            return currentLogoUrl;
        }
    }

    private static class UrlChangedBehavior extends AjaxFormComponentUpdatingBehavior {
        public UrlChangedBehavior() {
            super("onchange");
        }

        @Override
        protected IAjaxCallDecorator getAjaxCallDecorator() {
            return new NoAjaxIndicatorDecorator();
        }

        @Override
        protected void onUpdate(AjaxRequestTarget target) {
            final Component logoPreview = getComponent().getParent().get("logoPreview");
            target.add(logoPreview);
            CustomizingPanel parent = getFormComponent().getParent().findParent(CustomizingPanel.class);
            if (parent != null) {
                parent.isFakeLogo = false;
                MutableCentralConfigDescriptor descriptor = parent.getDescriptor();
                String logoUrl = descriptor.getLogo();
                if (isImageFake(logoUrl, parent)) {
                    parent.error("The File URL Contain a Non Valid Image Format");
                } else {
                    parent.info("Logo Uploaded Successfully. Please remember to save your changes.");
                }
                AjaxUtils.refreshFeedback(target);
            }
        }

        private boolean isImageFake(String logoUrl, CustomizingPanel parent) {
            boolean isFakeImage = false;
            ImageInputStream imageInputStream;
            if (logoUrl != null) {
                try {
                    byte[] imageByte = downloadImageFromURL(logoUrl);
                    if (imageByte != null) {
                        imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(imageByte));
                        Iterator<ImageReader> iter = ImageIO.getImageReaders(imageInputStream);
                        if (!iter.hasNext()) {
                            isFakeImage = true;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (isFakeImage) {
                    isFakeImage = true;
                    parent.isFakeLogo = true;
                }
            }
            return isFakeImage;
        }

        /**
         * download image from specific url
         *
         * @param strUrl - download image url link
         * @return byte [] of image
         */
        protected byte[] downloadImageFromURL(final String strUrl) {
            InputStream in;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                URL url = new URL(strUrl);
                in = new BufferedInputStream(url.openStream());
                byte[] buf = new byte[2048];
                int n;
                while (-1 != (n = in.read(buf))) {
                    out.write(buf, 0, n);
                }
                out.close();
                in.close();
            } catch (IOException e) {
                return null;
            }
            return out.toByteArray();
        }
    }

    private class ResetLink extends TitledAjaxLink {
        private LogoFileUploadForm form;

        public ResetLink(String id, LogoFileUploadForm form) {
            super(id, "Clear");
            this.form = form;
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            deleteLogo = true;
            cleanup();
            info("Custom logo has been reset. Please remember to save your changes.");
            getDescriptor().setLogo(null);
            target.add(form);
            AjaxUtils.refreshFeedback(target);
        }
    }
}
