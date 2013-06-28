
App = Ember.Application.create({LOG_TRANSITIONS: true});

Ember.Route.reopen({
	getTargetRoute: function(){
		var infos = this.router.router.targetHandlerInfos;
		return infos[infos.length - 1].handler.routeName;
	}
})

App.Router.map(function() {
	this.resource("configurations", function() {
		this.resource("configuration", {path: "/:configuration_id"}, function() {
			this.route("rules");
   			this.route("assertions");
			this.route("plugins");
		});
		this.route("new");
	});
	
	this.resource("plugin_management", {path: "plugins"});
	
	this.resource("crawlrecords", function(){ 
		this.resource("crawlrecord", {path: "/:crawlrecord_id"}, function() {
			this.resource("log");
			this.resource("plugin_output", {path: "plugin/:plugin_no"});
		});
		this.resource("config_filter", {path: "filter/:config_id"});
	});
});

App.ApplicationRoute = Ember.Route.extend({
	setupController: function(controller, model){
		controller.set("executionQueue", App.CrawlRecords.findAll(undefined, true));
		if(!("WebSocket" in window)) {
			alert('Need a browser that supports Sockets');
		} else {
			controller.connectSocket();
		}
	}
});

App.IndexRoute = Ember.Route.extend({
	redirect: function() {
		this.transitionTo('configurations');
	}
});

App.ConfigurationsIndexRoute = Ember.Route.extend({
	model: function (params) {
		return App.Configurations.findAll();
	},
	setupController: function(controller, model) {
		controller.set('content', model);
		var sideNav = this.controllerFor("sidenav");
		sideNav.set("sidenav",
			[App.Link.create({text:"New Configuration", target:"#/configurations/new", icon:"icon-pencil"})]);
		this.controllerFor('breadcrumb').set("breadcrumb", [App.Link.create({text: "Configurations", target: "#/configurations"})]);
	},
	exit: function(router){
		var sideNav = this.controllerFor("sidenav");
		sideNav.set("sidenav", []);
	}
});

App.ConfigurationRoute = Ember.Route.extend({
	model: function(params) {
		return App.Configurations.find(params.configuration_id);
	},
	setupController: function(controller, model) {
		controller.set("content", model);
		var sideNav = this.controllerFor("sidenav");
		sideNav.set("rest", controller.rest);
		sideNav.set("content", controller.get("content"));
		sideNav.set("sidenav",
				[App.Link.create({text:"Run Configuration", target:"run", action:true, icon:"icon-play"}),
				App.Link.create({text:"Save Configuration", target:"save", action:true, icon:"icon-ok"}),
				App.Link.create({text:"Crawl History", target:"#/crawlrecords/filter/" + model.id, icon:"icon-book"}),
				App.Link.create({text:"Delete Configuration", target:"delete", action:true, icon:"icon-remove"})]);
		this.controllerFor('breadcrumb').set("breadcrumb", [App.Link.create({text: "Configurations", target: "#/configurations"}),
				App.Link.create({text: model.id, target: "#/configurations/" + model.id})]);
	},
	serialize: function(context) {
		return { configuration_id: context.id };
	},
	exit: function(router){
		var sideNav = this.controllerFor("sidenav");
		sideNav.set("sidenav", []);
	}
});

App.ConfigurationIndexRoute = Ember.Route.extend({
	renderTemplate: function(){ this.render({controller: 'configuration'}); }
});

App.ConfigurationRulesRoute = Ember.Route.extend({
	renderTemplate: function(){ this.render({controller: 'configuration'}); }
});

App.ConfigurationAssertionsRoute = Ember.Route.extend({
	renderTemplate: function(){ this.render({controller: 'configuration'}); }
});

App.ConfigurationPluginsRoute = Ember.Route.extend({
	renderTemplate: function(){ this.render({controller: 'configuration'}); }
});

App.ConfigurationsNewRoute = Ember.Route.extend({
	setupController: function(controller, model) {
		var controller = this.controllerFor('configuration');
		var model = App.Configurations.getNew();
		controller.set('content', model);
		
		var sideNav = this.controllerFor("sidenav");
		sideNav.set("rest", controller.rest);
		sideNav.set("content", model);
		sideNav.set("sidenav",
			[App.Link.create({text:"Save Configuration", target:"add", action:true, icon:"icon-ok"})]);
		this.controllerFor('breadcrumb').set("breadcrumb", [App.Link.create({text: "Configurations", target: "#/configurations"}), App.Link.create({text: "New"})]);
	},
	renderTemplate: function() {
		this.render({controller: 'configuration'});
	},
	exit: function(router){
		var sideNav = this.controllerFor("sidenav");
		sideNav.set("sidenav", []);
	}
});

App.PluginManagementRoute = Ember.Route.extend({
	model: function(params) {
		return App.Plugins.findAll();
	},
	setupController: function(controller, model) {
		controller.set('content', model);
		this.controllerFor('breadcrumb').set("breadcrumb", [App.Link.create({text: "Plugins", target: "#/plugins"})]);
		if(!(window.File && window.FileReader && window.FileList && window.Blob)){
			alert('The File APIs are not fully supported in this browser.');
		}
	},
	exit: function(router){
		var sideNav = this.controllerFor("sidenav");
		sideNav.set("sidenav", []);
	}
});

App.CrawlrecordsIndexRoute = Ember.Route.extend({
	model: function(params) {
		return App.CrawlRecords.findAll();
	},
	setupController: function(controller, model) {
		controller.set('content', model);
		this.controllerFor('breadcrumb').set("breadcrumb", [App.Link.create({text: "History", target: "#/crawlrecords"})]);
	}
});

App.CrawlrecordRoute = Ember.Route.extend({
	model: function(params) {
		return App.CrawlRecords.find(params.crawlrecord_id);
	},
	setupController: function(controller, model) {
		controller.set('content', model);
		this.controllerFor('breadcrumb').set("breadcrumb", [App.Link.create({text: "History", target: "#/crawlrecords"}), App.Link.create({text: model.id, target: "#/crawlrecords/" + model.id})]);
		var sideNav = this.controllerFor("sidenav");
		sideNav.set("sidenav", [App.Link.create({text:"Configuration", target:"#/configurations/" + model.configurationId, icon:"icon-wrench"})]);
	},
	serialize: function(context) {
		return { crawlrecord_id: context.get("id") };
	},
	exit: function(router){
		var sideNav = this.controllerFor("sidenav");
		sideNav.set("sidenav", []);
	}
});

App.CrawlrecordIndexRoute = Ember.Route.extend({
	redirect: function(arg) {
		if(this.getTargetRoute() === this.routeName)
			this.transitionTo('log');
	}
});

App.LogRoute = Ember.Route.extend({
	setupController: function(controller, model) {
		var controller = this.controllerFor('crawlrecord');
		var appController = this.controllerFor('application');
		if (controller.isLogging) appController.sendMsg('stoplog');
		setTimeout(function(){ 
			$('#logPanel').css({'height':(($(document).height())-162)+'px'});
			appController.sendMsg('startlog-' + controller.get('content.id'));
			controller.set("isLogging", true);
			}, 0);
	},
	deactivate: function() {
		this.controllerFor('application').sendMsg('stoplog');
	},
	renderTemplate: function() {
		this.render({controller: 'crawlrecord'});
	}
});

App.PluginOutputRoute = Ember.Route.extend({
	model: function(params) {
		return {key: params.plugin_no};
	},
	serialize: function(context) {
		return { plugin_no: context.key };
	}
});

App.ConfigFilterRoute = Ember.Route.extend({
	model: function(params) {
		return {config_id: params.config_id}
	},
	setupController: function(controller, model) {
		var controller = this.controllerFor("crawlrecords.index");
		var model = App.CrawlRecords.findAll(model.config_id);
		controller.set('content', model);
		
		var sideNav = this.controllerFor("sidenav");
		sideNav.set("sidenav", [App.Link.create({text:"All Crawl Records", target:"#/crawlrecords", icon:"icon-book"})]);
		this.controllerFor('breadcrumb').set("breadcrumb", [App.Link.create({text: "History", target: "#/crawlrecords"})]);
	},
	renderTemplate: function(){ this.render("crawlrecords/index", {controller: "crawlrecords.index"}); },
	exit: function(router){
		var sideNav = this.controllerFor("sidenav");
		sideNav.set("sidenav", []);
	}
});