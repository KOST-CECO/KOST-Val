const renderer = function(node, treeOptions) {
	const { id, name, loadOnDemand = false, children, state, props = {} } = node;
	const droppable = (treeOptions.droppable) && (props.droppable);
	const { depth, open, path, total, selected = false, filtered, checked, indeterminate } = state;
	const childrenLength = Object.keys(children).length;
	const more = node.hasChildren();

	if(filtered === false) {
		return;
	}

	let togglerContent = '';
	if(!more && loadOnDemand) {
		togglerContent = tag('img', {
			'class': 'infinite-tree-toggler-icon sort',
			'src': 'images/sort_right.png'
		}, '');
	}
	if (more && open) {
		togglerContent = tag('img', {
			'class': 'infinite-tree-toggler-icon sort',
			'src': 'images/sort_down.png'
		}, '');
	}
	if (more && !open) {
		togglerContent = tag('img', {
			'class': 'infinite-tree-toggler-icon sort',
			'src': 'images/sort_right.png'
		}, '');
	}
	if(state.expanding && !state.loading) {
		togglerContent = tag('span', {
			'class': 'infinite-tree-toggler-icon refresh',
		}, '⟳');
	}

	const toggler = tag('span', {
		'class': (() => {
			if (!more && loadOnDemand) {
				return treeOptions.togglerClass + ' infinite-tree-closed';
			}
			if (more && open) {
				return treeOptions.togglerClass;
			}
			if (more && !open) {
				return treeOptions.togglerClass + ' infinite-tree-closed';
			}
			return '';
		})()
	}, togglerContent);

	const iconList = {
		"processplan": "images/processplan_32.png",
		"profile": "images/profile_32.png",
		"ruleset": "images/ruleset_32.png",
		"rule": "images/rule_32.png",
		"fixupset": "images/fixupset_32.png",
		"fixup": "images/fixup_32.png",
		"action": "images/action_32.png",
		"condition": "images/condition_32.png",
		"variable": "images/variable_32.png",

		"string": "images/string_32.png",
		"popup": "images/popup_32.png",
		"boolean": "images/boolean_32.png",
		"integer": "images/integer_32.png",
		"float": "images/float_32.png",
		"regex": "images/regex_32.png",
		"file": "images/file_32.png",
		"folder": "images/folder_32.png",

		"none": "images/none_32.png",
		"name": "images/name_32.png",
		"comment": "images/comment_32.png",
		"reportnomatch": "images/reportnomatch_32.png",
		"profilescript": "images/profilescript_32.png",
		"param": "images/param_32.png",
		"onoff": "images/onoff_32.png",
		"severity": "images/severity_32.png",
		"variable": "images/variable_32.png"
	}

	const iconSrc = iconList[props.icon];

	const icon = tag('img', {
		'class': 'infinite-tree-icon',
		'src': iconSrc
	}, '');

	const title = tag('span', {
		'class': 'infinite-tree-title'
	}, name);

	const treeNode = tag('div', {
		'class': 'infinite-tree-node',
		'style': 'margin-left: ' + (depth * 18) + 'px'
	}, (toggler != '<span class=""></span>' ? toggler : "") + icon + title);

	let treeNodeAttributes = {
		'draggable': 'false',
		'data-id': id,
		'data-expanded': more && open,
		'data-depth': depth,
		'data-path': path,
		'data-selected': selected,
		'data-children': childrenLength,
		'data-total': total,
		'class': 'infinite-tree-item' + (selected == true ? ' infinite-tree-selected' : '')
	};

	if(droppable) {
		treeNodeAttributes['droppable'] = true;
	}

	return tag('div', treeNodeAttributes, treeNode);
}