
document.addEventListener("DOMContentLoaded", function(){

	const tree = new InfiniteTree({
		el: document.querySelector('#tree'),
		// data: data,
		autoOpen: false,
		shouldLoadNodes: function(parentNode) {
			return !parentNode.hasChildren() && parentNode.loadNodes;
		},
		loadNodes: (parentNode, next) => {
			next(null, nodes, function() {
				// Completed
			});
		},
		nodeIdAttr: 'data-id',
		rowRenderer: renderer,
		shouldSelectNode: (node) => {
			if(!node || (node === tree.getSelectedNode())) {
				return false; // prevent from deselecting current node
			}
			return true;
		}
	});

	loadJson(function(response) {

		let d = getData(response);
		tree.loadData(d, ["Root"]);
		tree.selectNode(tree.nodes[0]);
		var element = document.getElementById("load");
		element.parentNode.removeChild(element);
		
		// var worker = new Worker('worker-prepare-data.js');
		// worker.postMessage(response);

		// worker.onmessage = function(e) {
		// 	tree.loadData(e.data);
		// 	tree.selectNode(tree.nodes[0]);
		// 	var element = document.getElementById("load");
		// 	element.parentNode.removeChild(element);
		// }
	})

	tree.on('keyDown', (event) => {
		event.preventDefault();

		// const loading = document.querySelector('#loading-layer')
		const node = tree.getSelectedNode();
		const nodeIndex = tree.getSelectedIndex();
		const control = event.ctrlKey || event.metaKey;

		if((control && event.keyCode === 40) || (event.keyCode === 35)) { // jump to bottom
			const lastNode = tree.nodes[tree.nodes.length-1];
			tree.scrollToNode(lastNode);
			if(lastNode != node) {
				tree.selectNode(lastNode);
			}
		}else if((control && event.keyCode === 38) || (event.keyCode === 36)) { // jump to top
			tree.scrollTop(0);
			if(node != tree.nodes[0]) {
				tree.selectNode(tree.nodes[0]);
			}
		}else if(event.keyCode === 37) { // left
			tree.closeNode(node);
		}else if(event.keyCode === 38) { // top
			const prevNode = tree.nodes[nodeIndex - 1] || node;
			if(prevNode != node) {
				tree.selectNode(prevNode);
			}
		}else if(event.keyCode === 39) { // right
			tree.openNode(node);
		}else if(event.keyCode === 40) { // down
			const nextNode = tree.nodes[nodeIndex + 1] || node;
			if(nextNode != node) {
				tree.selectNode(nextNode);
			}
		}else if(control && event.shiftKey && event.keyCode === 79) { // c
			if(selectedNodes.length) {
				for(var i in selectedNodes) {
					openNodesRecursive(tree, selectedNodes[i]);	
				}	
			}else {
				openNodesRecursive(tree, node);
			}
		}else if(control && event.shiftKey && event.keyCode === 67) { // c
			if(selectedNodes.length) {
				for(var i in selectedNodes) {
					closeNodesRecursive(tree, selectedNodes[i]);	
				}	
			}else {
				if(collectCloseNodesRecursive(node)) {
					closeNodes(tree);
				}
			}
		}else if(control && event.keyCode === 67) { // c
			if(!selectedNodes.length) {
				let initialDepth = tree.state.selectedNode.state.depth;
				let text =  getCopyTextRecursive(tree.state.selectedNode, initialDepth);
				copyStringToClipboard(text);
			}
		}
	});

	let selectedNodes = [];
	tree.on('click', (event) => {

		const currentNode = tree.getNodeFromPoint(event.clientX, event.clientY);
		if(!currentNode) {
			return;
		}
		const multipleSelectionMode = event.ctrlKey || event.metaKey;

		if(!multipleSelectionMode) {
			if(selectedNodes.length > 0) {
				event.stopPropagation();

				selectedNodes.forEach(selectedNode => {
					selectedNode.state.selected = false;
					tree.updateNode(selectedNode, {}, {shallowRendering: true});
				});
				selectedNodes = [];

				tree.state.selectedNode = currentNode;
				currentNode.state.selected = true;
				tree.updateNode(currentNode, {}, { shallowRendering: true });
			}
			return;
		}

		event.stopPropagation();
		const selectedNode = tree.getSelectedNode();

		if(selectedNodes.length === 0 && selectedNode) {
			selectedNodes.push(selectedNode);
			tree.state.selectedNode = null;
		}

		const index = selectedNodes.indexOf(currentNode);
		if(index >= 0 && selectedNodes.length > 1) {
			currentNode.state.selected = false;
			selectedNodes.splice(index, 1);
			tree.updateNode(currentNode, {}, {shallowRendering: true});
		}

		if(index < 0) {
			currentNode.state.selected = true;
			selectedNodes.push(currentNode);
			tree.updateNode(currentNode, {}, {shallowRendering: true});
		}

	});

	tree.on('doubleClick', function(event) {
		// führt manchmal zum schließen von parent nodes muss weiter geprüft werden
		// if(selectedNodes.length == 0) {
		// 	let node = tree.getSelectedNode();
		// 		if(node.state.open) {
		// 			tree.closeNode(node);
		// 		}else {
		// 			tree.openNode(node);
		// 	}
		// }
	});

	var modal = document.querySelector(".shortcut-modal");
	var trigger = document.querySelector(".hovicon");
	var closeButton = document.querySelector(".close-container");

	function toggleModal() {
		modal.classList.toggle("show-modal");
	}

	function windowOnClick(event) {
		if (event.target === modal) {
			toggleModal();
		}
	}

	trigger.addEventListener("click", toggleModal);
	closeButton.addEventListener("click", toggleModal);
	window.addEventListener("click", windowOnClick);
});

function getCopyTextRecursive(node, initialDepth) {
	let ret = "";

	ret = '\t'.repeat((node.state.depth-initialDepth)) + node.name + '\n';

	for(var i in node.children) {
		if(node.state.open) {
			ret += getCopyTextRecursive(node.children[i], initialDepth);
		}
	}
	
	return ret;
}

function openNodesRecursive(tree, node) {
	if(node.hasChildren()) {
		if(node.state.open === false) {
			tree.openNode(node);
		}
		for(var i in node.children) {
			openNodesRecursive(tree, node.children[i]);
		}
	}
}

let closeList = [];
function collectCloseNodesRecursive(node) {
	if(node.hasChildren()) {
		if(node.state.open === true) {
			closeList.push(node);
		}
		for(var i in node.children) {
			collectCloseNodesRecursive(node.children[i]);
		}
	}
	return true;
}

function closeNodes(tree) {
	closeList.reverse(); 
	if(closeList.length != 0) {
		for(var i in closeList) {
			tree.closeNode(closeList[i]);
		}
		closeList = [];
	}
}

function loadJson(callback, data) {
	if (typeof window.sendExtensionMessage === "function") { //CEF
		return new Promise((resolve, reject) => {
			window.sendExtensionMessage({
				request: {
					method:'loadDefault',
					data
				},
				onSuccess: (data) => {
					try {
						let value;
						if (data != null && data.value != null && data.contentType != null) {
							switch (data.contentType) {
								case 'json':
									value = JSON.parse(data.value);
									break;
								case 'string':
									value = data.value;
									break;
								default:
									// reject(new Error(`Received unknown contentType ${data.contentType}, ignoring`));
									break;
							}
						}
						callback(value);
						// resolve(value);
					} catch (e) {
						// reject(e);
					}
				},
				onFailure: (code, message) => {
					// reject(new Error(message));
				}
			});
		});
	}else {
		var xobj = new XMLHttpRequest();
		xobj.overrideMimeType("application/json");
		xobj.onreadystatechange = function() {
			if(this.readyState == 4 && this.status == 200) {
				callback(this.responseText);
			}
		}
		xobj.open('POST', 'Cerebellum V1.0.1 - 10.json', true);
		xobj.send();
	}
}

//pagenum is zero based!
function gotoPage(pagenum) {
	if(typeof window.sendExtensionMessage === "function") { //CEF
		return new Promise((resolve, reject) => {
			window.sendExtensionMessage({
				request: {
					method:'gotoPage',
					data: { page: pagenum }
				}
			});
		});
	}
}

function copyStringToClipboard(str) {
	var el = document.createElement('textarea');
	el.value = str;
	el.setAttribute('readonly', '');
	el.style = {position: 'absolute', left: '-9999px'};
	document.body.appendChild(el);
	el.select();
	document.execCommand('copy');
	document.body.removeChild(el);
}