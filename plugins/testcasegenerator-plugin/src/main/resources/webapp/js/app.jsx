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
	componentDidMount: function() {
		console.log(this.refs.iframe.contentWindow);
	    this.refs.iframe.addEventListener("load", (function(e) {
	    	console.log(e);
	    	e.target.contentWindow.onscroll = this.props.handleScroll;
	    	console.log(e.target.contentWindow);
	    }).bind(this));
	},

	render: function() {
		return <iframe src={"diffs/" + this.props.filename} className="diff-display" ref="iframe" />
	}
});

var Eventable = function(props) {
	var event = props.eventResult.eventable;
	return (
		<div>
			<div id={"eventable" + event.id} className="infoTable center">
				<table>
					<thead>
						<tr>
							<td colSpan="6">Event Info</td>
						</tr>
						<tr>
							<td>ID</td>
							<td>Event type</td>
							<td>XPath</td>
							<td>Form inputs</td>
							<td>Related frame</td>
							<td>Success</td>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td>{event.id}</td>
							<td>{event.eventType}</td>
							<td>{event.identification.value}</td>
							<td>{event.relatedFormInputs.toString() || "None"}</td>
							<td>{event.relatedFrame || "Root frame"}</td>
							<td className={props.eventResult.success ? "success" : "failure"}>{props.eventResult.success ? "true" : "false"}</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	)
}

var StateInfo = function(props) {
	var state = props.state;
	return (
		<div>
			<div id={"state" + state.id} className="infoTable center">
				<table>
					<thead>
						<tr>
							<td colSpan="4">State Info</td>
						</tr>
						<tr>
							<td>ID</td>
							<td>URL</td>
							<td>Success</td>
							<td>Identical</td>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td>{state.id}</td>
							<td>{state.url}</td>
							<td className={state.success ? "success" : "failure"}>{state.success ? "true" : "false"}</td>
							<td className={state.identical ? "success" : "failure"}>{state.identical ? "true" : "false"}</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	)
}

var Selector = React.createClass({
	handleChange: function(event) {
		this.props.updatePath(event.target.value);
	},

	render: function() {
		return (
			<select id="pathSelector" defaultValue="0" onChange={this.handleChange}>
				{this.props.methods.map(function(opt) {
					return <option 
							value={opt.id} 
							key={opt.id} 
							className={opt.success ? "success" : "failure"}>
							{opt.methodName}
						</option>
				})}
			</select>
		)
	}
});

var StateNavigator = React.createClass({
	handleClick: function(idx) {
		this.props.updateState(idx);
	},

	render: function() {
		var stateList = [];
		for(var i = 0; i < this.props.states; i++) {
			if(i == this.props.currState) {
				stateList.push(<span key={i} className="button disabled">{i}</span>);
			} else {
				stateList.push(<span key={i} className="button" onClick={this.handleClick.bind(this, i)}>{i}</span>);
			}
		}
		return (
			<div id="stateNavigator">
				{stateList}
			</div>
		)
	}
})

var StateDisplay = React.createClass({
	getInitialState: function() {
		return {
			visual: true
		}
	},

	render: function() {
		if(this.props.noChange) return <p>No differences to show</p>

		return 	(
			<div id="stateDisplay" className="center">
				<div>
					Diff display: 
					<input type="radio" name="diffSelect" value="visual" onChange={this.setHTML} defaultChecked={this.state.visual} /> Visual
					<input type="radio" name="diffSelect" value="raw" onChange={this.setHTML} defaultChecked={!this.state.visual} /> Raw HTML
				</div>
				<State filename={this.state.visual ? this.props.initState : this.props.initXMLState} handleScroll={this.handleScroll.bind(this, 1)}/>
				<State filename={this.state.visual ? this.props.newState : this.props.newXMLState} handleScroll={this.handleScroll.bind(this, 2)}/>
			</div>
		)
	},

	setHTML: function(e) {
		this.setState({
			visual: e.target.value === "visual"
		});
	},

	handleScroll: function handleScroll(source, e) {
		handleScroll.called = handleScroll.called || false;
		if(handleScroll.called) {
			handleScroll.called = false;
			return;
		}
		if(this.state.visual) return;
		var frames = document.getElementsByTagName("iframe");
		if(source == 1) {
			frames[1].contentWindow.scroll(frames[1].contentWindow.pageXOffset, frames[0].contentWindow.pageYOffset);
		} else {
			frames[0].contentWindow.scroll(frames[0].contentWindow.pageXOffset, frames[1].contentWindow.pageYOffset);
		}
		handleScroll.called = true;
	}
})

var App = React.createClass({
	getInitialState: function() {
		return {
			report: [],
			pathIdx: 0,
			stateIdx: 0
		}
	},
	
	componentDidMount: function() {
		loadJSON(function(result) {
			this.setState({
				report: JSON.parse(result)
			});
		}.bind(this));
	},

	render: function() {
		if(this.state.report.length == 0) {
			return (<div id="app"></div>);
		}
		var nextEventable, stateDisplay;
		var currPath = this.state.report[this.state.pathIdx];
		var stateIdx = this.state.stateIdx;
		var currState = currPath.crawlStates[stateIdx];
		if(stateIdx < currPath.crawlPath.length) {
			nextEventable = <Eventable eventResult={currPath.crawlPath[stateIdx]} />;
		}
		var goToPrevState, goToNextState;
		if(stateIdx > 0) {
			goToPrevState = <span id="prev-state" className="button" onClick={this.handleGoToPrevState}>Prev. state</span>
		} else {
			goToPrevState = <span id="prev-state" className="disabled button">Prev. state</span>
		}
		if(stateIdx < currPath.crawlStates.length - 1) {
			goToNextState = <span id="next-state" className="button" onClick={this.handleGoToNextState}>Next state</span>
		} else {
			goToNextState = <span id="next-state" className="disabled button">Next state</span>
		}
		var stateId = currState.id;
		return (
			<div id="app">
				<Selector 
					updatePath={this.updatePath} 
					methods={this.state.report.map(function(path, index){
						return {
							id: index,
							methodName: path.methodName,
							success: path.success
						}
					})} />
				<StateNavigator
					updateState={this.handleSetCrawlState}
					states={currPath.crawlStates.length}
					currState={this.state.stateIdx}
					/>
				<div id="navigation">
					{goToPrevState}
					{goToNextState}
				</div>
				<StateInfo state={currState} />
				{nextEventable || (currPath.crawlPath.length == 0 ? <p>No events fired in this crawl path</p>
																: <p>End of crawl path</p>)}
				<StateDisplay initState={stateId + "-orig.html"}
							 newState={stateId + "-new.html"}
							 initXMLState={stateId + "-raw-orig.html"}
							 newXMLState={stateId + "-raw-new.html"} 
							 noChange={currState.identical} />
			</div>
		);
	},

	handleGoToPrevState: function() {
		this.setState({
			stateIdx: this.state.stateIdx - 1
		});
	},

	handleGoToNextState: function() {
		this.setState({
			stateIdx: this.state.stateIdx + 1
		});
	},

	handleSetCrawlState: function(newStateIdx) {
		this.setState({
			stateIdx: newStateIdx
		})
	},

	updatePath: function(newPathIdx) {
		this.setState({
			pathIdx: newPathIdx,
			stateIdx: 0
		});
	}
});

ReactDOM.render(<App />, document.getElementById("report"));