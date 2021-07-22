
function isVisible(elem) {
    if (!(elem instanceof Element)) throw Error('DomUtil: elem is not an element.');
    const style = getComputedStyle(elem);
    if (style.display === 'none') return false;
    if (style.visibility !== 'visible') return false;
    if (style.opacity < 0.1) return false;
    if (elem.offsetWidth + elem.offsetHeight + elem.getBoundingClientRect().height +
        elem.getBoundingClientRect().width === 0) {
        return false;
    }
    const elemCenter   = {
        x: elem.getBoundingClientRect().left + elem.offsetWidth / 2,
        y: elem.getBoundingClientRect().top + elem.offsetHeight / 2
    };
    if (elemCenter.x < 0) return false;
    if (elemCenter.x > (document.documentElement.clientWidth || window.innerWidth)) return false;
    if (elemCenter.y < 0) return false;
    if (elemCenter.y > (document.documentElement.clientHeight || window.innerHeight)) return false;
    let pointContainer = document.elementFromPoint(elemCenter.x, elemCenter.y);
    do {
        if (pointContainer === elem) return true;
    } while (pointContainer = pointContainer.parentNode);
    return false;
}

function getDisplayed(elem) {
    return !!( elem.offsetWidth || elem.offsetHeight || elem.getClientRects().length );
}

function computeContentRect(a){
	var map1 = a.computedStyleMap();
	var padding_left = map1.get('padding-left').value;
	var padding_right = map1.get('padding-right').value;
	var border_left_width = map1.get('border-left-width').value;
	var border_right_width = map1.get('border-right-width').value;
	var padding_top = map1.get('padding-top').value;
	var padding_bottom = map1.get('padding-bottom').value ;
	var border_top_width = map1.get('border-top-width').value;
	var border_bottom_width = map1.get('border-bottom-width').value ;
	var width = a.getBoundingClientRect().width -  (padding_left + padding_right + border_left_width + border_right_width );
	var height = a.getBoundingClientRect().height - (padding_top + padding_bottom  + border_top_width + border_bottom_width);
	var x = scrollX + a.getBoundingClientRect().x + (padding_left +  border_left_width);
	var y = scrollY + a.getBoundingClientRect().y + (padding_top + border_top_width);
	var returnString = { x : Math.round(x), y:  Math.round(y), width:  Math.round(width) , height:  Math.round(height) };
	// console.log(returnString);
	return returnString;
};

function getVipsAttributes(xpath){
	// a= $x(xpath)[0];
	result = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);
	a = result.singleNodeValue
	var returnMap = {};
	if(isNaN(a)){
		returnMap['error'] = "NaN";
		return returnMap;
	}
	var map1 = a.computedStyleMap();
	returnMap['rectangle'] = computeContentRect(a);
	var fontSize=map1.get("font-size").value;
	if(!isNaN(fontSize)){
		returnMap['fontsize'] = Math.round(fontSize);
	}else{
		returnMap['fontsize'] = 0;
	}

	var fontWeight = map1.get('font-weight').value;
	if(!isNaN(fontSize)){
		returnMap['fontweight'] = Math.round(fontWeight);
	}else{
		returnMap['fontweight'] = 0;
	}

	returnMap['bgcolor'] = a.bgColor;
	returnMap['isdisplayed'] = getDisplayed(a);
	return returnMap;
}

