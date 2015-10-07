let events = {
    FORM_SUBMITTED:     'form:submitted',
    FORM_CLEAR_FIELD_VALIDATION: 'form:clear:field'
}

export default events;

let eventNames = {};
Object.keys(events).forEach(key => eventNames[events[key]] = key);
export const COMMON_EVENTS_NAMES = eventNames;
