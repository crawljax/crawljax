package com.crawljax.vips_selenium;

public class Scripts {

  public static String VIPS_SCIRPT = "\n" +
      "function isVisible(elem) {\n" +
      "    if (!(elem instanceof Element)) throw Error('DomUtil: elem is not an element.');\n" +
      "    const style = getComputedStyle(elem);\n" +
      "    if (style.display === 'none') return false;\n" +
      "    if (style.visibility !== 'visible') return false;\n" +
      "    if (style.opacity < 0.1) return false;\n" +
      "    if (elem.offsetWidth + elem.offsetHeight + elem.getBoundingClientRect().height +\n" +
      "        elem.getBoundingClientRect().width === 0) {\n" +
      "        return false;\n" +
      "    }\n" +
      "    const elemCenter   = {\n" +
      "        x: elem.getBoundingClientRect().left + elem.offsetWidth / 2,\n" +
      "        y: elem.getBoundingClientRect().top + elem.offsetHeight / 2\n" +
      "    };\n" +
      "    if (elemCenter.x < 0) return false;\n" +
      "    if (elemCenter.x > (document.documentElement.clientWidth || window.innerWidth)) return false;\n"
      +
      "    if (elemCenter.y < 0) return false;\n" +
      "    if (elemCenter.y > (document.documentElement.clientHeight || window.innerHeight)) return false;\n"
      +
      "    let pointContainer = document.elementFromPoint(elemCenter.x, elemCenter.y);\n" +
      "    do {\n" +
      "        if (pointContainer === elem) return true;\n" +
      "    } while (pointContainer = pointContainer.parentNode);\n" +
      "    return false;\n" +
      "}\n" +
      "\n" +
      "function getDisplayed(elem) {\n" +
      "    return !!( elem.offsetWidth || elem.offsetHeight || elem.getClientRects().length );\n" +
      "}\n" +
      "\n" +
      "function computeContentRect(a){\n" +
      "	var map1 = a.computedStyleMap();\n" +
      "	var padding_left = map1.get('padding-left').value;\n" +
      "	var padding_right = map1.get('padding-right').value;\n" +
      "	var border_left_width = map1.get('border-left-width').value;\n" +
      "	var border_right_width = map1.get('border-right-width').value;\n" +
      "	var padding_top = map1.get('padding-top').value;\n" +
      "	var padding_bottom = map1.get('padding-bottom').value ;\n" +
      "	var border_top_width = map1.get('border-top-width').value;\n" +
      "	var border_bottom_width = map1.get('border-bottom-width').value ;\n" +
      "	var width = a.getBoundingClientRect().width -  (padding_left + padding_right + border_left_width + border_right_width );\n"
      +
      "	var height = a.getBoundingClientRect().height - (padding_top + padding_bottom  + border_top_width + border_bottom_width);\n"
      +
      "	var x = scrollX + a.getBoundingClientRect().x + (padding_left +  border_left_width);\n" +
      "	var y = scrollY + a.getBoundingClientRect().y + (padding_top + border_top_width);\n" +
      "	var returnString = { x : Math.round(x), y:  Math.round(y), width:  Math.round(width) , height:  Math.round(height) };\n"
      +
      "	// console.log(returnString);\n" +
      "	return returnString;\n" +
      "};\n" +
      "\n" +
      "function getVipsAttributes(xpath){\n" +
      "	// a= $x(xpath)[0];\n" +
      "	result = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);\n"
      +
      "	a = result.singleNodeValue;\n" +
      "	var returnMap = {};\n" +
      "	if(a==null){\n" +
      "		return returnMap;\n" +
      "	}\n" +
      "	var map1 = a.computedStyleMap();\n" +
      "	returnMap['rectangle'] = computeContentRect(a);\n" +
      "	var fontSize=map1.get('font-size').value;\n" +
      "	if(!isNaN(fontSize)){\n" +
      "		returnMap['fontsize'] = Math.round(fontSize);\n" +
      "	}else{\n" +
      "		returnMap['fontsize'] = 0;\n" +
      "	}\n" +
      "\n" +
      "	var fontWeight = map1.get('font-weight').value;\n" +
      "	if(!isNaN(fontSize)){\n" +
      "		returnMap['fontweight'] = Math.round(fontWeight);\n" +
      "	}else{\n" +
      "		returnMap['fontweight'] = 0;\n" +
      "	}\n" +
      "\n" +
      "	returnMap['bgcolor'] = a.bgColor;\n" +
      "	returnMap['isdisplayed'] = getDisplayed(a);\n" +
      "	return returnMap;\n" +
      "}";

  public static String CDP_SCRIPT = "\n" +
      "function isVisible(elem) {\n" +
      "    if (!(elem instanceof Element)) throw Error('DomUtil: elem is not an element.');\n" +
      "    const style = getComputedStyle(elem);\n" +
      "    if (style.display === 'none') return false;\n" +
      "    if (style.visibility !== 'visible') return false;\n" +
      "    if (style.opacity < 0.1) return false;\n" +
      "    if (elem.offsetWidth + elem.offsetHeight + elem.getBoundingClientRect().height +\n" +
      "        elem.getBoundingClientRect().width === 0) {\n" +
      "        return false;\n" +
      "    }\n" +
      "    const elemCenter   = {\n" +
      "        x: elem.getBoundingClientRect().left + elem.offsetWidth / 2,\n" +
      "        y: elem.getBoundingClientRect().top + elem.offsetHeight / 2\n" +
      "    };\n" +
      "    if (elemCenter.x < 0) return false;\n" +
      "    if (elemCenter.x > (document.documentElement.clientWidth || window.innerWidth)) return false;\n"
      +
      "    if (elemCenter.y < 0) return false;\n" +
      "    if (elemCenter.y > (document.documentElement.clientHeight || window.innerHeight)) return false;\n"
      +
      "    let pointContainer = document.elementFromPoint(elemCenter.x, elemCenter.y);\n" +
      "    do {\n" +
      "        if (pointContainer === elem) return true;\n" +
      "    } while (pointContainer = pointContainer.parentNode);\n" +
      "    return false;\n" +
      "}\n" +
      "\n" +
      "function getDisplayed(elem) {\n" +
      "    return !!( elem.offsetWidth || elem.offsetHeight || elem.getClientRects().length );\n" +
      "}\n" +
      "\n" +
      "function computeContentRect(a){\n" +
      "	var map1 = a.computedStyleMap();\n" +
      "	var padding_left = map1.get('padding-left').value;\n" +
      "	var padding_right = map1.get('padding-right').value;\n" +
      "	var border_left_width = map1.get('border-left-width').value;\n" +
      "	var border_right_width = map1.get('border-right-width').value;\n" +
      "	var padding_top = map1.get('padding-top').value;\n" +
      "	var padding_bottom = map1.get('padding-bottom').value ;\n" +
      "	var border_top_width = map1.get('border-top-width').value;\n" +
      "	var border_bottom_width = map1.get('border-bottom-width').value ;\n" +
      "	var width = a.getBoundingClientRect().width -  (padding_left + padding_right + border_left_width + border_right_width );\n"
      +
      "	var height = a.getBoundingClientRect().height - (padding_top + padding_bottom  + border_top_width + border_bottom_width);\n"
      +
      "	var x = scrollX + a.getBoundingClientRect().x + (padding_left +  border_left_width);\n" +
      "	var y = scrollY + a.getBoundingClientRect().y + (padding_top + border_top_width);\n" +
      "	var returnString = { x : Math.round(x), y:  Math.round(y), width:  Math.round(width) , height:  Math.round(height) };\n"
      +
      "	// console.log(returnString);\n" +
      "	return returnString;\n" +
      "};\n" +
      "\n" +
      "function getVipsAttributes(xpath){\n" +
      "	// a= $x(xpath)[0];\n" +
      "	result = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);\n"
      +
      "	a = result.singleNodeValue;\n" +
      "	var returnMap = {};\n" +
      "	if(a==null){\n" +
      "		return returnMap;\n" +
      "	}\n" +
      "	var map1 = a.computedStyleMap();\n" +
      "	returnMap['rectangle'] = computeContentRect(a);\n" +
      "	var fontSize=map1.get('font-size').value;\n" +
      "	if(!isNaN(fontSize)){\n" +
      "		returnMap['fontsize'] = Math.round(fontSize);\n" +
      "	}else{\n" +
      "		returnMap['fontsize'] = 0;\n" +
      "	}\n" +
      "\n" +
      "	var fontWeight = map1.get('font-weight').value;\n" +
      "	if(!isNaN(fontSize)){\n" +
      "		returnMap['fontweight'] = Math.round(fontWeight);\n" +
      "	}else{\n" +
      "		returnMap['fontweight'] = 0;\n" +
      "	}\n" +
      "\n" +
      "	returnMap['bgcolor'] = a.bgColor;\n" +
      "	returnMap['isdisplayed'] = getDisplayed(a);\n" +
      "\n" +
      "	if(getEventListeners(a)['click']){\n" +
      "		returnMap['eventListeners'] =  getEventListeners(a)['click'][0].listener.toString()\n" +
      "	} \n" +
      "\n" +
      "	return returnMap;\n" +
      "}";
}
