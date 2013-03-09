	App.ConfigListController = Ember.ArrayController.extend({
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
    		else return Math.floor(lastDuration/60) + ' mins ' + Math.floor(lastDuration%60) + ' secs';
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
	    		App.CrawlHistory.add(this.content.id);
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
     
    App.HistoryListController = Ember.ArrayController.extend({ itemController: 'historyListItem' });
    App.HistoryListItemController = Ember.ObjectController.extend({
    	formatStartTime: function(){ 
    		var startTime = this.get('startTime');
    		if (startTime == null) return 'queued';
    		else return new Date(startTime); 
    	}.property('startTime'),
    	configURL: function() { return '#/' + this.get('configurationId'); }.property('configurationId')
    });