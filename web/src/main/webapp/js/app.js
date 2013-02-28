    App = Ember.Application.create();
    
    //Views
    App.SideNavView = Ember.CollectionView.extend({
  	  	tagName: "ul",
  	  	classNames: "nav nav-list",
  	  	itemViewClass: Ember.View.extend({ 
  	  		template: Ember.Handlebars.compile([
  	  			'{{#if view.content.action}}',
  	  			'	<a href="#" {{action "rest" view.content}}>{{view.content.text}}</a>',
  	  			'{{else}}',
  	  			'	<a {{bindAttr href="view.content.target"}}>{{view.content.text}}</a>',
  	  			'{{/if}}'].join("\n"))
  	  	}),
  	  	contentBinding: "controller.sidenav"
    });
    
    App.BreadcrumbView = Ember.CollectionView.extend({
    	tagName: "ul",
     	classNames: "breadcrumb well",
     	createChildView: function(view, attrs) {  
     		if (attrs.contentIndex === (this.content.length - 1)) {
     			view = this.lastItemViewClass || view;
     		} return this._super(view, attrs);
     	},
    	itemViewClass: Ember.View.extend({
    		template: Ember.Handlebars.compile('<a {{bindAttr href="view.content.target"}}>{{view.content.text}}</a><span class="divider">/</span>')
    	}),
    	lastItemViewClass: Ember.View.extend({
    		classNames: "active",
    		template: Ember.Handlebars.compile("{{view.content.text}}")
    	}),
    	contentBinding: "controller.breadcrumb"
  	});
    
    //Models       
    App.Link = Ember.Object.extend({ text: null, target: null, action: false });
    
    App.browsers = [
         Ember.Object.create({name: "Mozilla Firefox", value:"firefox"}),
         Ember.Object.create({name: "Google Chrome", value:"chrome"}),
         Ember.Object.create({name: "Microsoft Internet Explorer", value:"ie"})
	];
    
    App.clickRules = [
         Ember.Object.create({name: "Click Default Elements", value:"Default"}),
         Ember.Object.create({name: "Click More Elements", value:"More"}),
         Ember.Object.create({name: "Custom", value:"Custom"})
    ];
    
    //Controllers
    App.NewController = Ember.Controller.extend({
    	rest: function(link){
	    	switch(link.target)
	    	{
	    	case "add":
	    		if (validateForm('config_form')) {
	    			var router = this.get('target');
	    			App.Config.add(this.content, function(data){ router.transitionTo('config', data); });
	    		}
	    		break;
	    	}
	    }
    });
    
    App.ConfigListController = Ember.ArrayController.extend({ itemController: 'configListItem' });
    App.ConfigListItemController = Ember.ObjectController.extend({
    	formatLastCrawl : function() {
    		lastCrawl = this.get('lastCrawl');
    		if (lastCrawl == null ) return 'never';
    		else return new Date(lastCrawl); }.property('lastCrawl')
    });
    App.ConfigController = Ember.Controller.extend({
    	needs: ['application'],
    	rest: function(link){
	    	switch(link.target)
	    	{
	    	case "run":
	    		App.CrawlHistory.add(this.content.id);
	    	case "save":
	    		if (validateForm('config_form')) {
	    			App.Config.update(this.content);
	    		}
	    		break;
	    	case "delete":
	    		var router = this.get('target');
	    		App.Config.remove(this.content, function(data){ router.transitionTo('config_list'); });
	    		break;
	    	}	
	    },
    	moveTo: function(route) {
    		if (validateForm('config_form')) {
    			var router = this.get('target');
    			router.transitionTo(route);
    		}
    	},
    	addFormField: function() { this.content.formInputValues.pushObject({name: '', value: ''}); },
    	removeFormField: function(item) { this.content.formInputValues.removeObject(item); }
    });
     
    App.HistoryListController = Ember.ArrayController.extend({ itemController: 'historyListItem' });
    App.HistoryListItemController = Ember.ObjectController.extend({
    	formatCreateTime: function(){ return new Date(this.get('createTime')); }.property('createTime'),
    	configURL: function() { return '#/' + this.get('configurationId'); }.property('configurationId')
    });
    
    
    //Routing
    App.Router.map(function() {
   		this.resource("config_list", { path: "/" });
   		this.resource("config", {path: "/:id"}, function(){
   			this.route("advanced");
   			this.route("plugins");
   			this.resource("config_history", {path: "history"});
   		});
   		this.route("new");
   		this.resource("about");
   		this.resource("contact")
   		this.resource("history_list", {path: "/history"}, function() {
   			this.resource("history", {path: "/:id"})
   		});
   		this.resource("manage");
   	});

    
    App.ConfigListRoute = Ember.Route.extend({
      setupController: function(controller, model) {
        controller.set('content', App.Config.findAll());
        controller.set('sidenav',
        	[App.Link.create({text:"New Configuration", target:"#/new"}), 
        	 App.Link.create({text:"Crawl History", target:"#/history"}), 
        	 App.Link.create({text:"Manage Crawljax", target:"#/manage"})]);
        controller.set('breadcrumb', [App.Link.create({text: "Home"})]);
      }
    });
    
    App.NewRoute = Ember.Route.extend({
    	setupController: function(controller, model) {
    		controller.set('content', App.Config.newConfig());
    		controller.set('sidenav',
            	[App.Link.create({text:"Save Configuration", target:"add", action:true})]);
    		controller.set('breadcrumb', [App.Link.create({text: "Home", target: "#"}), App.Link.create({text: "New"})]);
        }
    });
    
    App.ConfigRoute = Ember.Route.extend({
    	setupController: function(controller, model) {
            controller.set('content', model);
            controller.set('sidenav',
            		[App.Link.create({text:"Run Configuration", target:"run", action:true}),
            		 App.Link.create({text:"Save Configuration", target:"save", action:true}),
            		 App.Link.create({text:"Crawl History", target:"#/"+model.id+"/history"}),
            		 App.Link.create({text:"Delete Configuration", target:"delete", action:true})]);
            controller.set('breadcrumb', [App.Link.create({text: "Home", target: "#"}), 
                                          App.Link.create({text: 'Configuration', target: "#/" + model.id})]);
        },
        serialize: function(object) { return { id: object.id }; },
        deserialize: function(params) { return App.Config.find(params.id); }
    });
    
    App.ConfigIndexRoute = Ember.Route.extend({
    	renderTemplate: function(){ this.render({controller: 'config'}); }
    });
    
    App.ConfigAdvancedRoute = Ember.Route.extend({
    	renderTemplate: function(){ this.render({controller: 'config'}); }
    });
    
    App.ConfigPluginRoute = Ember.Route.extend({
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
            	[App.Link.create({text:"All Configurations", target:"#"}),
            	 App.Link.create({text:"New Configuration", target:"#/new"}),  
            	 App.Link.create({text:"Manage Crawljax", target:"#/manage"})]);
            controller.set('breadcrumb', [App.Link.create({text: "Home", target: "#"}), App.Link.create({text: "History"})]);
        }
   }); 
    
    App.HistoryListIndexRoute = Ember.Route.extend({
    	setupController: function(controller, model) { 
    		this.controllerFor('history_list').set('content', App.CrawlHistory.findAll()); },
    	renderTemplate: function(){ this.render({controller: 'history_list'}); }
    });