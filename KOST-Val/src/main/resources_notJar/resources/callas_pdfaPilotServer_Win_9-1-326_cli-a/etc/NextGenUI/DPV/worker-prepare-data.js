onmessage = function(e) {
	postMessage(getData(JSON.parse(e.data)));
}

var nodenamelist = [];
const getData = (json) => {
	if(json.aggregated.doc.dpartroot.hasOwnProperty('nodenamelist')) {
		for (var item in json.aggregated.doc.dpartroot.nodenamelist) {
			nodenamelist.push(json.aggregated.doc.dpartroot.nodenamelist[item]);
		}
	}
	if(typeof nodenamelist[0] === 'undefined') {
		nodenamelist.push("Level 1");
	}

	var reduced_json = reduceDpartJson(json.aggregated.doc.dpartroot.dparts[0].dparts, 1);

	var ret_map = [{
		"id": json.aggregated.file.name,
		"name": `File name: ${json.aggregated.file.name}`,
		"props": {
			"icon": "dpart-root",
			"isOpen": true
		},
		"children": [{
			"id": nodenamelist[0],
			"name": nodenamelist[0],
			"props": {
				"icon": "dpart-root",
				"isOpen": true
			},
			"children": reduced_json
		}]
	}];

	return ret_map;
}

let nodes_pages;
let global_counter = 0;
const reduceDpartJson = (obj, depth) =>  Object.entries(obj).reduce((op, [key, val]) => {
	let final = null;
	if(val.hasOwnProperty("start")) {
		if(typeof nodenamelist[depth] === 'undefined') {
			nodenamelist.push(`Level ${depth+1}`);
		}
		final = {
			"id": `id ${++global_counter}`,
			"name": nodenamelist[depth] + ` ${leafGetPageRange(val)}`,
			"props": {
				"icon": "dpart-leaf"
			},
			"children": reduceDpartStart(val)
		}
	}else if(key == 'dpm') {
		final = {
			"id": `id ${++global_counter}`,
			"name": "DPM",
			"props": {
				"icon": "dpart-dpm"
			},
			"children": getDpartDPM(val)
		}
	}else if(key == 'dparts') {
		depth++;
		final = reduceDpartJson(val, depth);
	}else if(typeof val === 'object') {
		if(typeof nodenamelist[depth] === 'undefined') {
			nodenamelist.push(`Level ${depth+1}`);
		}

		if(depth == 1) {
			nodes_pages = leafPagesArray(val);
			nodes_pages = pagesArraySortAndCompress(nodes_pages);
		}

		final = {
			"id": `id ${++global_counter}`,
			"name": nodenamelist[depth] + ` ${nodes_pages}`,
			"props": {
				"icon": "dpart-node"
			},
			"children": reduceDpartJson(val, depth)
		}
	}
	return op.concat(final);
} , []);


const reduceDpartStart = (obj) => Object.entries(obj).reduce((op, [key, val]) => {
	let final = null;
	if(key == 'dpm') {
		final = {
			"id": `id ${++global_counter}`,
			"name": "DPM",
			"props": {
				"icon": "dpart-dpm"
			},
			"children": getDpartDPM(val)
		}
	}else if(key == 'start') {
		if(obj.hasOwnProperty("end")) {
			final = [{
				"id": `id ${++global_counter}`,
				"name": `Start ${obj.start}`,
				"props": {
					"icon": "dpart-pagelink",
					"linkData": obj.start,
					"isLink": true
				}
			}, {
				"id": `id ${++global_counter}`,
				"name": `End ${obj.end}`,
				"props": {
					"icon": "dpart-pagelink",
					"linkData": obj.end,
					"isLink": true
				}
			}]
		}else {
			final = {
				"id": `id ${++global_counter}`,
				"name": `Start ${obj.start}`,
				"props": {
					"icon": "dpart-pagelink",
					"linkData": obj.start,
					"isLink": true
				}
			}
		}
	}else {
		return op;
	}

	return op.concat(final);	
}, []);

const reduceDpartDpm = (obj) => Object.entries(obj).reduce((op, [key, val]) => {
	let final = null;
	if(typeof val !== 'object') {
		final = {
			"id": `id ${++global_counter}`,
			"name": `${key} : ${val}`,
			"props": {
				"icon": "dpart-structure"
			}
		}
	}else {
		final = {
			"id": `id ${++global_counter}`,
			"name": key,
			"props": {
				"icon": "dpart-structure"
			},
			"children": reduceDpartDpm(val)
		}
	}
	return op.concat(final);
}, []);

function getDpartDPM(obj) {
	let final =[{
		"id": `id ${++global_counter}`,
		"name": JSON.stringify(obj),
		"props": {
			"icon": "dpart-structure"
		}
	}]
	return final;
}

function leafGetPageRange(obj) {
	return obj.hasOwnProperty("end") ? `${obj.start} - ${obj.end}` : obj.start;
}

function pagesArraySortAndCompress(arr) {
	arr.sort((a, b) => a - b);
	let ranges = [], one, two, first = arr[0], last = arr[arr.length-1];
	for(var i = 0; i < arr.length; i++) {
		two = one = arr[i];
		for(;arr[i + 1] - arr[i] == 1; i++) {
			two = arr[i + 1];
		}
		ranges.push(one == two ? one : one + ' - ' + two);
	}
	if(ranges.length > 1) {
		return `${first} ... ${last}`;
	}else {
		return ranges;
	}
}

function leafPagesArray(obj) {
	let pages = [];
	leafPagesScan(obj);
	function leafPagesScan(obj) {

		if(obj.hasOwnProperty("start")) {
			if(obj.hasOwnProperty("end")) {
				for(var i = obj.start; i <= obj.end; i++) {
					if(!pages.includes(i)) {
						pages.push(i);
					}
				}
			}else {
				if(!pages.includes(obj.start)) {
					pages.push(obj.start);
				}
			}
		}

		if(obj instanceof Object) {
			for(let k in obj) {
				if(k != "dpm") {
					leafPagesScan(obj[k]);
				}
			}
		}
	}
	return pages;
}


