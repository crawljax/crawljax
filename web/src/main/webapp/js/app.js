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
    		template: Ember.Handlebars.compile('<a href="#">{{view.content.text}}</a><span class="divider">/</span>')
    	}),
    	lastItemViewClass: Ember.View.extend({
    		classNames: "active",
    		template: Ember.Handlebars.compile("{{view.content.text}}")
    	}),
    	contentBinding: "controller.breadcrumb"
  	});
    
    //Models    
    App.Config = Ember.Object.extend();
    App.Config.reopenClass({
    	allConfigs: [],
    	findAll: function(){
    		this.allConfigs.length = 0;
    	    $.ajax({
    	      url: '/rest/configurations',
    	      dataType: 'json',
    	      context: this,
    	      success: function(response){
    	        response.forEach(function(config){
    	          this.allConfigs.addObject(App.Config.create(config))
    	        }, this);
    	      }
    	    });
    	    return this.allConfigs;
    	 },
    	 find: function(id){
    		 var config = App.Config.create({id: id});
    		 $.ajax({
    			 url: '/rest/configurations/' + id,
    			 dataType: 'json',
    			 context: config,
    			 success: function(response){ this.setProperties(response); }
    		 });
    		 return config;
    	 },
    	 newConfig: function()
    	 {
    		 var config = App.Config.create({});
    		 $.ajax({
    			 url: '/rest/configurations/new',
    			 dataType: 'json',
    			 context: config,
    			 success: function(response){ this.setProperties(response); }
    		 });
    		 return config;
    	 },
    	 add: function(config, callback)
    	 {
    		 $.ajax({
    			 url: '/rest/configurations',
    			 type: 'POST',
    			 contentType: "application/json;",
    			 data: JSON.stringify(config),
    			 dataType: 'json',
    			 context: config,
    			 success: function(response){ this.setProperties(response); }
    		 }).done(function(data){
    			 if (callback !== undefined) callback(data);
    		 });
    		 return config;
    	 },
    	 update: function(config)
    	 {
    		 $.ajax({
    			 url: '/rest/configurations/' + config.id,
    			 type: 'PUT',
    			 contentType: "application/json;",
    			 data: JSON.stringify(config),
    			 dataType: 'json',
    			 context: config,
    			 success: function(response){ this.setProperties(response); }
    		 });
    		 return config;
    	 }
	});
    
    App.Link = Ember.Object.extend({ text: null, target: null, action: false });
    
    App.browsers = [
         Ember.Object.create({name: "Mozilla Firefox", value:"firefox"}),
         Ember.Object.create({name: "Google Chrome", value:"chrome"}),
         Ember.Object.create({name: "Microsoft Internet Explorer", value:"ie"})
	];
    
    //Controllers/Routes
    App.Router.map(function() {
   		this.resource("config_list", { path: "/" });
   		this.resource("config", {path: "/:id"}, function(){
   			this.route("input");
   			this.route("plugins");
   			this.resource("history");
   		});
   		this.route("new");
   		this.resource("about");
   		this.resource("contact")
   		this.resource("history");
   		this.resource("manage");
   	});

    App.NewController = Ember.Controller.extend({
    	rest: function(link){
	    	switch(link.target)
	    	{
	    	case "add":
	    		var router = this.get('target');
	    		var controller = this;
	    		App.Config.add(this.content, 
	    			function(){ router.transitionTo('config', controller.content); });
	    		break;
	    	}
	    }
    });
    
    App.ConfigController = Ember.Controller.extend({
    	rest: function(link){
	    	switch(link.target)
	    	{
	    	case "save":
	    		App.Config.update(this.content);
	    		break;
	    	}
	    }
    });
    
    App.ConfigListController = Ember.ArrayController.extend({});
    App.ConfigListRoute = Ember.Route.extend({
      setupController: function(controller, model) {
        controller.set('content', App.Config.findAll());
        controller.set('sidenav',
        	[App.Link.create({text:"New Configuration", target:"#/new"}), 
        	 App.Link.create({text:"Crawl History", target:"#/history"}), 
        	 App.Link.create({text:"Manage Crawljax", target:"#/manage"})]);
        controller.set('breadcrumb', [{text: "Home", route: "/"}]);
      }
    });
    
    App.NewRoute = Ember.Route.extend({
    	setupController: function(controller, model) {
    		controller.set('content', App.Config.newConfig());
    		controller.set('sidenav',
            	[App.Link.create({text:"Save Configuration", target:"add", action:true})]);
    		controller.set('breadcrumb', [{text: "Home", route: "/"}, {text: "New"}]);
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
            controller.set('breadcrumb', [{text: "Home", route: "/"}, {text: model.name, route: "#/" + model.id}]);
        },
        serialize: function(object) { return { id: object.id }; },
        deserialize: function(params) { return App.Config.find(params.id); }
    });
    
    App.ConfigIndexRoute = Ember.Route.extend({
    	renderTemplate: function(){ this.render({controller: 'config'}) }
    });