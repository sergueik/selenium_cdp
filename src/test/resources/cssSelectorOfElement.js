cssSelectorOfElement = function(element) {
	if (!(element instanceof Element))
		return;
	var path = [];
	while (element.nodeType === Node.ELEMENT_NODE) {
		var selector = element.nodeName.toLowerCase();
		if (element.id) {
			if (element.id.indexOf('-') > -1) {
				selector += '[id="' + element.id + '"]';
			} else {
				selector += '#' + element.id;
			}
			path.unshift(selector);
			break;
		} else if (element.className) {
			selector += '.' + element.className.replace(/^\s+/,'').replace(/\s+$/,'').replace(/\s+/g, '.');
		} else {
			var element_sibling = element;
			var sibling_cnt = 1;
			while (element_sibling = element_sibling.previousElementSibling) {
				if (element_sibling.nodeName.toLowerCase() == selector)
					sibling_cnt++;
			}
			if (sibling_cnt != 1)
				selector += ':nth-of-type(' + sibling_cnt + ')';
		}
		path.unshift(selector);
		element = element.parentNode;
	}
	return path.join(' > ');
}
return cssSelectorOfElement(arguments[0]);