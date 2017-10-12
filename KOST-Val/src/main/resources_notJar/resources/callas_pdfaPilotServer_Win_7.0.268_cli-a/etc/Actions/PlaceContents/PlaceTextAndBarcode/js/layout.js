function setupPage(e,d,minh_mm){
	var pr = document.getElementById('pr');
	pr.innerHTML='@page{ size:10000pt 10000pt; }';

	var c = document.getElementById('c');
	c.style.width = "10000pt";
	c.style.height= "10000pt";

	var clr = document.getElementById('clr');
	clr.innerHTML='*{'+d.c+'}';
	if(d.w<0)
		document.body.style.whiteSpace = 'nowrap';
	else 
		document.body.style.whiteSpace = 'normal';
	if(d.w>0)
		e.style.width = d.w + "pt";

	//1in = 2.54cm = 25.4mm = 72pt = 6pc
	var d_h = d.h;
	if(d_h<=0)
		d_h = minh_mm*25.4/72;
	if(d_h>0)
		e.style.height = d_h + "pt";

	var w = d.w <= 0 ? 0.75*e.offsetWidth : d.w;
	var h = d.h <= 0 ? 0.75*e.offsetHeight: d.h;

	c.style.width = w+"pt";
	c.style.height= h+"pt";

	pr.innerHTML='@page{ size:'+(w+500)+'pt '+(h+500)+'pt;-cchip-cropbox:0 500pt '+w+'pt '+h+'pt}';

	// console.log( pr.innerHTML);

}
function hideBarcode(){
	document.getElementById('b').style.display = 'none';
	var btmp = document.getElementById('btemp');
	if(btmp)
		btmp.style.display = 'none';
}
function hideText(){
	document.getElementById('t').style.display = 'none';
}
function setBarcode(d) {
	hideText();

	var b = document.getElementById('b');

	var c = document.getElementById('c');
	var btemp = document.getElementById('btemp');
	if(btemp)
		c.removeChild(btemp);

	document.getElementById('bt').setAttribute('value',d.s);
	document.getElementById('bd').setAttribute('value',d.v);

	btemp = b.cloneNode(true);
	btemp.id = "btemp";
	btemp.style.display = 'initial';
	btemp.style.fontFamily = d.f;
	btemp.style.fontSize = d.fs+'pt';

	var extra_params = bc_lookup[d.s];
	var minh = 0;
	// console.log(extra_params);

	if( typeof extra_params != "undefined" )
	{
		extra_params = extra_params.params;
		if( typeof extra_params.height_mm != "undefined")
		{
			minh = extra_params.height_mm;
		}

		for( var i = 0; i < extra_params.length; ++i)
		{
			var extraParam = extra_params[i];
			// console.log( "Adding barcode param " + JSON.stringify(extraParam));
			//<param id='bt' name='type' value='Code 128'/>
			var newParam = document.createElement("param");
			newParam.setAttribute('name',extraParam.n);
			newParam.setAttribute('value',extraParam.v);
			btemp.appendChild(newParam);
		}
	}

	document.getElementById('b').style.display = 'none';
	c.insertBefore(btemp,b);

	// console.log( r.innerHTML);

	setupPage(btemp,d,minh);
}
function setText(d) {
	hideBarcode();
	var t = document.getElementById('t');
	t.style.display = 'initial';
	t.innerHTML = d.v;
	t.style.fontFamily = d.f;
	t.style.fontSize = d.fs+'pt';
	setupPage(t,d,0);
}
function setData(d) {
	if(d.t=='t')
		setText(d);
	else if(d.t=='b')
		setBarcode(d);
}
function cchipPrintLoop(){
	for(i=0;i<data.length;++i){
		setData(data[i]);
		cchip.printPages(1);
	}
}
function onLoad(){
	setData(data[0]);
}
