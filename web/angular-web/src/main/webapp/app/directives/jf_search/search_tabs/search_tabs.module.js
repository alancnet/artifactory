import {jfQuick}    from './jf_quick'
import {jfClass}    from './jf_class'
import {jfGavc}     from './jf_gavc'
import {jfProperty} from './jf_property'
import {jfChecksum} from './jf_checksum'
import {jfRemote}   from './jf_remote'

export default angular.module('searchTabs', [])
        .directive({
            'jfQuick': jfQuick,
            'jfClass': jfClass,
            'jfGavc' : jfGavc,
            'jfProperty':jfProperty,
            'jfChecksum': jfChecksum,
            'jfRemote':jfRemote
        })