    App = Ember.Application.create();
    
    //Views
    App.SideNavView = Ember.CollectionView.extend({
  	  tagName: "ul",
  	  classNames: "nav nav-list",
  	  itemViewClass: Ember.View.extend({ template: Ember.Handlebars.compile("<a {{bindAttr href='view.content.route'}}>{{view.content.text}}</a>") }),
  	  contentBinding: "App.sideNav.content"
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
    	contentBinding: "App.breadcrumb.content"
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
    	        response.data.forEach(function(config){
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
    				this.setProperties(response.data);
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
   		this.resource("about");
   		this.resource("contact")
   		this.resource("people");
   		this.resource("history");
   		this.resource("manage");
   	});
    //App.Router.reopen({ location: 'history' });
    
    App.SideNavController = Ember.ArrayController.extend({ content: [] });
    App.sideNav = App.SideNavController.create({});
    App.BreadcrumbController = Ember.ArrayController.extend({ content: [] });
    App.breadcrumb = App.BreadcrumbController.create({});
    
    App.ConfigListController = Ember.ArrayController.extend({});
    App.ConfigListRoute = Ember.Route.extend({
      setupController: function(controller, model) {
        controller.set('content', App.Config.findAll());
        App.sideNav.set('content',
        		[{text:"New Configuration", route:"#/new"}, {text:"People", route:"#/people"},
  	            {text:"Crawl History", route:"#/history"}, {text:"Manage Crawljax", route:"#/manage"}]);
        App.breadcrumb.set('content', [{text: "Home", route: "/"}]);
      }
    });
    
    App.ConfigRoute = Ember.Route.extend({
    	setupController: function(controller, model) {
            controller.set('content', model);
            App.sideNav.set('content',
            		[{text:"Run Configuration", route:"#/"+model.id+"/run"},
            		 {text:"Save Configuration", route:"#/"+model.id+"/save"},
            		 {text:"Crawl History", route:"#/"+model.id+"/history"}]);
            App.breadcrumb.set('content', [{text: "Home", route: "/"}, {text: model.name, route: "#/" + model.id}]);
        },
        serialize: function(object) { return { id: object.id }; },
        deserialize: function(params) { return App.Config.find(params.id); }
    });
    
    App.ConfigIndexRoute = Ember.Route.extend({
    	model: function(params) { return this.modelFor("config"); }
    });