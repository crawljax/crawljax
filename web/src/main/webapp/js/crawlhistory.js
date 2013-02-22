App.CrawlHistory = Ember.Object.extend();

App.CrawlHistory.reopenClass({
	isSaving: false,
	findAll: function(id){
		var allHistory = [];
		var data = '';
		if (id !== undefined) data = { config: id };
	    $.ajax({
	      url: '/rest/history',
	      dataType: 'json',
	      context: allHistory,
	      data: data,
	      success: function(response){
	        response.forEach(function(history){
	          this.addObject(App.CrawlHistory.create(history))
	        }, this);
	      }
	    });
	    return allHistory;
	 },
	 add: function(configId, callback)
	 {
		 if (!this.isSaving) {
			 this.isSaving = true;
			 $.ajax({
				 url: '/rest/history',
				 type: 'POST',
				 contentType: "application/json;",
				 data: configId,
				 dataType: 'json',
				 context: this,
				 success: function(response){ 
					 this.isSaving = false;
					 if (callback !== undefined) callback(response);
			 	}
			 });
		 }
	 }
});