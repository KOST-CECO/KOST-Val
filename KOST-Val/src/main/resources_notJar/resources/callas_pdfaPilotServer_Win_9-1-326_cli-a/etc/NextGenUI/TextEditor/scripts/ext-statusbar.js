define("ace/ext/statusbar",["require","exports","module","ace/lib/dom","ace/lib/lang"], function(require, exports, module) {
"use strict";
var dom = require("../lib/dom");
var lang = require("../lib/lang");

var StatusBar = function(editor, parentNode, translations = {}) {
    this.element = dom.createElement("div");
    this.element.className = "ace_status-indicator";
    parentNode.prepend(this.element);
    const strings = {
        "line": translations["line"] || "Line",
        "lines": translations["lines"] || "lines",
        "column": translations["column"] || "Columne",
        "selection_regions": translations["selection_regions"] || "selection regions",
        "characters_selected": translations["characters_selected"] || "characters selected"
    };

    var statusUpdate = lang.delayedCall(function(){
        this.updateStatus(editor, strings);
    }.bind(this)).schedule.bind(null, 100);
    
    editor.on("changeStatus", statusUpdate);
    editor.on("changeSelection", statusUpdate);
    editor.on("keyboardActivity", statusUpdate);
};

(function(){
    this.updateStatus = function(editor, strings) {
        var status = [];
        function add(str, separator) {
            str && status.push(str, separator || ", ");
        }

        add(editor.keyBinding.getStatusText(editor));
        if (editor.commands.recording)
            add("REC");
        
        var sel = editor.selection;
        var c = sel.lead;
        
        if (sel.rangeCount) {
            add(sel.rangeCount + " selection regions", ", ");
        } else {
            if (!sel.isEmpty()) {
                var r = editor.getSelectionRange();
                var s = c.document.$lines.slice(r.start.row, r.end.row+1);
                s[0] = s[0].slice(r.start.column);
                s[s.length-1] = s[s.length-1].substring(0, s.length != 1 ? r.end.column : r.end.column - r.start.column);
                add(s.length + " lines, " + s.join("").length + " characters selected", ", ");
            } else {
                add(strings["line"] + " " + (c.row + 1) + ", Columne " + (c.column + 1), ", ");
            }
        }
        status.pop();
        this.element.textContent = status.join("");
    };
}).call(StatusBar.prototype);

exports.StatusBar = StatusBar;

});                (function() {
                    window.require(["ace/ext/statusbar"], function(m) {
                        if (typeof module == "object" && typeof exports == "object" && module) {
                            module.exports = m;
                        }
                    });
                })();
            