import TreeConfig from '../jf_common_browser/jf_common_browser.config';
let conf = _.cloneDeep(TreeConfig);
_.extend(conf.core,{'check_callback':true});
export default conf;
