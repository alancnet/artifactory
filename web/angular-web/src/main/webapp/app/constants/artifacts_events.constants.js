let events = {
    ACTIVATE_TREE_SEARCH:   'tree:search:activate',
    TREE_SEARCH_CHANGE:     'tree:search:change',
    TREE_NODE_SELECT:       'tree:node:select',
    TREE_NODE_OPEN:         'tree:node:open',
    TREE_SEARCH_KEYDOWN:    'tree:search:keydown',
    TREE_SEARCH_CANCEL:     'tree:search:cancel',
    TREE_COMPACT:           'tree:compact',
    TREE_REFRESH:           'tree:refresh',
    SEARCH_COLLAPSE:        'search:collapse',
    SEARCH:                 'search',
    CLEAR_SEARCH:           'search:clear',

    ACTION_WATCH:           'action:watch',    // node
    ACTION_UNWATCH:         'action:unwatch',  // node
    ACTION_COPY:            'action:copy',     // node, target
    ACTION_MOVE:            'action:move',     // node, target
    ACTION_COPY_STASH:            'action:copystash',     // repoKey
    ACTION_MOVE_STASH:            'action:movestash',     // repoKey
    ACTION_DISCARD_STASH:         'action:discardstash',     //
    ACTION_DISCARD_FROM_STASH:         'action:discardfromstash',     //node
    ACTION_REFRESH_STASH:         'action:refreshstash',     //
    ACTION_EXIT_STASH:         'action:exitstash',     //
    ACTION_DELETE:          'action:delete',   // node
    ACTION_REFRESH:         'action:refresh',   // node
    ACTION_DEPLOY:         'action:deploy',   // repoKey

    BUILDS_BREADCRUMBS:     'builds:breadcrumbs',

    LOGO_UPDATED: 'logo:updated',

    SHOW_SPINNER: 'spinner:show',
    HIDE_SPINNER: 'spinner:hide',
    CANCEL_SPINNER: 'spinner:cancel',

    USER_CHANGED:           'user:changed',

    TABS_REFRESH:           'tabs:refresh',
    TAB_NODE_CHANGED:       'tabs:node:changed',

    ARTIFACT_URL_CHANGED:   'artifact:url:changed',
    SEARCH_URL_CHANGED:     'search:url:changed',

    CLOSE_MODAL:            'modal:close',

    FOOTER_REFRESH:         'footer:refresh'
};

export default events;

let eventNames = {};
Object.keys(events).forEach(key => eventNames[events[key]] = key);
export const EVENTS_NAMES = eventNames;
