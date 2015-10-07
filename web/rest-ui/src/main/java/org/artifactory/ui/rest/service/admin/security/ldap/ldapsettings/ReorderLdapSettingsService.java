package org.artifactory.ui.rest.service.admin.security.ldap.ldapsettings;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReorderLdapSettingsService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(ReorderLdapSettingsService.class);

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        List<String> newOrderList = request.getModels();
        log.debug("Changing order of ldap settings by new input: {}", newOrderList);
        if (CollectionUtils.isNullOrEmpty(newOrderList)) {
            log.debug("Got empty list from UI - aborting");
            return;
        }
        MutableCentralConfigDescriptor descriptor = centralConfigService.getMutableDescriptor();
        SecurityDescriptor security = descriptor.getSecurity();
        List<LdapSetting> listToSave = Lists.newArrayList();

        //Map ldap settings by key and use the input (ordered list) to insert into actual list
        Map<String, LdapSetting> ldapSettingNameMap = Maps.newHashMap();
        security.getLdapSettings().stream()
                .forEach(ldapSetting -> ldapSettingNameMap.put(ldapSetting.getKey(), ldapSetting));
        newOrderList.stream()
                .forEach(ldapSettingKey -> listToSave.add(ldapSettingNameMap.get(ldapSettingKey)));
        security.setLdapSettings(listToSave);
        descriptor.setSecurity(security);
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
    }
}