App.ApplicationController = Ember.Controller.extend({
	needs: ['crawlrecord'],
	executionQueue: [],
	updateQueue: function(id, status){
		var element = this.executionQueue.find(function(item){
			return (item.id == id);
		});
		if (element != null)
			element.set('crawlStatus', status);
	},
	removeQueue: function(id) {
		var element = this.executionQueue.find(function(item){
			return (item.id == id);
		});
		if (element != null)
			this.executionQueue.removeObject(element);
	},
	socket: null,
	connectSocket: function(){
		//create web socket url from current path
		var loc = window.location, host;
		if (loc.protocol === "https:") {
			host = "wss:";
		} else {
			host = "ws:";
		}
		host += "//" + loc.host;
		host += loc.pathname + "socket";

		try {
			var controller = this;
			this.socket = new WebSocket(host);
			
			this.socket.onmessage = function(msg){
				if (msg.data.indexOf('log-') == 0)
					$('#logPanel').append('<p>'+msg.data.slice(4)+'</p>');
				if (msg.data.indexOf('queue-') == 0) {
					var record = App.CrawlRecords.create({});
					record.setProperties(JSON.parse(msg.data.slice(6)));
					record.plugins = [];
					controller.get('executionQueue').pushObject(record);
				}
				if (msg.data.indexOf('init-') == 0)
					controller.updateQueue(msg.data.slice(5), "initializing");
				if (msg.data.indexOf('run-') == 0)
					controller.updateQueue(msg.data.slice(4), "running");
				if (msg.data.indexOf('fail-') == 0) {
					controller.updateQueue(msg.data.slice(5), "failure");
					setTimeout(function(){controller.removeQueue(msg.data.slice(5));}, 5000);
				}
				if (msg.data.indexOf('success-') == 0) {
					controller.updateQueue(msg.data.slice(8), "success");
					if(controller.get('controllers.crawlrecord.content.id') == msg.data.slice(8)) {
						controller.get('controllers.crawlrecord').set('content', App.CrawlRecords.find(msg.data.slice(8)));
					}
					setTimeout(function(){controller.removeQueue(msg.data.slice(8));}, 5000);
				}
				if (msg.data.indexOf('message-') == 0) {
					var positivity = 0;
					if (msg.data.indexOf('success-') == 8) {
						positivity = 1;
						controller.displayMessage(msg.data.slice(16), positivity);
					} else if (msg.data.indexOf('error-') == 8) {
						positivity = -1;
						controller.displayMessage(msg.data.slice(14), positivity);
					} else {
						controller.displayMessage(msg.data.slice(8), positivity);
					}
				}
			};
			this.socket.onclose = function(){
				controller.connectSocket();
			};
		}catch(exception){
			 alert('Error'+exception);
		}
	},
	sendMsg: function(text){
		try{
			var self = this;
			if (self.socket.readyState != 1) {
				setTimeout(function(){ self.socket.send(text);  }, 500);
			}
			else self.socket.send(text);
		} catch(exception){
			alert("Socket Timed out. Refresh your browser. ");
		}
   },
   displayMessage: function(text, positivity) {
		var clazz = "info";
		if(positivity > 0) clazz = "success";
		if(positivity < 0) clazz = "error";
		$('#notification').removeClass().addClass("alert").addClass("alert-" + clazz).text(text);
		clearTimeout(this.messageTimeout);
		this.messageTimeout = setTimeout(function() {
			$('#notification').removeClass().addClass("alert").addClass("alert-mute");
		}, 3000);
   }
});

App.BreadcrumbController = Ember.Controller.extend({
	needs: ['application']
});

App.SidenavController = Ember.Controller.extend({
	needs: ['application']
	//sidenav: null
});

App.ExecutionQueueController = Ember.Controller.extend({
	needs: ['application']
});

App.ConfigurationsIndexController = Ember.ArrayController.extend({
	needs: ['application'],
	itemController: 'ConfigurationsIndexItem'
});

App.ConfigurationsIndexItemController = Ember.Controller.extend({
	lastCrawlFormatted: function() {
		var lastCrawl = this.get('content.lastCrawl');
		if (lastCrawl == null ) return 'never';
		else return new Date(lastCrawl);
	}.property('content.lastCrawl'),
	lastDurationFormatted: function() {
		var lastDuration = this.get('content.lastDuration')/1000;
		if (lastDuration == 0 ) {
			if (this.get('content.lastCrawl') == null) return 'n/a';
			else return 'running';
		}
		return Math.floor(lastDuration/60) + ' min ' + Math.floor(lastDuration%60) + ' sec';
	}.property('content.lastCrawl', 'content.lastDuration')
});

App.ConfigurationController = Ember.Controller.extend({
	needs: ['application'],
	rest: function(link){
		switch(link.target)
		{
			case "add":
				if (validateForm('config_form')) {
					var router = this.get('target');
					App.Configurations.add(this.get("content"), function(data){ router.transitionToRoute('configuration', data); });
				}
				break;
			case "run":
				App.CrawlRecords.add(this.get("content.id"));
				break;
			case "save":
				if (validateForm("config_form")) {
					this.set('content', App.Configurations.update(this.get('content')));
					this.get("controllers.application").displayMessage("Configuration Saved", 1);
				}
				break;
			case "delete":
				var r = confirm("Are you sure you want to permanently delete this configuration?");
				if (r == true) {
					var router = this.get('target');
					var _this = this;
					App.Configurations.remove(this.get("content"), function(data){ 
						router.transitionToRoute('configurations');
						_this.get("controllers.application").displayMessage("Configuration Deleted", 1);
					});
				}
				break;
		}
	},
	moveTo: function(route) {
		if (validateForm('config_form')) {
			var router = this.get('target');
			router.transitionTo(route);
		}
	}
});

App.ClickRulesController = Ember.ArrayController.extend({
	add: function() {
		this.content.pushObject({rule: 'click', elementTag: 'a', conditions: []});
	},
	remove: function(item) { 
		this.content.removeObject(item); },
	itemController: 'clickRuleItem'
});

App.ClickRuleItemController = Ember.Controller.extend({
	addCondition: function() {
		this.content.conditions.pushObject({condition: 'wAttribute', expression: ''});
	},
	removeCondition: function(item) { 
		this.content.conditions.removeObject(item);
	}
});

App.ConditionsController = Ember.ArrayController.extend({
	add: function() {
		this.content.pushObject({condition: 'url', expression: ''});
	},
	remove: function(item) {
		this.content.removeObject(item);
	},
	itemController: "conditionItem"
});

App.ConditionItemController = Ember.Controller.extend({
	
});

App.FormInputsController = Ember.ArrayController.extend({
	add: function() {
		this.content.pushObject({name: '', value: ''});
	},
	remove: function(item) {
		this.content.removeObject(item);
	},
	itemController: "formInputItem"
});

App.FormInputItemController = Ember.Controller.extend({
	
});

App.ComparatorItemController = Ember.Controller.extend({
	needsExpression: function() { 
		var type = this.get('content.type');
		return ( type == 'attribute' || type == 'xpath' || type == 'distance' || type == 'regex' );
	}.property('content.type')
});

App.PluginsController = Ember.ArrayController.extend({
	add: function() {
		this.content.pushObject({id: null});
	},
	remove: function(item) {
		this.content.removeObject(item);
	},
	itemController: 'pluginItem'
});

App.PluginItemController = Ember.Controller.extend({
	contentChanged: function() {
		this.set("selectItems", App.Plugins.selectItems());
	}.observes("content"),
	selectionChanged: function(id) {
		var content = this.get('content');
		this.set('content.id', id);
		if(id) {
			var plugin = App.Plugins.find(id);
			this.set('plugin', plugin);
			var params = [];
			var values = content.parameters;
			if(plugin.parameters) {
				for(var i = 0, l = plugin.parameters.length; i < l; i++) {
					var parameter = plugin.parameters[i];
					for(var j = 0, l_2 = values.length; j < l_2; j++) {
						if(values[j].id === parameter.id) {
							parameter.value = values[j].value;
						}
					}
					params.push(parameter);
				}
			}
			this.set('content.parameters', params);
		} else {
			this.set('content.parameters', []);
		}
	}
});

App.PluginManagementController = Ember.ArrayController.extend({
	needs: ['application'],
	refresh: function() {
		this.get("controllers.application").displayMessage("Refreshing List...", 0);
		var _this = this;
		App.Plugins.refresh(function(plugins){
			_this.set('content', plugins);
			_this.get("controllers.application").displayMessage("List Refreshed", 1);
		});
	},
	addFile: function() {
		var file = this.get("pluginFile");
		if(!file){
			alert("Please select a file");
			return;
		}
		if(file.name.indexOf(".jar") === -1 || file.name.indexOf(".jar") !== file.name.length - 4) {
			alert("Please select a .jar file");
			return;
		}
		this.get("controllers.application").displayMessage("Uploading Plugin...", 0);
		var _this = this;
		var reader = new FileReader();
		reader.onload = function(e) {
			App.Plugins.add(file.name, e.target.result, undefined, function() {
				_this.set('content', App.Plugins.findAll());
			});
		}
		reader.readAsDataURL(file);
	},
	addURL: function() {
		var url = this.get("url");
		if(!url) {
			alert("Please enter a url");
			return;
		}
		var name =  url.split("/").pop();
		this.get("controllers.application").displayMessage("Downloading Plugin...", 0);
		var _this = this;
		App.Plugins.add(name, undefined, url, function() {
			_this.set('content', App.Plugins.findAll());
		});
	},
	itemController: 'pluginManagementItem'
});

App.PluginManagementItemController = Ember.Controller.extend({
	needs: ['application', 'pluginManagement'],
	remove: function() {
		var r = confirm("Are you sure you want to remove " + this.get('content.name') + " (id: " + this.get('content.id') + ")");
		if (r == true) {
			var _this = this;
			App.Plugins.remove(this.get('content'), function(){
				_this.get('controllers.pluginManagement').set('content', App.Plugins.findAll());
				_this.get("controllers.application").displayMessage("Plugin Deleted", 1);
			});
		}
	}
});

App.CrawlrecordsIndexController = Ember.ArrayController.extend({
	needs: ['application'],
	itemController: 'CrawlrecordsIndexItem'
});

App.CrawlrecordsIndexItemController = Ember.Controller.extend({
	startTimeFormatted: function(){
		var startTime = this.get('content.startTime');
		if (startTime == null) return 'queued';
		else return new Date(startTime);
	}.property('content.startTime'),
	configURL: function(){
		return '#/configurations/' + this.get('content.configurationId');
	}.property('content.configurationId')
});

App.CrawlrecordController = Ember.Controller.extend({
	needs: ['application'],
	isFinished: function(){
		return (this.get('content.crawlStatus') == 'success');
	}.property('content.crawlStatus')
});

App.PluginOutputController = Ember.Controller.extend({
	needs: ['crawlrecord'],
	url: function() {
		return "/output/crawl-records/" + this.get('controllers.crawlrecord.content.id') + "/plugins/" + this.get('content.key') + "/";
	}.property('content.key', 'controllers.crawlrecord.content.id')
});