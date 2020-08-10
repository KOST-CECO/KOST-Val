var tag = (function() {
	const voidElements = [
		'area',
		'base',
		'br',
		'col',
		'embed',
		'hr',
		'img',
		'input',
		'keygen',
		'link',
		'meta',
		'param',
		'source',
		'track',
		'wbr'
	];

	return tag = (tag, attrs, text) => {
		if (typeof tag === 'object') {
			text = attrs;
			attrs = tag;
			tag = 'div';
		}

		let voidElement = voidElements.indexOf(('' + tag).toLowerCase()) >= 0;
		let html = '<' + tag;

		attrs = { ...attrs };
		Object.keys(attrs).forEach(name => {
			let value = attrs[name];
			if (typeof value === 'string') {
				value = escapeHTML('' + value);
				html += ' ' + name + '="' + value + '"';
			} else if (!!value) {
				html += ' ' + name;
			}
		});

		if (voidElement) {
			html += '>';
		} else if (text !== undefined) {
			html += '>' + text + '</' + tag + '>';
		} else {
			html += '/>';
		}

		return html;
	};
}());