package org.artifactory.ui.rest.service.utils.validation;

import org.springframework.beans.factory.annotation.Lookup;

/**
 * Factory for UI validator services.
 *
 * @author Yossi Shaul
 */
public abstract class ValidationsServiceFactory {

    @Lookup
    public abstract GetTimeFormatValidatorService getTimeFormatValidatorService();

    @Lookup
    public abstract GetNameValidatorService  getNameValidatorService();

    @Lookup
    public abstract GetXsdNCNameValidatorService getXmlNameValidatorService();

    @Lookup
    public abstract GetUniqueXmlIdValidatorService getUniqueXmlIdValidatorService();

}
