/*
	This file should only contain Mustache.js templates.
	These are needed to dynamically load the custom input fields.

	You can learn more about Mustache.js on their Github.
	https://github.com/janl/mustache.js
*/

let templateException = `<div class="exception">
	<h1>Error!</h1>
	<button onclick="removeException(this)"></button>
	<p>{{ex}}</p>
</div>`;

let templateBoolean = `<div class="row">
	<div class="col-s-4">
		<span class="input-label" data-label-swap-against="{{id}}">{{label}}</span>
	</div>
	<div class="col-s-8">
		<div class="usage-input-container">
			<button class="boolean {{active}}" data-type="boolean" data-index="{{index}}" data-tooltip="{{tooltip}}" data-id="{{id}}">
				<div class="circle"></div>
			</button>
			<div class="usage-button-anim-container">
				<button class="usage-button" onclick="loadUsageModal('{{id}}');" {{usageButtonActive}}>i</button>
			</div>
		</div>
	</div>
</div>`;

let templatePopup = `<div class="row">
	<div class="col-s-4">
		<span class="input-label" data-label-swap-against="{{id}}">{{label}}</span>
	</div>
	<div class="col-s-8">
		<div class="usage-input-container">
			<div class="popup">
				<select data-id="{{id}}" data-type="popup" data-index="{{index}}" data-tooltip="{{tooltip}}">
					{{#values}}
						<option value="{{value}}" {{selected}}>{{label}}</option>
					{{/values}}
				</select>
				<div class="popup-arrow">
					<span></span>
				</div>
			</div>
			<div class="usage-button-anim-container">
				<button class="usage-button" onclick="loadUsageModal('{{id}}');" {{usageButtonActive}}>i</button>
			</div>
		</div>
	</div>
</div>`;

let templatePopupBrowse = `<div class="row">
	<div class="col-s-4">
		<span class="input-label" data-label-swap-against="{{id}}">{{label}}</span>
	</div>
	<div class="col-s-8">
		<div class="usage-input-container">
			<div class="popup-browse">
				<div class="browse-select">
					<select data-id="{{id}}" data-type="popup-browse" data-index="{{index}}" data-tooltip="{{tooltip}}">
						{{#values}}
							<option value="{{value}}" {{selected}}>{{label}}</option>
						{{/values}}
					</select>
					<div class="browse-select-arrow">
						<span></span>
					</div>
				</div>
				<div class="browse-button">
					<button data-settings="{{settings}}">
						<svg viewBox="0 0 20 20"><path d="M19.5,10H16V6a1.15,1.15,0,0,0-1-1H8V4A1.11,1.11,0,0,0,7,3H1A1.11,1.11,0,0,0,0,4V16.78c0,.12.22.22.5.22h15c.28,0,.5-.1.5-.22l4-6.56C20,10.1,19.78,10,19.5,10ZM1,4H7V6h8v4H4.5c-.28,0-.5.1-.5.22L1,15ZM15.12,16H1.65l3-5H18.35Z"/></svg>
					</button>
				</div>
			</div>
			<div class="usage-button-anim-container">
				<button class="usage-button" onclick="loadUsageModal('{{id}}');" {{usageButtonActive}}>i</button>
			</div>
		</div>
	</div>
</div>`;

let templateNumber = `<div class="row">
	<div class="col-s-4">
		<label for="{{id}}" class="input-label" data-label-swap-against="{{id}}">{{label}}</label>
	</div>
	<div class="col-s-8">
		<div class="usage-input-container">
			<div class="number">
				<input type="number" data-index="{{index}}" data-type="number" data-tooltip="{{tooltip}}" id="{{id}}" data-id="{{id}}" value="{{value}}" data-ranges="{{ranges}}">
				<div class="number-nav">
					<div class="number-button number-up">
						<span></span>
					</div>
					<div class="number-button number-down">
						<span></span>
					</div>
				</div>
			</div>
			<div class="usage-button-anim-container">
				<button class="usage-button" onclick="loadUsageModal('{{id}}');" {{usageButtonActive}}>i</button>
			</div>
		</div>
	</div>
</div>`;

let templateRegex = `<div class="row">
	<div class="col-s-4">
		<label for="{{id}}" class="input-label" data-label-swap-against="{{id}}">{{label}}</label>
	</div>
	<div class="col-s-8">
		<div class="usage-input-container">
			<div class="regex">
				<div class="tag">RegEx</div>
				<input type="text" id="{{id}}" data-type="regex" data-index="{{index}}" data-id="{{id}}" data-tooltip="{{tooltip}}" value="{{value}}">
			</div>
			<div class="usage-button-anim-container">
				<button class="usage-button" onclick="loadUsageModal('{{id}}');" {{usageButtonActive}}>i</button>
			</div>
		</div>
	</div>
</div>`;

let templateString = `<div class="row">
	<div class="col-s-4">
		<label for="{{id}}" class="input-label" data-label-swap-against="{{id}}">{{label}}</label>
	</div>
	<div class="col-s-8">
		<div class="usage-input-container">
			<div class="string">
				<input type="text" id="{{id}}" data-type="string" data-index="{{index}}" data-id="{{id}}" data-tooltip="{{tooltip}}" value="{{value}}">
			</div>
			<div class="usage-button-anim-container">
				<button class="usage-button" onclick="loadUsageModal('{{id}}');" {{usageButtonActive}}>i</button>
			</div>
		</div>
	</div>
</div>`;

let templateArray = `<div class="row">
	<div class="col-s-4">
		<label for="{{id}}" class="input-label" data-label-swap-against="{{id}}">{{label}}</label>
	</div>
	<div class="col-s-8">
		<div class="usage-input-container">
			<div class="array">
				<div class="tag">Array</div>
				<input type="text" id="{{id}}" data-type="array" data-index="{{index}}" data-id="{{id}}" data-tooltip="{{tooltip}}" value="{{value}}">
			</div>
			<div class="usage-button-anim-container">
				<button class="usage-button" onclick="loadUsageModal('{{id}}');" {{usageButtonActive}}>i</button>
			</div>
		</div>
	</div>
</div>`;

let templateObject = `<div class="row">
	<div class="col-s-4">
		<label for="{{id}}" class="input-label" data-label-swap-against="{{id}}">{{label}}</label>
	</div>
	<div class="col-s-8">
		<div class="usage-input-container">
			<div class="object">
				<div class="tag">Object</div>
				<textarea id="{{id}}" data-type="object" data-index="{{index}}" data-id="{{id}}" data-tooltip="{{tooltip}}">{{value}}</textarea>
			</div>
			<div class="usage-button-anim-container">
				<button class="usage-button" onclick="loadUsageModal('{{id}}');" {{usageButtonActive}}>i</button>
			</div>
		</div>
	</div>
</div>`;

let templatePath = `<div class="row">
	<div class="col-s-4">
		<label for="{{id}}" class="input-label" data-label-swap-against="{{id}}">{{label}}</label>
	</div>
	<div class="col-s-8">
		<div class="usage-input-container">
			<div class="path">
				<input type="text" id="{{id}}" data-id="{{id}}" data-type="path" data-index="{{index}}" data-settings="{{settings}}" data-tooltip="{{tooltip}}" value="{{value}}">
				<div class="path-button">
					<button>
						<svg viewBox="0 0 20 20"><path d="M19.5,10H16V6a1.15,1.15,0,0,0-1-1H8V4A1.11,1.11,0,0,0,7,3H1A1.11,1.11,0,0,0,0,4V16.78c0,.12.22.22.5.22h15c.28,0,.5-.1.5-.22l4-6.56C20,10.1,19.78,10,19.5,10ZM1,4H7V6h8v4H4.5c-.28,0-.5.1-.5.22L1,15ZM15.12,16H1.65l3-5H18.35Z"/></svg>
					</button>
				</div>
			</div>
			<div class="usage-button-anim-container">
				<button class="usage-button" onclick="loadUsageModal('{{id}}');" {{usageButtonActive}}>i</button>
			</div>
		</div>
	</div>
</div>`;
