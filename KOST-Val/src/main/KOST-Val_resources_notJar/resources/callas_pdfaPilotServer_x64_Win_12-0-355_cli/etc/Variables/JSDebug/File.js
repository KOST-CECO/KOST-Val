let fs = require("fs");

function File(path) {
	this.path = path;
	this.fullName = fullName(path);
	this.exists = fsExistsSync(path);
	if( this.exists) {
		this.lastModified = fsLastModified(path);
	}
	this.name = filename(path);
}

File.prototype.read = function() {
	return fs.readFileSync(this.path, "utf8");
}

File.prototype.exists = this.exists;

File.prototype.fullName = this.fullName;

File.prototype.lastModified = this.lastModified;

File.prototype.name = this.name;

function fullName(path) {
	return path;
}

function fsExistsSync(path) {
	try {
		if(fs.existsSync(path)) {
			return true;
		}else {
			return false;
		}
	} catch (e) {
		return false;
	}
}

function fsLastModified(path) {
	let stats = fs.statSync(path);
	let mtime = new Date(stats.mtime);
	return mtime.toString();
}

function filename(path) {
	return path.split('/').pop();
}

module.exports = File;