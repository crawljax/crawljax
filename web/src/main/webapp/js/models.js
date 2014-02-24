
App.Link = Ember.Object.extend({ text: null, icon: null, target: null, action: false });
		
App.browsers = [
		 Ember.Object.create({name: "Mozilla Firefox", value:"FIREFOX"}),
		 Ember.Object.create({name: "Google Chrome", value:"CHROME"}),
		 Ember.Object.create({name: "Microsoft Internet Explorer", value:"INTERNET_EXPLORER"})
	];
		
App.clickType = [Ember.Object.create({name: "Click", value:"click"}),
		Ember.Object.create({name: "Don't Click", value:"noClick"})];
		
App.tags = ["a", "abbr", "address", "area", "article", "aside", "audio",
		"button", "canvas", "details", "div", "figure", "footer",
		"form", "header", "img", "input", "label", "li", "nav", "ol", 
		"section", "select", "span", "summary", "table", "td", "textarea", 
		"th", "tr", "ul", "video"];
		
App.clickConditions = [
		Ember.Object.create({name: "With Attribute (name=value):", value:"wAttribute"}),
		Ember.Object.create({name: "With Text:", value:"wText"}),
		Ember.Object.create({name: "Under XPath:", value:"wXPath"}),
		Ember.Object.create({name: "When URL contains:", value:"url"}),
		Ember.Object.create({name: "When URL does not contain:", value:"notUrl"}),
		Ember.Object.create({name: "When Regex:", value:"regex"}),
		Ember.Object.create({name: "When not Regex:", value:"notRegex"}),
		Ember.Object.create({name: "When XPath:", value:"xPath"}),
		Ember.Object.create({name: "When not XPath:", value:"notXPath"}),
		Ember.Object.create({name: "When element is visible with id:", value:"visibleId"}),
		Ember.Object.create({name: "When element is not visible with id:", value:"notVisibleId"}),
		Ember.Object.create({name: "When element is visible with text:", value:"visibleText"}),
		Ember.Object.create({name: "When element is not visible with text:", value:"notVisibleText"}),
		Ember.Object.create({name: "When element is visible with tag:", value:"visibleTag"}),
		Ember.Object.create({name: "When element is not visible with tag:", value:"notVisibleTag"}),
		Ember.Object.create({name: "When Javascript is true:", value:"javascript"})];
		
App.pageConditions = [
		 Ember.Object.create({name: "When URL contains:", value:"url"}),
		 Ember.Object.create({name: "When URL does not contain:", value:"notUrl"}),
		 Ember.Object.create({name: "When Regex:", value:"regex"}),
		 Ember.Object.create({name: "When not Regex:", value:"notRegex"}),
		 Ember.Object.create({name: "When XPath:", value:"xPath"}),
		 Ember.Object.create({name: "When not XPath:", value:"notXPath"}),
		 Ember.Object.create({name: "When element is visible with id:", value:"visibleId"}),
		 Ember.Object.create({name: "When element is not visible with id:", value:"notVisibleId"}),
		 Ember.Object.create({name: "When element is visible with text:", value:"visibleText"}),
		 Ember.Object.create({name: "When element is not visible with text:", value:"notVisibleText"}),
		 Ember.Object.create({name: "When element is visible with tag:", value:"visibleTag"}),
		 Ember.Object.create({name: "When element is not visible with tag:", value:"notVisibleTag"}),
		 Ember.Object.create({name: "When Javascript is true:", value:"javascript"})];
		
App.comparators = [
		 Ember.Object.create({name: "Ignore Attribute:", value:"attribute"}),
		 Ember.Object.create({name: "Ignore White Space", value:"simple"}),
		 Ember.Object.create({name: "Ignore Dates", value:"date"}),
		 Ember.Object.create({name: "Ignore Scripts", value:"script"}),
		 Ember.Object.create({name: "Only observe plain DOM structure", value:"plain"}),
		 Ember.Object.create({name: "Ignore Regex:", value:"regex"}),
		 Ember.Object.create({name: "Ignore XPath:", value:"xPath"}),
		 Ember.Object.create({name: "Ignore within Distance Edit Threshold:", value:"distance"})];

//Configuration Model
App.Configurations = Ember.Object.extend();
App.Configurations.reopenClass({
	findAll: function() {
		var configurations = [];
		$.ajax({
			url: '/rest/configurations',
			async: false,
			dataType: 'json',
			context: configurations,
			success: function(response) {
				response.forEach(function(configuration) {
					this.addObject(App.Configurations.create(configuration))
				}, this);
			}
		});
		return configurations;
	},
	find: function(id) {
		var configuration = App.Configurations.create({id: id });
		$.ajax({
			url: '/rest/configurations/' + id,
			async: false,
			dataType: 'json',
			context: configuration,
			success: function(response) {
				this.setProperties(response); 
			}
		});
		return configuration;
	},
	getNew: function(id) {
		var config = App.Configurations.create({});
		$.ajax({
			url: '/rest/configurations/new' + (id ? '/' + id : ""),
			async: false,
			dataType: 'json',
			context: config,
			success: function(response){
				this.setProperties(response);
			}
		});
		return config;
	},
	add: function(config, callback)
	{
		$.ajax({
			url: '/rest/configurations',
			async: false,
			type: 'POST',
			contentType: "application/json;",
			data: JSON.stringify(config, this.cleanJSON),
			dataType: 'json',
			context: config,
			success: function(response){
				this.setProperties(response);
				if (callback !== undefined) callback(this);
			}
		});
		return config;
	},
	update: function(config)
	{
		$.ajax({
			url: '/rest/configurations/' + config.id,
			async: false,
			type: 'PUT',
			contentType: "application/json;",
			data: JSON.stringify(config, this.cleanJSON),
			dataType: 'json',
			context: config,
			success: function(response){ 
				this.setProperties(response);
			}
		});
		return config;
	},
	remove: function(config, callback)
	{
		$.ajax({
			url: '/rest/configurations/' + config.id,
			async: false,
			type: 'DELETE',
			contentType: "application/json;",
			data: JSON.stringify(config, this.cleanJSON),
			dataType: 'json',
			context: config,
			success: function(response){ 
				if (callback !== undefined) callback(config); 
			}
		});
	},
	cleanJSON: function(key, value){ 
		return value;
	}
});

App.Plugins = Ember.Object.extend();
App.Plugins.reopenClass({
	findAll: function(){
		var plugins = [];
		$.ajax({
			url: '/rest/plugins',
			async: false,
			dataType: 'json',
			context: plugins,
			success: function(response){
				response.forEach(function(plugin){
					this.addObject(App.Plugins.create(plugin));
				}, this);
			}
		});
		return plugins;
	},
	refresh: function(callback){
		var plugins = [];
		$.ajax({
			url: '/rest/plugins',
			async: true,
			type: 'PUT',
			dataType: 'json',
			context: plugins,
			success: function(response){
				response.forEach(function(plugin){
					this.addObject(App.Plugins.create(plugin));
				}, this);
				if (callback !== undefined) callback(plugins);
			}
		});
	},
	find: function(id){
		var plugin = App.Plugins.create({id: id });
		$.ajax({
			url: '/rest/plugins/' + id,
			async: false,
			dataType: 'json',
			context: plugin,
			success: function(response){
				this.setProperties(response);
			}
		});
		return plugin;
	},
	selectItems: function(){
		selectItems = App.Plugins.findAll();
		for(var i = 0, l = selectItems.length; i < l; i++){
			selectItems[i] = Ember.Object.create({name: selectItems[i].id, value: selectItems[i].id});
		}
		selectItems.splice(0, 0, Ember.Object.create({name: "Select Plugin", value: ""}));
		return selectItems;
	},
	add: function(fileName, data, url, callback) {
		var fd = new FormData();
		fd.append("name", fileName);
		if(data) {
			fd.append("file", data);
		} else if(url) {
			fd.append("url", url);
		}
		$.ajax({
			url: '/rest/plugins',
			async: true,
			type: 'POST',
			data: fd,
			processData: false,
			contentType: false,
			success: function(response) {
				if (callback !== undefined) callback();
			}
		});
	},
	remove: function(plugin, callback)
	{
		$.ajax({
			url: '/rest/plugins/' + plugin.id,
			async: false,
			type: 'DELETE',
			contentType: "application/json;",
			data: JSON.stringify(plugin, this.cleanJSON),
			dataType: 'json',
			context: plugin,
			success: function(response){ 
				if (callback !== undefined) callback(plugin); 
			}
		});
	}
});

App.CrawlRecords = Ember.Object.extend();
App.CrawlRecords.reopenClass({
	findAll: function(configId, active){
		var records = [];
		var data = '';
		if (configId !== undefined) data = { config: configId };
		if (active) data = { active: true };
		$.ajax({
			url: '/rest/history',
			async: false,
			dataType: 'json',
			context: records,
			data: data,
			success: function(response){
				response.forEach(function(record){
					var pluginArray = [];
					pluginArray.push({key: "0", name: "Crawl Overview"});
					for(key in record.plugins){
						record.plugins[key].key = key;
						pluginArray.push(record.plugins[key]);
					}
					record.plugins = pluginArray;
					this.addObject(App.CrawlRecords.create(record))
				}, this);
			}
		});
		return records;
	},
	find: function(id){
		var record = App.CrawlRecords.create({id: id });
		$.ajax({
			url: '/rest/history/' + id,
			async: false,
			dataType: 'json',
			context: record,
			success: function(response){
				var pluginArray = [];
				pluginArray.push({key: "0", name: "Crawl Overview"});
				for(key in response.plugins){
					response.plugins[key].key = key;
					pluginArray.push(response.plugins[key]);
				}
				response.plugins = pluginArray;
				this.setProperties(response);
			}
		});
		return record;
	},
	add: function(configId, callback){
		var record = App.CrawlRecords.create({configurationId: configId});
		$.ajax({
			url: '/rest/history',
			async: false,
			type: 'POST',
			contentType: "application/json;",
			data: configId,
			dataType: 'json',
			context: record,
			success: function(response){
				var pluginArray = [];
				pluginArray.push({key: "0", name: "Crawl Overview"});
				for(key in response.plugins){
					response.plugins[key].key = key;
					pluginArray.push(response.plugins[key]);
				}
				response.plugins = pluginArray;
				this.setProperties(response);
				if (callback !== undefined) callback(response);
			}
		});
		return record;
	}
});
