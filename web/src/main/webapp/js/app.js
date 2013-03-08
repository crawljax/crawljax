	App = Ember.Application.create();
    
    App.Router.map(function() {
   		this.resource("config_list", { path: "/" });
   		this.resource("config", {path: "/:id"}, function(){
   			this.route("conditions");
   			this.route("assertions");
   			this.route("plugins");
   			this.resource("config_history", {path: "history"});
   		});
   		this.route("new");
   		this.resource("about");
   		this.resource("history_list", {path: "/history"}, function() {
   			this.resource("history", {path: "/:id"})
   		});
   	});

    
    App.ConfigListRoute = Ember.Route.extend({
      setupController: function(controller, model) {
        controller.set('content', App.Config.findAll());
        controller.set('sidenav',
        	[App.Link.create({text:"New Configuration", target:"#/new", icon:"icon-pencil"}), 
        	 App.Link.create({text:"Crawl History", target:"#/history", icon:"icon-book"})]);
        controller.set('breadcrumb', [App.Link.create({text: "Home"})]);
      }
    });
    
    App.NewRoute = Ember.Route.extend({
    	renderTemplate: function(){ this.render({controller: 'config'}); },
    	setupController: function(controller, model) {
    		this.controllerFor('config').set('content', App.Config.newConfig());
    		this.controllerFor('config').set('sidenav',
            	[App.Link.create({text:"Save Configuration", target:"add", action:true, icon:"icon-folder-open"})]);
    		this.controllerFor('config').set('breadcrumb', [App.Link.create({text: "Home", target: "#"}), App.Link.create({text: "New"})]);
        }
    });
    
    App.ConfigRoute = Ember.Route.extend({
    	setupController: function(controller, model) {
            controller.set('content', model);
            controller.set('sidenav',
            		[App.Link.create({text:"Run Configuration", target:"run", action:true, icon:"icon-play"}),
            		 App.Link.create({text:"Save Configuration", target:"save", action:true, icon:"icon-folder-open"}),
            		 App.Link.create({text:"Crawl History", target:"#/"+model.id+"/history", icon:"icon-book"}),
            		 App.Link.create({text:"Delete Configuration", target:"delete", action:true, icon:"icon-remove"})]);
            controller.set('breadcrumb', [App.Link.create({text: "Home", target: "#"}), 
                                          App.Link.create({text: 'Configuration', target: "#/" + model.id})]);
        },
        serialize: function(object) { return { id: object.id }; },
        deserialize: function(params) { return App.Config.find(params.id); }
    });
    
    App.ConfigIndexRoute = Ember.Route.extend({
    	renderTemplate: function(){ this.render({controller: 'config'}); }
    });
    
    App.ConfigConditionsRoute = Ember.Route.extend({
    	renderTemplate: function(){ this.render({controller: 'config'}); }
    });
    
    App.ConfigAssertionsRoute = Ember.Route.extend({
    	renderTemplate: function(){ this.render({controller: 'config'}); }
    });
    
    App.ConfigPluginsRoute = Ember.Route.extend({
    	renderTemplate: function(){ this.render({controller: 'config'}); }
    });
    
    App.ConfigHistoryRoute = Ember.Route.extend({
    	setupController: function(controller, model) {
    		var configController = this.controllerFor('config');
            this.controllerFor('history_list').set('content', App.CrawlHistory.findAll(configController.get('content').id));
            configController.set('breadcrumb', [App.Link.create({text: "Home", target: "#"}), 
                                          App.Link.create({text: 'Configuration', target: "#/" + configController.get('content').id}), 
                                          App.Link.create({text: "History"})]);
        },
    	renderTemplate: function(){ this.render("history_list/index", {controller: 'history_list'}); }
    });
    
    App.HistoryListRoute = Ember.Route.extend({
        setupController: function(controller, model) {
            controller.set('sidenav',
            	[App.Link.create({text:"All Configurations", target:"#", icon:"icon-list"}),
            	 App.Link.create({text:"New Configuration", target:"#/new", icon:"icon-pencil"})]);
            controller.set('breadcrumb', [App.Link.create({text: "Home", target: "#"}), App.Link.create({text: "History"})]);
        }
   }); 
    
    App.HistoryListIndexRoute = Ember.Route.extend({
    	renderTemplate: function(){ this.render({controller: 'history_list'}); },
    	setupController: function(controller, model) { 
    		this.controllerFor('history_list').set('content', App.CrawlHistory.findAll()); }
    });