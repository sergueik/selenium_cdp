cssSelectorOfElement = function(element) {

	if (element == document)
		return ('body');
	else if (element.id)
		return (element.tagName.toLowerCase() + '#' + element.id);

	var selector = element.tagName.toLowerCase();
	var angular_attribute = ['ng-model', 'ng-href', 'name', 'aria-label'].reduce(function(a, b) {
		return a || (element.getAttribute(b) ? b : null);
	}, null);
	if (angular_attribute != null) {
		selector += '[' + angular_attribute + '="' +
			element.getAttribute(angular_attribute).replace(/\\/g, '\\\\').replace(/\'/g, '\\\'').replace(/\"/g, '\\"').replace(/\0/g, '\\0') + '"]';
	} else if (element.className) {
		selector += '.' + element.className.replace(/^\s+/, '').replace(/\s+$/, '').replace(/\s+/g, '.');
	}
	// NOTE: using element.parentNode.querySelectorAll() to count siblings and using the ':nth-of-type(' selector suffix
  // is somewhat coarse for attribute-less nodes
	var nodes = element.parentNode.querySelectorAll(selector);
	if (nodes && nodes.length > 1)
		var elementIndex = Array.prototype.slice.call(nodes).indexOf(element);
	if (elementIndex > 0) {
		selector += ':nth-of-type(' + (elementIndex + 1).toString() + ')';
	}
	selector = selector.replace(/\s/g, ' ');
	if (element.parentNode)
		selector = cssSelectorOfElement(element.parentNode) + ' > ' + selector;
	return selector;
};

return cssSelectorOfElement(arguments[0]);