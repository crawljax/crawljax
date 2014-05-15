/*
 * diffLoader.js
 * Functions to load and display the Dom diff view in the CrawlOverview state
 * pages.
 * author: Ivan Plantevin
 */

var diffLoader = {
	/* Management of state browsing history */
	updateStateHistoryWithDom: function(currentDom) {
		// Current is now previous.
		var previous = localStorage.getItem("currentState");
		var current = diffLoader.fileName(location.href);

		if (!previous) {
			// No previous state, set to current.
			localStorage.setItem("previousState", current);
			localStorage.setItem("previousDom", currentDom);
		} else if (previous != current) { // Don't diff itself (e.g. after reload)
			localStorage.setItem("previousState", previous);
			localStorage.setItem("previousDom", localStorage.getItem("currentDom"));
		}

		localStorage.setItem("currentState", current);
		localStorage.setItem("currentDom", currentDom);
		console.log("previous: " + localStorage.getItem("previousState"));
		console.log("current: " + localStorage.getItem("currentState"));
	},

	/* Only loads the diff if it hasn't been loaded yet */
	initDiff: function() {
		if (diffLoader.diffIsNotYetLoaded()) {
			console.log("Calculating and loading diff");
			diffLoader.loadDomDiff();
		}
	},

	diffIsNotYetLoaded: function() {
		return $("#diffOutput").html() == "";
	},

	/*
	 * Loads doms from localStorage and displays the diff.
	 */
	loadDomDiff: function() {
		$("#diffOutput").html("Loading diff...");
		// Default inline view (1) and context size of 4
		var viewType = 1,
			contextSize = 4,
			dom1 = localStorage.getItem("previousDom"),
			dom2 = localStorage.getItem("currentDom"),
			state1 = localStorage.getItem("previousState"),
			state2 = localStorage.getItem("currentState");
		diffLoader.showStringsDiff(dom1, dom2, viewType, contextSize, state1, state2);
	},

	/*
	 * Calls the jsdifflib functions to actually calculate and display the
	 * diff.
	 */
	showStringsDiff: function(text1, text2, viewType, contextSize, name1, name2) {
		var lines1 = difflib.stringAsLines(text1),
			lines2 = difflib.stringAsLines(text2),
			sequenceMatcher = new difflib.SequenceMatcher(lines1, lines2),
			opcodes = sequenceMatcher.get_opcodes(),
			diffOutputDiv = document.getElementById("diffOutput");

		contextSize = contextSize || null;
		name1 = name1 || "Base Text";
		name2 = name2 || "New Text";

		diffOutputDiv.innerHTML = "";
		diffOutputDiv.appendChild(diffview.buildView({
			baseTextLines: lines1,
			newTextLines: lines2,
			opcodes: opcodes,
			baseTextName: name1,
			newTextName: name2,
			contextSize: contextSize,
			viewType: viewType
		}));

		diffLoader.makeDiffTitleClickable(name1, name2);
	},

	/*
	 * jsdifflib escapes the state names, so to make them clickable we insert
	 * own html to link titles in the diff table to their respective pages.
	 */
	makeDiffTitleClickable: function(state1, state2) {
		var hyperlinkedTextTitle =
			'<a title="Open in new window" target="_blank" href="' + state1 +
			'">' + state1 + '</a> vs. ' +
			'<a title="Open in new window" target="_blank" href="' + state2 +
			'">' + state2 + '</a>';
		$(".texttitle").first().html(hyperlinkedTextTitle);
	},

	fileName: function(fullPath) {
		var fileNameIndex = fullPath.lastIndexOf("/") + 1;
		return fullPath.substring(fileNameIndex);
	},

	/*
	 * Adds listeners for the two file inputs in the Dom diff view.
	 */
	setupFileInputListeners: function() {
		var file1 = document.getElementById("diffFile1");
		FileReaderJS.setupInput(file1, {
			readAsDefault: "Text",
			on: {
				load: function(e, file) {
					localStorage.setItem("previousDom", e.target.result);
					localStorage.setItem("previousState", file.name);
					diffLoader.loadDomDiff();
				}
			}
		});

		var file2 = document.getElementById("diffFile2");
		FileReaderJS.setupInput(file2, {
			readAsDefault: "Text",
			on: {
				load: function(e, file) {
					localStorage.setItem("currentDom", e.target.result);
					localStorage.setItem("currentState", file.name);
					diffLoader.loadDomDiff();
			}
			}
		});
	}
} // End diffLoader