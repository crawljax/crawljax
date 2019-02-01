"use strict";

var ReactDOM = require("react-dom");
var React = require("react");

function loadJSON(callback) {

	var xobj = new XMLHttpRequest();
	xobj.overrideMimeType("application/json");
	xobj.open('GET', './diffs/report.json', true); // Replace 'my_data' with the path to your file
	xobj.onreadystatechange = function () {
		if (xobj.readyState == 4 && xobj.status == "200") {
			// Required use of an anonymous callback as .open will NOT return a value but simply returns undefined in asynchronous mode
			callback(xobj.response);
		}
	};
	xobj.send(null);
}

var State = React.createClass({
	displayName: "State",

	componentDidMount: function componentDidMount() {
		console.log(this.refs.iframe.contentWindow);
		this.refs.iframe.addEventListener("load", (function (e) {
			console.log(e);
			e.target.contentWindow.onscroll = this.props.handleScroll;
			console.log(e.target.contentWindow);
		}).bind(this));
	},

	render: function render() {
		return React.createElement("iframe", { src: "diffs/" + this.props.filename, className: "diff-display", ref: "iframe" });
	}
});

var Eventable = function Eventable(props) {
	var event = props.eventResult.eventable;
	return React.createElement(
		"div",
		null,
		React.createElement(
			"div",
			{ id: "eventable" + event.id, className: "infoTable center" },
			React.createElement(
				"table",
				null,
				React.createElement(
					"thead",
					null,
					React.createElement(
						"tr",
						null,
						React.createElement(
							"td",
							{ colSpan: "6" },
							"Event Info"
						)
					),
					React.createElement(
						"tr",
						null,
						React.createElement(
							"td",
							null,
							"ID"
						),
						React.createElement(
							"td",
							null,
							"Event type"
						),
						React.createElement(
							"td",
							null,
							"XPath"
						),
						React.createElement(
							"td",
							null,
							"Form inputs"
						),
						React.createElement(
							"td",
							null,
							"Related frame"
						),
						React.createElement(
							"td",
							null,
							"Success"
						)
					)
				),
				React.createElement(
					"tbody",
					null,
					React.createElement(
						"tr",
						null,
						React.createElement(
							"td",
							null,
							event.id
						),
						React.createElement(
							"td",
							null,
							event.eventType
						),
						React.createElement(
							"td",
							null,
							event.identification.value
						),
						React.createElement(
							"td",
							null,
							event.relatedFormInputs.toString() || "None"
						),
						React.createElement(
							"td",
							null,
							event.relatedFrame || "Root frame"
						),
						React.createElement(
							"td",
							{ className: props.eventResult.success ? "success" : "failure" },
							props.eventResult.success ? "true" : "false"
						)
					)
				)
			)
		)
	);
};

var StateInfo = function StateInfo(props) {
	var state = props.state;
	return React.createElement(
		"div",
		null,
		React.createElement(
			"div",
			{ id: "state" + state.id, className: "infoTable center" },
			React.createElement(
				"table",
				null,
				React.createElement(
					"thead",
					null,
					React.createElement(
						"tr",
						null,
						React.createElement(
							"td",
							{ colSpan: "4" },
							"State Info"
						)
					),
					React.createElement(
						"tr",
						null,
						React.createElement(
							"td",
							null,
							"ID"
						),
						React.createElement(
							"td",
							null,
							"URL"
						),
						React.createElement(
							"td",
							null,
							"Success"
						),
						React.createElement(
							"td",
							null,
							"Identical"
						)
					)
				),
				React.createElement(
					"tbody",
					null,
					React.createElement(
						"tr",
						null,
						React.createElement(
							"td",
							null,
							state.id
						),
						React.createElement(
							"td",
							null,
							state.url
						),
						React.createElement(
							"td",
							{ className: state.success ? "success" : "failure" },
							state.success ? "true" : "false"
						),
						React.createElement(
							"td",
							{ className: state.identical ? "success" : "failure" },
							state.identical ? "true" : "false"
						)
					)
				)
			)
		)
	);
};

var Selector = React.createClass({
	displayName: "Selector",

	handleChange: function handleChange(event) {
		this.props.updatePath(event.target.value);
	},

	render: function render() {
		return React.createElement(
			"select",
			{ id: "pathSelector", defaultValue: "0", onChange: this.handleChange },
			this.props.methods.map(function (opt) {
				return React.createElement(
					"option",
					{
						value: opt.id,
						key: opt.id,
						className: opt.success ? "success" : "failure" },
					opt.methodName
				);
			})
		);
	}
});

var StateNavigator = React.createClass({
	displayName: "StateNavigator",

	handleClick: function handleClick(idx) {
		this.props.updateState(idx);
	},

	render: function render() {
		var stateList = [];
		for (var i = 0; i < this.props.states; i++) {
			if (i == this.props.currState) {
				stateList.push(React.createElement(
					"span",
					{ key: i, className: "button disabled" },
					i
				));
			} else {
				stateList.push(React.createElement(
					"span",
					{ key: i, className: "button", onClick: this.handleClick.bind(this, i) },
					i
				));
			}
		}
		return React.createElement(
			"div",
			{ id: "stateNavigator" },
			stateList
		);
	}
});

var StateDisplay = React.createClass({
	displayName: "StateDisplay",

	getInitialState: function getInitialState() {
		return {
			visual: true
		};
	},

	render: function render() {
		if (this.props.noChange) return React.createElement(
			"p",
			null,
			"No differences to show"
		);

		return React.createElement(
			"div",
			{ id: "stateDisplay", className: "center" },
			React.createElement(
				"div",
				null,
				"Diff display:",
				React.createElement("input", { type: "radio", name: "diffSelect", value: "visual", onChange: this.setHTML, defaultChecked: this.state.visual }),
				" Visual",
				React.createElement("input", { type: "radio", name: "diffSelect", value: "raw", onChange: this.setHTML, defaultChecked: !this.state.visual }),
				" Raw HTML"
			),
			React.createElement(State, { filename: this.state.visual ? this.props.initState : this.props.initXMLState, handleScroll: this.handleScroll.bind(this, 1) }),
			React.createElement(State, { filename: this.state.visual ? this.props.newState : this.props.newXMLState, handleScroll: this.handleScroll.bind(this, 2) })
		);
	},

	setHTML: function setHTML(e) {
		this.setState({
			visual: e.target.value === "visual"
		});
	},

	handleScroll: function handleScroll(source, e) {
		handleScroll.called = handleScroll.called || false;
		if (handleScroll.called) {
			handleScroll.called = false;
			return;
		}
		if (this.state.visual) return;
		var frames = document.getElementsByTagName("iframe");
		if (source == 1) {
			frames[1].contentWindow.scroll(frames[1].contentWindow.pageXOffset, frames[0].contentWindow.pageYOffset);
		} else {
			frames[0].contentWindow.scroll(frames[0].contentWindow.pageXOffset, frames[1].contentWindow.pageYOffset);
		}
		handleScroll.called = true;
	}
});

var App = React.createClass({
	displayName: "App",

	getInitialState: function getInitialState() {
		return {
			report: [],
			pathIdx: 0,
			stateIdx: 0
		};
	},

	componentDidMount: function componentDidMount() {
		loadJSON((function (result) {
			this.setState({
				report: JSON.parse(result)
			});
		}).bind(this));
	},

	render: function render() {
		if (this.state.report.length == 0) {
			return React.createElement("div", { id: "app" });
		}
		var nextEventable, stateDisplay;
		var currPath = this.state.report[this.state.pathIdx];
		var stateIdx = this.state.stateIdx;
		var currState = currPath.crawlStates[stateIdx];
		if (stateIdx < currPath.crawlPath.length) {
			nextEventable = React.createElement(Eventable, { eventResult: currPath.crawlPath[stateIdx] });
		}
		var goToPrevState, goToNextState;
		if (stateIdx > 0) {
			goToPrevState = React.createElement(
				"span",
				{ id: "prev-state", className: "button", onClick: this.handleGoToPrevState },
				"Prev. state"
			);
		} else {
			goToPrevState = React.createElement(
				"span",
				{ id: "prev-state", className: "disabled button" },
				"Prev. state"
			);
		}
		if (stateIdx < currPath.crawlStates.length - 1) {
			goToNextState = React.createElement(
				"span",
				{ id: "next-state", className: "button", onClick: this.handleGoToNextState },
				"Next state"
			);
		} else {
			goToNextState = React.createElement(
				"span",
				{ id: "next-state", className: "disabled button" },
				"Next state"
			);
		}
		var stateId = currState.id;
		return React.createElement(
			"div",
			{ id: "app" },
			React.createElement(Selector, {
				updatePath: this.updatePath,
				methods: this.state.report.map(function (path, index) {
					return {
						id: index,
						methodName: path.methodName,
						success: path.success
					};
				}) }),
			React.createElement(StateNavigator, {
				updateState: this.handleSetCrawlState,
				states: currPath.crawlStates.length,
				currState: this.state.stateIdx
			}),
			React.createElement(
				"div",
				{ id: "navigation" },
				goToPrevState,
				goToNextState
			),
			React.createElement(StateInfo, { state: currState }),
			nextEventable || (currPath.crawlPath.length == 0 ? React.createElement(
				"p",
				null,
				"No events fired in this crawl path"
			) : React.createElement(
				"p",
				null,
				"End of crawl path"
			)),
			React.createElement(StateDisplay, { initState: stateId + "-orig.html",
				newState: stateId + "-new.html",
				initXMLState: stateId + "-raw-orig.html",
				newXMLState: stateId + "-raw-new.html",
				noChange: currState.identical })
		);
	},

	handleGoToPrevState: function handleGoToPrevState() {
		this.setState({
			stateIdx: this.state.stateIdx - 1
		});
	},

	handleGoToNextState: function handleGoToNextState() {
		this.setState({
			stateIdx: this.state.stateIdx + 1
		});
	},

	handleSetCrawlState: function handleSetCrawlState(newStateIdx) {
		this.setState({
			stateIdx: newStateIdx
		});
	},

	updatePath: function updatePath(newPathIdx) {
		this.setState({
			pathIdx: newPathIdx,
			stateIdx: 0
		});
	}
});

ReactDOM.render(React.createElement(App, null), document.getElementById("report"));