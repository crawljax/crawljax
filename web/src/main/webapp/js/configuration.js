App.Config = Ember.Object.extend();

App.Config.reopenClass({
    	allConfigs: [],
    	isSaving: false,
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
    		 if (!this.isSaving) {
    			 this.isSaving = true;
    			 $.ajax({
    				 url: '/rest/configurations',
    				 type: 'POST',
    				 contentType: "application/json;",
    				 data: JSON.stringify(config),
    				 dataType: 'json',
    				 context: this,
    				 success: function(response){ 
    					 config.setProperties(response);
    					 this.isSaving = false;
    					 if (callback !== undefined) callback(config);
    			 	}
    			 });
    		 }
    		 return config;
    	 },
    	 update: function(config)
    	 {
    		 if (!this.isSaving) {
    			 this.isSaving = true;
    			 $.ajax({
    				 url: '/rest/configurations/' + config.id,
    				 type: 'PUT',
    				 contentType: "application/json;",
    				 data: JSON.stringify(config),
    				 dataType: 'json',
    				 context: this,
    				 success: function(response){ 
    					 config.setProperties(response);
    					 this.isSaving = false;
    				 }
    			 });
    		 }
    		 return config;
    	 },
    	 remove: function(config, callback)
    	 {
    		 if (!this.isSaving) {
    			 this.isSaving = true;
    			 $.ajax({
    				 url: '/rest/configurations/' + config.id,
    				 type: 'DELETE',
    				 contentType: "application/json;",
    				 data: JSON.stringify(config),
    				 dataType: 'json',
    				 context: this,
    				 success: function(response){ 
    					 this.isSaving = false;
    					 if (callback !== undefined) callback(config); 
    				 }
    			 });
    		 }
    	 }
});