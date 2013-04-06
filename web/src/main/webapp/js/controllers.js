	//********************************************
	// controllers.js
	// 
	// Contains the code for all the controllers
	//********************************************
	
	App.ApplicationController = Ember.Controller.extend({
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
	            		 var record = App.CrawlHistory.create({});
	            	 	 record.setProperties(JSON.parse(msg.data.slice(6)));
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
	            		 setTimeout(function(){controller.removeQueue(msg.data.slice(8));}, 5000);
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
       }
	});

	App.ConfigListController = Ember.ArrayController.extend({
		needs: ['application'],
		itemController: 'configListItem', 
    	sideNavDisabled: function() { 
    		return this.get('content.isLoading'); }.property('content.isLoading')
	});
    App.ConfigListItemController = Ember.ObjectController.extend({
    	formatLastCrawl : function() {
    		lastCrawl = this.get('lastCrawl');
    		if (lastCrawl == null ) return 'never';
    		else return new Date(lastCrawl); }.property('lastCrawl'),
    	formatLastDuration : function() {
    		lastDuration = this.get('lastDuration')/1000;
    		if (lastDuration == 0 ) {
    			if (this.get('lastCrawl') == null) return 'n/a';
    			else return 'running';
    		}
    		else return Math.floor(lastDuration/60) + ' min ' + Math.floor(lastDuration%60) + ' sec';
    	}.property('lastCrawl', 'lastDuration')
    });
    
    App.ConfigController = Ember.Controller.extend({
    	needs: ['application'],
    	rest: function(link){
	    	switch(link.target)
	    	{
	    	case "add":
	    		if (validateForm('config_form')) {
	    			var router = this.get('target');
	    			App.Config.add(this.content, function(data){ router.transitionTo('config', data); });
	    		}
	    		break;
	    	case "run":
	    		var record = App.CrawlHistory.add(this.content.id);
	    		break;
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
    	sideNavDisabled: function() { 
    		return this.get('content.isLoading') || this.get('content.isSaving'); 
    	}.property('content.isLoading', 'content.isSaving')
    });
    
    App.ClickRulesController = Ember.ArrayController.extend({
    	add: function() { this.content.pushObject({rule: 'click', elementTag: 'a', conditions: []}); },
    	remove: function(item) { 
    		this.content.removeObject(item); },
    	itemController: 'clickRule'
    });
    App.ClickRuleController = Ember.ObjectController.extend({
    	addCondition: function() { this.content.conditions.pushObject({condition: 'wAttribute', expression: ''}); },
    	removeCondition: function(item) { 
    		this.content.conditions.removeObject(item); }
    })
    
    App.ConditionsController = Ember.ArrayController.extend({
    	add: function() { this.content.pushObject({condition: 'url', expression: ''}); },
    	remove: function(item) { this.content.removeObject(item); }
    });
    
    App.ComparatorsController = Ember.ArrayController.extend({
    	add: function() { this.content.pushObject({type: 'attributes', expression: ''}); },
    	remove: function(item) { this.content.removeObject(item); },
    	itemController: 'comparator'
    });
    App.ComparatorController = Ember.ObjectController.extend({
    	needsExpression: function() { 
    		var type = this.get('type');
    		return ( type == 'attribute' || type == 'xpath' || type == 'distance' || type == 'regex' ); }.property('type')
    });
    
    App.FormInputsController = Ember.ArrayController.extend({
    	add: function() { this.content.pushObject({name: '', value: ''}); },
    	remove: function(item) { this.content.removeObject(item); }
    });
     
    App.HistoryListController = Ember.ArrayController.extend({
    	needs: ['application'],
    	itemController: 'historyListItem'
    });
    App.HistoryListItemController = Ember.ObjectController.extend({
    	formatStartTime: function(){ 
    		var startTime = this.get('startTime');
    		if (startTime == null) return 'queued';
    		else return new Date(startTime); 
    	}.property('startTime'),
    	configURL: function() { return '#/' + this.get('configurationId'); }.property('configurationId')
    });
    
    App.HistoryController = Ember.Controller.extend({
    	needs: ['application'],
    	isFinished: function(){
    		return (this.get('content.crawlStatus') == 'success');
    	}.property('content.crawlStatus'),
    	overviewURL : function(){
    		return "/output/" + this.get('content.id') + "/crawloverview/index.html";
    	}.property('content.id')
    });