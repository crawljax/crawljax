    App = Ember.Application.create();
    
    //Views
    App.SideNavView = Ember.CollectionView.extend({
  	  tagName: "ul",
  	  classNames: "nav nav-list",
  	  itemViewClass: Ember.View.extend({ template: Ember.Handlebars.compile("<a {{bindAttr href='view.content.route'}}>{{view.content.text}}</a>") }),
  	  contentBinding: "App.sideNavController.content"
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
    	contentBinding: "App.breadcrumbController.content"
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
    			 success: function(response){
    				this.setProperties(response);
    			 }
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
    			 success: function(response){
    				this.setProperties(response);
    			 }
    		 });
    		 return config;
    	 }
	});
    
    //Controllers/Routes
    App.Router.map(function() {
   		this.resource("config_list", { path: "/" });
   		this.resource("config", {path: "/:id"}, function(){
   			this.route("run");
   			this.route("save");
   			this.route("input");
   			this.route("plugins");
   			this.resource("history");
   		});
   		this.route("new");
   		this.resource("about");
   		this.resource("contact")
   		this.resource("people");
   		this.resource("history");
   		this.resource("manage");
   	});
    //App.Router.reopen({ location: 'history' });
    
    App.sideNavController = Ember.ArrayController.create({});
    App.breadcrumbController = Ember.ArrayController.create({});
    
    App.ConfigListController = Ember.ArrayController.extend({});
    App.ConfigListRoute = Ember.Route.extend({
      setupController: function(controller, model) {
        controller.set('content', App.Config.findAll());
        App.sideNavController.set('content',
        		[{text:"New Configuration", route:"#/new"}, {text:"Crawl History", route:"#/history"}, {text:"Manage Crawljax", route:"#/manage"}]);
        App.breadcrumbController.set('content', [{text: "Home", route: "/"}]);
      }
    });
    
    App.NewRoute = Ember.Route.extend({
    	setupController: function(controller, model) {
    		controller.set('content', App.Config.newConfig());
    		App.sideNavController.set('content',
            	[{text:"Save Configuration", route:"#/save"}]);
    		App.breadcrumbController.set('content', [{text: "Home", route: "/"}, {text: "New", route: ""}]);
        }
    });
    
    App.ConfigRoute = Ember.Route.extend({
    	setupController: function(controller, model) {
            controller.set('content', model);
            App.sideNavController.set('content',
            		[{text:"Run Configuration", route:"#/"+model.id+"/run"},
            		 {text:"Save Configuration", route:"#/"+model.id+"/save"},
            		 {text:"Crawl History", route:"#/"+model.id+"/history"}]);
            App.breadcrumbController.set('content', [{text: "Home", route: "/"}, {text: model.name, route: "#/" + model.id}]);
        },
        serialize: function(object) { return { id: object.id }; },
        deserialize: function(params) { 
        	return App.Config.find(params.id); }
    });
    
    App.ConfigIndexController = Ember.Controller.extend({needs: ["config"]});