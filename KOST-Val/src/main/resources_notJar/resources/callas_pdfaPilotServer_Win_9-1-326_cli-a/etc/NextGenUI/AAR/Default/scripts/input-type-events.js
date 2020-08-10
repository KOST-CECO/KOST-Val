function setInputEvents() {
	document.querySelectorAll('.number').forEach(function(element, index) {
		let event = document.createEvent("Event");
		event.initEvent("input");
		let number = element,
			input = number.getElementsByTagName('input')[0],
			buttonUp = number.querySelector('.number-up'),
			buttonDown = number.querySelector('.number-down');

		buttonUp.onclick = function() {
			let oldValue = parseFloat(input.valueAsNumber);
			let newValue = oldValue + 1;
			input.value = newValue;
			input.dispatchEvent(event);
		}

		buttonDown.onclick = function() {
			let oldValue = parseFloat(input.valueAsNumber);
			let newValue = oldValue - 1;
			input.value = newValue;
			input.dispatchEvent(event);
		}

		input.oninput = function() {
			if(input.value == "") {
				addErrorClassToElement(number, input);
			}else if(input.dataset.ranges != "") {
				let rangesArray = input.dataset.ranges.split(" ");
				rangesArray = rangesArray.map(function(value) {
					return parseFloat(value);
				});
				if(numberBetweenRanges(parseFloat(input.value), rangesArray)) {
					removeErrorClassFromElement(number, input.dataset.id);
				}else {
					addErrorClassToElement(number, input);
				}
			}else {
				removeErrorClassFromElement(number, input.dataset.id);
			}
		}
		input.dispatchEvent(event);
	});

	document.querySelectorAll('.boolean').forEach(function(element, index) {
		let sw = element;
		sw.onclick = function() {
			sw.classList.toggle("active");
		}
	});

	document.querySelectorAll('.regex').forEach(function(element, index) {
		let event = document.createEvent("Event");
		event.initEvent("input");

		let regex = element,
			input = regex.getElementsByTagName('input')[0];

		input.oninput = async function() {
			if(await isRegexValid(input.value)) {
				removeErrorClassFromElement(regex, input.dataset.id);
			}else {
				addErrorClassToElement(regex, input);
			}
		}
		input.dispatchEvent(event);
	});

	document.querySelectorAll('.array').forEach(function(element, index) {
		let event = document.createEvent("Event");
		event.initEvent("input");

		let array = element,
			input = array.getElementsByTagName('input')[0];

		input.oninput = function() {
			let valid = true;
			if(input.value != "") {
				if(isJSONValid(input.value)) {
					if(!Array.isArray(JSON.parse(input.value))) {
						valid = false;
					}
				}else {
					valid = false;
				}
			}else {
				valid = false;
			}
			if(valid) {
				removeErrorClassFromElement(array, input.dataset.id);
			}else {
				addErrorClassToElement(array, input);
			}
		}
		input.dispatchEvent(event);
	});

	document.querySelectorAll('.object').forEach(function(element, index) {
		let event = document.createEvent("Event");
		event.initEvent("input");

		let object = element,
			input = object.getElementsByTagName('textarea')[0];

		if(isJSONValid(input.value)) {
			input.value = JSON.stringify(JSON.parse(input.value), undefined, 4);
		}

		input.oninput = function() {
			let valid = true;
			if(input.value != "") {
				if(!isJSONValid(input.value)) {
					valid = false;
				}
			}else {
				valid = false;
			}
			if(valid) {
				removeErrorClassFromElement(object, input.dataset.id);
			}else {
				addErrorClassToElement(object, input);
			}
		}
		input.dispatchEvent(event);
	});

	document.querySelectorAll('.path').forEach(function(element, index) {
		let path = element,
			button = path.querySelector('button'),
			textInput = path.querySelector('input[type="text"]');
		button.onclick = function() {
			browse(textInput.dataset.settings).then(function(response) {
				if(response != "") {
					textInput.value = response;
				}
			});
		}
	});

	document.querySelectorAll('.popup').forEach(function(element, index) {
		let popup = element,
			select = popup.querySelector('select');
		if(select.options.length == 0) {
			addErrorClassToElement(popup, select);
		}
	});

	document.querySelector('#target-scroll-container').addEventListener('scroll', function(event) {
		if(event.target.scrollTop > 200) {
			document.querySelector('.header').classList.add("shrink");
		}else if(event.target.scrollTop < 185) {
			document.querySelector('.header').classList.remove("shrink");
		}
	});

	document.addEventListener('keydown', function(event) {
		const keycode = event.key;
		const control = event.ctrlKey || event.metaKey;

		if(keycode == 'Enter') {
			handleEnter();
		}else if(keycode == 'Escape') {
			handleEscape();
		}else if(control && keycode == 'k') {
			event.preventDefault();
			swapLabel();
		}else if(control && keycode == 'i') {
			event.preventDefault();
			let inspectWrapper = document.querySelector('#inspect-scroll-container .inspect-wrapper');
			if(inspectWrapper.getAttribute('data-in-animation') === "false") {
				inspectWrapper.setAttribute('data-in-animation', 'true');
				toggleUsage(document.querySelector('#inspect-scroll-container .inspect-wrapper'), 220);
			}
		}
	});

	document.querySelector(".usage-modal-container").addEventListener('click', function(event) {
		if(event.target == this) {
			toggleModal();
		}
	});

	document.querySelector('#inspect-scroll-container .inspect-wrapper').addEventListener('animationend', function(event) {
		if(event.currentTarget.style.display === 'block') {
			inspectActive = true;
			setInspectTableTimer();
		}else {
			inspectActive = false;
		}
		event.currentTarget.setAttribute('data-in-animation', 'false');
	});

	document.getElementById('target').addEventListener('keydown', function(event) {
		setInspectTableTimer();
	});

	document.getElementById('target').addEventListener('click', function(event) {
		setInspectTableTimer();
	});

	document.querySelectorAll('.popup-browse').forEach(function(element, index) {
		let popBrowse = element,
			select = popBrowse.querySelector('select'),
			button = popBrowse.querySelector('button');
		button.onclick = function() {
			browse(button.dataset.settings).then(function(response) {
				if(response != "") {
					let optionAlreadyExists = false;
					for(let i = 0; i < select.options.length; i++) {
						if(select.options[i].value == response) {
							optionAlreadyExists = true;
							break;
						}
					}

					if(!optionAlreadyExists) {
						let opt = document.createElement("option");
						opt.text = response;
						opt.value = response;
						opt.selected = true;
						select.add(opt);
					}
				}
			});
		}
	});
}