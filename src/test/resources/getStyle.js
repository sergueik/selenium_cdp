/**
 * Returns computed style of the element or some of its properties
 *
 * arguments[0] {Element} The event target.
 * arguments[1] {String} The name of the property (e.g. 'width').
 */

// http://www.htmlgoodies.com/html5/css/referencing-css3-properties-using-javascript.html#fbid=88eQV8NzD6Q 
// https://developer.mozilla.org/en-US/docs/Web/API/Window/getComputedStyle
getStyle = function(element, property) {
	var result = '';
	if (window.getComputedStyle) {
		var styleObj = window.getComputedStyle(element, null);
		// var styleObj = document.defaultView.getComputedStyle(element, null);   
		if (undefined !=  property ) {
			result = styleObj.getPropertyValue(property);
		} else {
			var len = styleObj.length;
			for (var i = 0; i < len; i++) {
				var style = styleObj[i];
				result += '  ' + style + ':' + styleObj.getPropertyValue(style) + '\n';
			}
		}
	}
	// IE lacks getComputedStyle but has currentStyle 
	else if (element.currentStyle) {
		try {
			result = element.currentStyle[property];
		} catch (e) {}
	}
	return result;
}

var element = arguments[0];
var property = arguments[1];
return getStyle(element, property);