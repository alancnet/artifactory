var Linkable = {
    onclick: function(textarea, e) {
        if (e.ctrlKey) {
            window.open(textarea.value, '_blank')
            return false;
        }
    },
    onmouseover: function(textarea, e) {
        textarea.focus();
        Linkable.onkey(textarea, e)
    },
    onkeyevent: function(textarea, e) {
        textarea.style.cursor = e.ctrlKey ? 'pointer' : '';
    }
};