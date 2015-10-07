import {jfAccordion}     from './jf_accordion/jf_accordion';
import {jfCode}          from './jf_code/jf_code';
import {jfFooter}        from './jf_footer/jf_footer';
import {jfHeader}        from './jf_header/jf_header';
import {jfMessages}      from './jf_messages/jf_messages';
import {jfHeaderSearch}  from './jf_header_search/jf_header_search';
import {jfSidebar}       from './jf_sidebar/jf_sidebar';
import {jfDragDrop}      from './jf_drag_drop/jf_drag_drop'
import {jfBrowseFiles}   from './jf_browse_files/jf_browse_files';
import dynamicDirective  from './jf_dynamic_directive/jf_dynamic_directive';
import {jfActions}       from './jf_actions/jf_actions';
import {jfTooltip}       from './jf_tooltip/jf_tooltip';
import {jfHelpTooltip}       from './jf_help_tooltip/jf_help_tooltip';
import {jfSearch}        from './jf_search/jf_search';
import {jfList}          from './jf_list/jf_list';
import searchTabs        from './jf_search/search_tabs/search_tabs.module';
import {jfCheckbox}      from './jf_checkbox/jf_checkbox';
import {jfSwitch}        from './jf_switch/jf_switch';
import {jfGrid} from './jf_grid/jf_grid';
import {jfGridPagination} from './jf_grid_pagination/jf_grid_pagination';
import {jfGridBatchActions}   from './jf_grid_batch_actions/jf_grid_batch_actions';
import {ArtifactoryDeployModal}   from './jf_deploy/artifactory_deploy_modal';
import {jfMultiDeploy}   from './jf_deploy/jf_multi_deploy';
import {jfSingleDeploy}   from './jf_deploy/jf_single_deploy';
import {jfPrint}   from './jf_print/jf_print';
import {jfAutoFocus}   from './jf_autofocus/jf_autofocus';
import {jfField}   from './jf_field/jf_field';
import {jfCodeMirror}   from './jf_codemirror/jf_codemirror';
import {jfBodyClass}   from './jf_body_class/jf_body_class';
import {jfInputTextV2}   from './jf_input_text_v2/jf_input_text_v2';
import {jfCronFormatter}   from './jf_cron_formatter/jf_cron_formatter';
import {jfGridFilter}   from './jf_grid_filter/jf_grid_filter';
import {jfBreadcrumb}   from './jf_breadcrumb/jf_breadcrumb';
import {jfTabs}   from './jf_tabs/jf_tabs';
import {jfTab}   from './jf_tabs/jf_tab';
import {jfPanel}   from './jf_panel/jf_panel';
import {jfUiSelect}   from './jf_ui_select/jf_ui_select';
import {jfSpinner}   from './jf_spinner/jf_spinner';
import {jfRevealInput}   from './jf_reveal_input/jf_reveal_input';
import {jfEnterPress}   from './jf_enter_press/jf_enter_press';
import {jfDisableFeature} from './jf_disable_feature/jf_disable_feature';
import {jfHideForAol} from './jf_hide_for_aol/jf_hide_for_aol';
import {jfFileDrop} from './jf_file_drop/jf_file_drop';
import {jfAutoComplete} from './jf_auto_complete/jf_auto_complete';
import {jfClearErrors} from './jf_clear_errors/jf_clear_errors';
import {jfTooltipOnOverflow} from './jf_tooltip_on_overflow/jf_tooltip_on_overflow';
import {jfClipCopy} from './jf_clip_copy/jf_clip_copy';
import {jfDockerV2Layer} from './jf_docker_v2_layer/jf_docker_v2_layer';
import {jfDockerV2Layers} from './jf_docker_v2_layers/jf_docker_v2_layers';

// custom field validators
import {jfValidation}   from './jf_validation/jf_validation';
import {jfValidatorName}   from './validators/jf_validator_name';
import {jfValidatorUniqueId}   from './validators/jf_validator_unique_id';
import {jfValidatorXmlName}   from './validators/jf_validator_xml_name';
import {jfValidatorCron}   from './validators/jf_validator_cron';
import {jfValidatorLdapUrl}   from './validators/jf_validator_ldap_url';
import {jfValidatorPathPattern}   from './validators/jf_validator_path_pattern';
import {jfValidatorIntValue}   from './validators/jf_validator_int_value';
import {jfValidatorMaxTextLength}   from './validators/jf_validator_max_text_length';
import {jfSpecialChars}   from './jf_special_chars/jf_special_chars';
import {jfRepokeyValidator}   from './jf_repokey_validator/jf_repokey_validtaor';
import {jfValidatorDateFormat}   from './validators/jf_validator_date_format';


angular.module('artifactory.directives', ['artifactory.services', 'artifactory.dao', 'searchTabs', 'ui.select', 'ngSanitize', 'ui.highlight'])
    .directive({
        'jfAccordion': jfAccordion,
        'jfCode': jfCode,
        'jfFooter': jfFooter,
        'jfHeader': jfHeader,
        'jfMessages': jfMessages,
        'jfHeaderSearch': jfHeaderSearch,
        'jfSidebar': jfSidebar,
        'jfDragDrop': jfDragDrop,
        'jfBrowseFiles': jfBrowseFiles,
        'dynamicDirective': dynamicDirective,
        'jfActions': jfActions,
        'jfTooltip': jfTooltip,
        'jfHelpTooltip': jfHelpTooltip,
        'jfSearch': jfSearch,
        'jfList': jfList,
        'jfCheckbox': jfCheckbox,
        'jfSwitch': jfSwitch,
        'jfGrid': jfGrid,
        'jfGridPagination': jfGridPagination,
        'jfGridBatchActions': jfGridBatchActions,
            'jfSingleDeploy': jfSingleDeploy,
            'jfMultiDeploy': jfMultiDeploy,
        'jfPrint': jfPrint,
        'jfValidation': jfValidation,
        'jfAutoFocus': jfAutoFocus,
        'jfField': jfField,
        'jfCodeMirror': jfCodeMirror,
        'jfBodyClass': jfBodyClass,
        'jfInputTextV2': jfInputTextV2,
        'jfCronFormatter': jfCronFormatter,
        'jfValidatorName': jfValidatorName,
        'jfValidatorUniqueId': jfValidatorUniqueId,
        'jfValidatorXmlName': jfValidatorXmlName,
        'jfValidatorCron': jfValidatorCron,
        'jfValidatorLdapUrl': jfValidatorLdapUrl,
        'jfValidatorPathPattern': jfValidatorPathPattern,
        'jfValidatorIntValue': jfValidatorIntValue,
        'jfValidatorDateFormat': jfValidatorDateFormat,
        'jfValidatorMaxTextLength': jfValidatorMaxTextLength,
        'jfGridFilter': jfGridFilter,
        'jfBreadcrumb': jfBreadcrumb,
        'jfTabs': jfTabs,
        'jfTab': jfTab,
        'jfSpecialChars': jfSpecialChars,
        'jfPanel': jfPanel,
        'jfUiSelect': jfUiSelect,
        'jfSpinner': jfSpinner,
        'jfRepokeyValidator': jfRepokeyValidator,
        'jfRevealInput': jfRevealInput,
        'jfEnterPress': jfEnterPress,
        'jfDisableFeature': jfDisableFeature,
        'jfHideForAol': jfHideForAol,
        'jfFileDrop': jfFileDrop,
        'jfAutoComplete': jfAutoComplete,
        'jfClearErrors': jfClearErrors,
        'jfTooltipOnOverflow': jfTooltipOnOverflow,
        'jfClipCopy': jfClipCopy,
        'jfDockerV2Layer': jfDockerV2Layer,
        'jfDockerV2Layers': jfDockerV2Layers
        })
        .service('ArtifactoryDeployModal', ArtifactoryDeployModal);