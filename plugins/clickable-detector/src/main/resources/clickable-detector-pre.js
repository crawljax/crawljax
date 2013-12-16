var rel = Node.prototype.removeEventListener;
Node.prototype.removeEventListener = function() {
        rel.apply(this, arguments);
        unannotate(this);
}

var ael = Node.prototype.addEventListener;
Node.prototype.addEventListener = function() {
        ael.apply(this, arguments);
        annotate(this);
}

function annotate(e) {
    if (e.setAttribute != undefined) {
        e.setAttribute("data-cj-clickable", "true");
    }
}

function unannotate(e) {
    if (e.removeAttribute != undefined) {
        e.removeAttribute("data-cj-clickable");
    }
}

var observer = new MutationSummary({
        callback : handleChanges,
        queries : [ {
                all : true
        } ]
});

function handleChanges(summaries) {
        // alert("DOM mutated!");
        detectClickables();
}

function detectClickables() {
        var all = document.getElementsByTagName("*");
        for ( var i = 0; i < all.length; i++) {
                var node = all[i];
                if (all[i].onclick != null) {
                        annotate(node);
                } else {
                        if (node.nodeType == 1) {// element of type html-object/tag
                                if (node.tagName == "A" || node.tagName == "BUTTON") {
                                        annotate(node);
                                }
                                if (node.tagName == "INPUT"
                                                && node.getAttribute("type").toUpperCase() == "SUBMIT") {
                                        annotate(node);
                                }
                        }
                }
        }
}