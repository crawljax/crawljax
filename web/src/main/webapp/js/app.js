var app = angular.module('crawljaxApp', ['ui.router']);

app.config(['$stateProvider', '$urlRouterProvider', function ($stateProvider, $urlRouterProvider) {
	$stateProvider
		.state('config',
			{
				url: '/configurations',
				controller: 'ConfigIndexController',
				templateUrl: 'partials/config.html',
				resolve: {
					configs: function(configHttp){
						return configHttp.getConfigurations();
					}
				}
			})
		.state('configNew',
			{
				url: '/configurations/new',
				controller: 'ConfigNewController',
				templateUrl: 'partials/configNew.html',
				resolve: {
					config: function(configHttp){
						return configHttp.getNewConfiguration();
					}
				}
			})
		.state('configCopy',
			{
				url: '/configurations/new/:copyId',
				controller: 'ConfigCopyController',
				templateUrl: 'partials/configNew.html',
				resolve: {
					config: function($stateParams, configHttp){
						return configHttp.getConfiguration($stateParams.copyId);
					}
				}
			})	
		.state('configDetail',
			{
				url: '/configurations/:configId',
				templateUrl: 'partials/configView/configDetail.html',
				controller: 'ConfigController',
				resolve: {
					config: function($stateParams, configHttp){
						return configHttp.getConfiguration($stateParams.configId);
					}
				}
			})
		.state('configDetail.main',
			{
				url: '^/configurations/:configId/',
				templateUrl: 'partials/configView/configDetail.main.html'
			})
		.state('configDetail.rules',
			{
				url: '^/configurations/:configId/rules',
				templateUrl: 'partials/configView/configDetail.rules.html'
			})
		.state('configDetail.assertions',
			{
				url: '^/configurations/:configId/assertions',
				templateUrl: 'partials/configView/configDetail.assertions.html'
			})
		.state('configDetail.plugins',
			{
				url: '^/configurations/:configId/plugins',
				templateUrl: 'partials/configView/configDetail.plugins.html',
				controller: 'ConfigPluginsController',
				resolve: {
					plugins: function(pluginHttp){
						return pluginHttp.getPlugins();
					}
				}
			})
		.state('plugins',
			{
				url: '/plugins',
				controller: 'PluginsController',
				templateUrl: 'partials/plugins.html',
				resolve: {
					plugins: function(pluginHttp){
						return pluginHttp.getPlugins();
					}
				}
			})
		.state('history',
			{
		    	url: '/history',
				controller: 'HistoryIndexController',
    			templateUrl: 'partials/history.html',
				resolve: {
					crawlRecords: function(historyHttp){
						return historyHttp.getHistory();
					}
				}
			})
		.state('history.filter',
			{
				url: '/filter/:filter',
				controller: 'HistoryIndexController',
				templateUrl: 'partials/history.html'
			})
		.state('crawl', 
			{
				url: '/history/:crawlId',
				templateUrl: 'partials/crawlView/crawl.html',
				controller: 'CrawlRecordController',
				resolve: {
					crawl: function($stateParams, historyHttp){
						return historyHttp.getCrawl($stateParams.crawlId);
					}
				}
			})
		.state('crawl.log',
			{
				url: '^/history/:crawlId/log',
				templateUrl: 'partials/crawlView/crawl.log.html'
			})
		.state('crawl.pluginOutput',
			{
				url: '^/history/:crawlId/plugin/:pluginId',
				templateUrl: 'partials/crawlView/crawl.pluginOutput.html',
				controller: 'CrawlRecordPluginController'
			});
	$urlRouterProvider
		.otherwise('/configurations');
}]);

app.run(['$rootScope', '$state', '$stateParams', '$http', 'configHttp', 'pluginHttp', 'historyHttp', 'socket', 
         function($rootScope, $state, $stateParams, $http, configHttp, pluginHttp, historyHttp, socket){
	$rootScope.$state = $state;
	$rootScope.$stateParams = $stateParams;
	
	$rootScope.browsers = [{name: "Mozilla Firefox", value: "FIREFOX"}, 
	                       {name: "Google Chrome", value: "CHROME"}, 
	                       {name: "Internet Explorer", value: "INTERNET EXPLORER"}, 
	                       {name: "PhantomJS", value:"PHANTOMJS"}];
	
	$rootScope.clickConditions = [
		{name: "With Attribute (name=value):", value:"wAttribute"},
		{name: "With Text:", value:"wText"},
		{name: "Under XPath:", value:"wXPath"},
		{name: "When URL contains:", value:"url"},
		{name: "When URL does not contain:", value:"notUrl"},
		{name: "When Regex:", value:"regex"},
		{name: "When not Regex:", value:"notRegex"},
		{name: "When XPath:", value:"xPath"},
		{name: "When not XPath:", value:"notXPath"},
		{name: "When element is visible with id:", value:"visibleId"},
		{name: "When element is not visible with id:", value:"notVisibleId"},
		{name: "When element is visible with text:", value:"visibleText"},
		{name: "When element is not visible with text:", value:"notVisibleText"},
		{name: "When element is visible with tag:", value:"visibleTag"},
		{name: "When element is not visible with tag:", value:"notVisibleTag"},
		{name: "When Javascript is true:", value:"javascript"}];
	
	$rootScope.clickType = [{name: "Click", value: "click"}, {name: "Don't Click", value: "noClick"}];
	
	$rootScope.tags = ["a", "abbr", "address", "area", "article", "aside", "audio",
	                   "button", "canvas", "details", "div", "figure", "footer",
	                   "form", "header", "img", "input", "label", "li", "nav", "ol", 
	                   "section", "select", "span", "summary", "table", "td", "textarea", 
	                   "th", "tr", "ul", "video"];
	
	$rootScope.pageConditions = [
		 {name: "When URL contains:", value:"url"},
		 {name: "When URL does not contain:", value:"notUrl"},
		 {name: "When Regex:", value:"regex"},
		 {name: "When not Regex:", value:"notRegex"},
		 {name: "When XPath:", value:"xPath"},
		 {name: "When not XPath:", value:"notXPath"},
		 {name: "When element is visible with id:", value:"visibleId"},
		 {name: "When element is not visible with id:", value:"notVisibleId"},
		 {name: "When element is visible with text:", value:"visibleText"},
		 {name: "When element is not visible with text:", value:"notVisibleText"},
		 {name: "When element is visible with tag:", value:"visibleTag"},
		 {name: "When element is not visible with tag:", value:"notVisibleTag"},
		 {name: "When Javascript is true:", value:"javascript"}];
	
	$rootScope.comparators = [
		 {name: "Ignore Attribute:", value:"attribute"},
		 {name: "Ignore White Space", value:"simple"},
		 {name: "Ignore Dates", value:"date"},
		 {name: "Ignore Scripts", value:"script"},
		 {name: "Only observe plain DOM structure", value:"plain"},
		 {name: "Ignore Regex:", value:"regex"},
		 {name: "Ignore XPath:", value:"xPath"},
		 {name: "Ignore within Distance Edit Threshold:", value:"distance"}];
	
	$rootScope.configurations = [];
	$rootScope.plugins = [];
	$rootScope.crawlRecords = [];
	
	historyHttp.getHistory(true).then(function(data){
		socket.executionQueue = data;
	});
	
	if(!("WebSocket" in window)) {
		alert('Need a browser that supports Sockets');
	} else {
		socket.connectSocket();
	}
	
	$rootScope.$on('$stateChangeStart', function(event, toState, fromState){
		if(typeof angular.element("#config_form")[0] != "undefined"){
			switch(fromState.name){
				case 'configDetail.main':
				case 'configDetail.rules':
				case 'configDetail.assertions':
				case 'configDetail.plugins':
					if(!validateForm('config_form')) event.preventDefault();
					break;
				default:
					break;
			}
		}
	});
	
	$rootScope.$on('$stateChangeSuccess', function(event, toState, fromState){
		var sideNav = angular.element("#sideNav").scope();
		var breadcrumb = angular.element("#breadcrumb").scope();
		switch(toState.name){
			case 'config':
				sideNav.links = [{icon: 'icon-pencil', target: 'configNew', action: false, text: 'New Configuration'}];
				breadcrumb.links = [{text: 'Configurations'}];
				break;
			case 'configDetail.main':
			case 'configDetail.rules':
			case 'configDetail.assertions':
			case 'configDetail.plugins':
				sideNav.links = [{icon: 'icon-play', target: 'run', action: true, text: 'Run Configuration'},
								{icon: 'icon-ok', target: 'save', action: true, text: 'Save Configuration'},
								{icon: 'icon-book', target: 'history.filter({filter: $stateParams.configId})', action: false, text: 'Crawl History'},
								{icon: 'icon-pencil', target: 'configCopy({copyId: $stateParams.configId})', action: false, text: 'New Copy'},
								{icon: 'icon-remove', target: 'delete', action: true, text: 'Delete Configuration'}];
				breadcrumb.links = [{text: 'Configurations', target: 'config'}, {text: $stateParams.configId}]
				break;
			case 'configNew':
			case 'configCopy':
				sideNav.links = [{icon: 'icon-ok', target: 'save', action: true, text: 'Save Configuration'}];
				breadcrumb.links = [{text: 'Configurations', target: 'config'}, {text: 'New'}];
				break;
			case 'plugins':
				breadcrumb.links = [{text: 'Plugins'}];
				sideNav.links = [];
				break;
			case 'history':
				breadcrumb.links = [{text: 'History'}];
				sideNav.links = [];
				break;
			case 'history.filter':
				sideNav.links = [{icon: 'icon-book', target: 'history', action: false, text: 'All Crawl Records'}];
				breadcrumb.links = [{text: 'History'}];
				break;
			case 'crawl.log':
			case 'crawl.pluginOutput':
				sideNav.links = [{icon: 'icon-wrench', target: 'configDetail.main({configId: configId})', action: false, text: 'Configuration'}]
				breadcrumb.links = [{text: 'History', target: 'history'}, {text: 'Crawl ' + $stateParams.crawlId}];
				break;
			default:
				sideNav.links = [];
				break;
		}
	});
}])