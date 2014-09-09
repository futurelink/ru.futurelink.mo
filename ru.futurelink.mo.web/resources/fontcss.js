/**
 * Font CSS loader script
 */

var $ = document; // shortcut
var cssId = 'fontCSS';  // you could encode the css path itself to generate id.
if (!$.getElementById(cssId)) {
	var head  = $.getElementsByTagName('head')[0];
	var link  = $.createElement('link');
	link.id   = cssId;
	link.rel  = 'stylesheet';
	link.type = 'text/css';
	link.href = 'https://fluvio.ru/static/fonts/roboto.css';
	link.media = 'all';
	head.appendChild(link);
}
