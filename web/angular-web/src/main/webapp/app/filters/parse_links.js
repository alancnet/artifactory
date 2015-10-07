export function ParseLinks() {

    return function(str) {
        return str.replace(/\[(.*?)\]/g, function (match) {
            let linkData = match.substring(1, match.length - 1).split(',');
            if (linkData.length == 2)
                return '<a href="' + linkData[0].trim() + '" target="_blank">' + linkData[1].trim() + '</a>';
            else
                return match;
        });
    }
}