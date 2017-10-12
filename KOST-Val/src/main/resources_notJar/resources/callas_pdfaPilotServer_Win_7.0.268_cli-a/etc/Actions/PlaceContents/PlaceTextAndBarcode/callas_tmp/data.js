/*
	The data var holds an array of objects to be placed on page
	For every entry in tha array on page  with a crop box according to 'w' and 'h' 
	is created in the overlay PDF

	t: type, 'b' for barcode and 't' for text
	w: width in pt, if <=0 the intrinsic width of the object is used
	h: height in pt, if <=0 the intrinsic height of the object is used
	f: font face
	fs: font size
	v: value, barcode data for t='b' and text for t='t'
	c: color for elements
	s: barcode symbology
*/
var data=
[
	{'t': 'b', 'w':0, 'h':0, 'f': 'Arial, Helvetica, sans-serif', 'fs':12, 'v':'123456789012', 'c':'color:-cchip-cmyk(0,0,0,1);background-color:-cchip-cmyk(0,0,0,0);', 's':'EAN 13'},
	{'t': 'b', 'w':119.055, 'h':119.055, 'f': 'Arial, Helvetica, sans-serif', 'fs':12, 'v':'www.callassoftware.com', 'c':'color:-cchip-cmyk(0,0,0,1);background-color:-cchip-cmyk(0,0,0,0);', 's':'QR-Code'},

	{'t': 't', 'w':0, 'h':0, 'f': 'Arial, Helvetica, sans-serif', 'fs':1, 'v':'www.<b>callassoftware</b>.com', 'c':'color:-cchip-cmyk(0,0,0,1);background-color:-cchip-cmyk(0,0,0,0);', 's':'QR-Code'},
	{'t': 't', 'w':0, 'h':0, 'f': 'Arial, Helvetica, sans-serif', 'fs':2, 'v':'www.<b>callassoftware</b>.com', 'c':'color:-cchip-cmyk(0,0,0,1);background-color:-cchip-cmyk(0,0,0,0);', 's':'QR-Code'},
	{'t': 't', 'w':0, 'h':0, 'f': 'Arial, Helvetica, sans-serif', 'fs':4, 'v':'www.<b>callassoftware</b>.com', 'c':'color:-cchip-cmyk(0,0,0,1);background-color:-cchip-cmyk(0,0,0,0);', 's':'QR-Code'},
	{'t': 't', 'w':0, 'h':0, 'f': 'Arial, Helvetica, sans-serif', 'fs':5, 'v':'www.<b>callassoftware</b>.com', 'c':'color:-cchip-cmyk(0,0,0,1);background-color:-cchip-cmyk(0,0,0,0);', 's':'QR-Code'},
	{'t': 't', 'w':0, 'h':0, 'f': 'Arial, Helvetica, sans-serif', 'fs':12, 'v':'äöü www.callassoftware.com', 'c':'color:-cchip-cmyk(0,0,0,1);background-color:-cchip-cmyk(0,0,0,0);', 's':'QR-Code'},
	{'t': 't', 'w':0, 'h':0, 'f': 'Arial, Helvetica, sans-serif', 'fs':24, 'v':'äöü<br>www.callassoftware.com', 'c':'color:-cchip-cmyk(0,0,0,1);background-color:-cchip-cmyk(0,0,0,0);', 's':'QR-Code'},
	{'t': 't', 'w':0, 'h':0, 'f': 'Arial, Helvetica, sans-serif', 'fs':48, 'v':'www.<b>callassoftware</b>.com', 'c':'color:-cchip-cmyk(0,0,0,1);background-color:-cchip-cmyk(0,0,0,0);', 's':'QR-Code'},
];
