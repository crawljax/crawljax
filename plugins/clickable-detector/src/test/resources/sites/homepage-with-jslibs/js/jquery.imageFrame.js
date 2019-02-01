/*
 * jQuery imageFrame plugin
 * Version 1.0  (2008-07-11)
 * @requires jQuery v1.1.3 or above
 * @requires ifixpng plugin (http://jquery.khurshid.com/ifixpng.php)
 *
 * Copyright (c) 2008 Softwyre, Inc. (www.softwyre.com)
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
 * 
 * See imageFrame.css and associated images for example frames.
 */
 

(function($){
	$.fn.imageFrame = function(frameStyle) {
		var frameStyle = $.imageFrame.frameStyles[frameStyle]
			|| $.imageFrame.frameStyles[$.imageFrame.defaultFrameStyle];
		return this.each(function(){
			var selected = $(this);

			if ($.browser.mozilla
				&& $.browser.version.substr(0,2) == '1.')
			{
				if ($.browser.version.substr(2,1) < '8') //Firefox 1.0 and lower
				{
					//Crashes browser
					return;
				}
				else if ($.browser.version.substr(2,1) == '8' //Firefox 2 or 1.5
					&& /t(d|h)/i.test($(this).parent()[0].tagName))
				{
					//this situation causes content loss.  Kill the frame
					return;
				}
			}
			
			//Replace align attribute with float.  Horizontal align causes problems in IE
			var originalAlign = selected.attr('align');
			if (originalAlign) {
				originalAlign = originalAlign.toLowerCase();
				if (originalAlign == 'left')
					selected.css({'float': 'left'});
				else if (originalAlign == 'right')
					selected.css({'float': 'right'});
			}
			
			var originalStyle = {
				'display': selected.css('display'),
				'float': selected.css('float'),
				'clear': selected.css('clear')
			};
					
			//turn any internal floats into left floats for IE.  Do it in all browsers for consistency.
			//NOTE: this fix must occur BEFORE any dimension fixes, because it fixes size issues in IE
			selected.css('float', (originalStyle['float'] != 'none') ? 'left' : 'none');
			
			selected
				//Two spans are added (instead of one) to account for problems with IE6 percentage calculations
				.wrap('<span class="' + $.imageFrame.genericFrameClass + ' ' + frameStyle.frameClass + '"><span class="' + $.imageFrame.genericFrameClass + 'Buffer"></span></span>')
				.parent() //.frameBuffer
				.each(function(){
					$.imageFrame.propagateBufferStyle(originalStyle, $(this));
				})
				.append('<span class="tl"></span><span class="t"><span></span></span><span class="tr"></span><span class="r"><span></span></span><span class="br"></span><span class="b"><span></span></span><span class="bl"></span><span class="l"><span></span></span>')
				.parent() //.frame
				.each(function(){
					var $$ = $(this);
					//Propagate relevant style
					$$.css({
						'display': (originalStyle['display'] == 'inline') ? 'inline-block' : originalStyle['display'],
						'clear': originalStyle['clear'],
						'float': originalStyle['float']
					});
					
					if ($.browser.msie) {
						// transparency fix for IE6-
						if ($.browser.version < 7) {
							$$.find('>span>span').ifixpng();
							$$.find('>span>span>span').ifixpng();
						}
						
						// fix for right floats in shrink wrapped table cells in IE7 causing frame collapse.
						// must occur before dimension fix!
						var parentElement = $$.parent();
						if ($.browser.version == 7
							&& parentElement[0].tagName.toLowerCase() == 'td'
							&& $$.css('float') == 'right'
							&& parentElement.width() <= $$.outerWidth({margin:true})) //This is the "shrink wrapped" check
						{
							$$.css('float', 'left');
						}
						
						// dimension fix - IE6 and 7
						if ($.browser.version < 8) {
							$.imageFrame.fixFrameDimensions($$);
						}
					}
				})
				;
		});
	};
	
	$.imageFrame = {
		genericFrameClass:'frame',
		defaultFrameStyle:'sharp',
		frameStyles: {
			sharp:{
				frameClass:'frameSharp'
			},
			soft:{
				frameClass:'frameSoft'
			}

		},
		fixFrameDimensions: function($$) {
			var frameWidth = $$.outerWidth();
			var frameHeight = $$.outerHeight();

			// Handle rounding errors in IE 6-
			if ($.browser.msie
				&& $.browser.version < 7)
			{
				frameWidth = (frameWidth % 2 == 0) ? frameWidth : frameWidth - 1;
				frameHeight = (frameHeight % 2 == 0) ? frameHeight : frameHeight - 1;
			}
			$$.find('.t').width(frameWidth);
			$$.find('.b').width(frameWidth);
			$$.find('.l').height(frameHeight);
			$$.find('.r').height(frameHeight);
		},
		propagateBufferStyle: function(originalStyle, buffer){
			buffer.css({
				'display': (originalStyle['display'] == 'inline') ? 'inline-block' : originalStyle['display'],
				'float': (originalStyle['float'] != 'none') ? 'left' : 'none' //For IE, right floats must be propagated to the buffer as left floats
			});
			if (buffer.css('float') != 'none') {
				buffer.css({
					'clear': 'both'
				});
			}
		}
	};
})(jQuery);