    App = Ember.Application.create();
    
    //Views
    App.HeaderView = Ember.View.extend({templateName: "header"});
    App.SideNavView = Ember.View.extend({
    	  template: Ember.Handlebars.compile("<ul class='nav nav-list'>{{#each view.content}}<li><a {{bindAttr href='route'}}>{{text}}</a></li>{{/each}}</ul>"),
    	  content: [{text:"New Configuration", route:"#/new"}, {text:"People", route:"#/people"},
    	            {text:"Crawl History", route:"#/history"}, {text:"Manage Crawljax", route:"#/manage"}]
    	});
    
    App.BreadcrumbView = Ember.View.extend({
  	  templateName: 'breadcrumb',
  	  content: [{text: "Home", route: "/", active: true}]
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
    	        }, this)
    	      }
    	    })
    	    return this.allConfigs;
    	 },
    	 find: function(id)
    	 {
    		 var config = App.Config.create({id: id});
    			 $.ajax({
    			    url: '/rest/configurations/' + id,
    			    dataType: 'json',
    			    context: config,
    			    success: function(response){
    			      this.setProperties(response.data);
    			    }
    			 })
    		return config;
    	 }
	});
    
    //Controllers/Routes
    App.Router.map(function() {
   		this.resource("config_list", { path: "/" });
   		this.resource("config", {path: "/:id"});
   		this.resource("people");
   		this.resource("history");
   		this.resource("manage");
   	});
    //App.Router.reopen({ location: 'history' });
    
    App.ConfigListController = Ember.ArrayController.extend({});
    App.ConfigListRoute = Ember.Route.extend({
      setupController: function(controller, model) {
        controller.set('content', App.Config.findAll()); }
    });
    
    App.ConfigController = Ember.Controller.extend({});
    App.ConfigRoute = Ember.Route.extend({
        setupController: function(controller, model) {
          controller.set('content', model); },
        serialize: function(object) { return { id: object.id }; },
        deserialize: function(params) { return App.Config.find(params.id); }
      });