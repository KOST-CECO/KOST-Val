
const renderer = (node, treeOptions) => {
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
		// togglerContent = '►';
		togglerContent = tag('img', {
			'class': classNames('infinite-tree-toggler-icon', 'sort'),
			'src': 'src/sort_right.png'
		}, '');
	}
	if (more && open) {
		// togglerContent = '▼';
		togglerContent = tag('img', {
			'class': classNames('infinite-tree-toggler-icon', 'sort'),
			'src': 'src/sort_down.png'
		}, '');
	}
	if (more && !open) {
		// togglerContent = '►';
		togglerContent = tag('img', {
			'class': classNames('infinite-tree-toggler-icon', 'sort'),
			'src': 'src/sort_right.png'
		}, '');
	}


	if(state.expanding && !state.loading) {
		// togglerContent = '⟳';
		togglerContent = tag('span', {
			'class': classNames('infinite-tree-toggler-icon', 'refresh'),
		}, '⟳');
	}


	const toggler = tag('a', {
		'class': (() => {
			if (!more && loadOnDemand) {
				return classNames(treeOptions.togglerClass, 'infinite-tree-closed');
			}
			if (more && open) {
				return classNames(treeOptions.togglerClass);
			}
			if (more && !open) {
				return classNames(treeOptions.togglerClass, 'infinite-tree-closed');
			}
			return '';
		})()
	}, togglerContent);


	let iconSrc = 'src/DPart_Root_2.png';

	if(props.icon == 'dpart-node') {
		iconSrc = 'src/DPart_Node_2.png';
	}else if(props.icon == 'dpart-leaf') {
		iconSrc = 'src/DPart_Leaf_2.png';
	}else if(props.icon == 'dpart-dpm') {
		iconSrc = 'src/DPart_DPM_2.png';
	}else if(props.icon == 'dpart-structure') {
		iconSrc = 'src/DPart_Structure_2.png';
	}else if(props.icon == 'dpart-pagelink') {
		iconSrc = 'src/DPart_PageLink_2.png';
	}

	const icon = tag('img', {
		'class': 'infinite-tree-icon',
		'src': iconSrc
	}, '');


	let title = undefined;

	if(props.hasOwnProperty("isLink")) {
		if(props.isLink == true) {
			title = tag('span', {
				'class': classNames('infinite-tree-link'),
				'onclick': `gotoPage(${(props.linkData-1).toString()})`
			}, name);
		}
	}else {
		title = tag('span', {
			'class': classNames('infinite-tree-title')
		}, escapeHTML(loadOnDemand ? '(loadOnDemand) ' + name : name));
	}

	const treeNode = tag('div', {
		'class': 'infinite-tree-node',
		'style': 'margin-left: ' + (depth * 18) + 'px'
	}, (toggler != '<a class=""></a>' ? toggler : "") + icon + title);

	let treeNodeAttributes = {
		'draggable': 'false',
		'data-id': id,
		'data-expanded': more && open,
		'data-depth': depth,
		'data-path': path,
		'data-selected': selected,
		'data-children': childrenLength,
		'data-total': total,
		'class': classNames('infinite-tree-item', { 'infinite-tree-selected': selected }),
	};

	if (droppable) {
		treeNodeAttributes['droppable'] = true;
	}

	return tag('div', treeNodeAttributes, treeNode);
}