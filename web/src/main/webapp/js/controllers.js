app.controller('ConfigIndexController', ['$rootScope', 'configs', function($rootScope, configs){
	$rootScope.configurations = configs;
}]);

app.controller('ConfigController', ['$scope', '$rootScope', '$state', 'configAdd', 'configHttp', 'pluginHttp', 'historyHttp', 'restService', 'config', 
                                    function($scope, $rootScope, $state, configAdd, configHttp, pluginHttp, historyHttp, restService, config){
	$scope.config = config;
	
	restService.changeRest(function(link){
		switch(link.target){
			case 'run':
				historyHttp.addCrawl(config);
				break;
			case 'save':
				if(validateForm('config_form')) configHttp.updateConfiguration($scope.config, $rootScope.$stateParams.configId);
				break;
			case 'delete':
				if(confirm('Are you sure you want to delete this configuration?')){
					configHttp.deleteConfiguration(config, $rootScope.$stateParams.configId).then(function(){
						$state.go('config');
					});
				}
				break;
			default:
				break;
		}
	});
	
	$scope.configAdd = configAdd;
	$scope.setPlugin = function(plugin, configId){
		if(typeof configId != 'undefined'){
			pluginHttp.getPlugin(configId).then(function(data){
				var pluginHolder = data;
				
				for(var property in pluginHolder){
					plugin[property] = pluginHolder[property];
				}
			});
		}
		else{
			for(var property in plugin){
				plugin[property] = undefined;
			}
		}
	}
}]);

app.controller('ConfigPluginsController', ['$rootScope', 'plugins', function($rootScope, plugins){
	$rootScope.plugins = plugins;
}]);

app.controller('ConfigNewController', ['$scope', '$rootScope', '$state', 'restService', 'configHttp', 'config', 
                                       function($scope, $rootScope, $state, restService, configHttp, config){
	$scope.config = config;
	
	restService.changeRest(function(link){
		switch(link.target){
			case 'save':
				if(validateForm('config_form')){
					var idSource = configHttp.postConfiguration($scope.config);
					idSource.then(function(result){
						$state.go('configDetail.main', {configId: result.data.id});
					})
				}
				break;
			default:
				break;
		}
	});
}]);

app.controller('ConfigCopyController', ['$scope', '$rootScope', '$state', 'restService', 'configHttp', 'config', 
                                        function($scope, $rootScope, $state, restService, configHttp, config){
	$scope.config = config;
	
	restService.changeRest(function(link){
		switch(link.target){
			case 'save':
				if(validateForm('config_form')){
					var idSource = configHttp.postConfiguration($scope.config);
					idSource.then(function(result){
						$state.go('configDetail.main', {configId: result.data.id});
					});
				}
				break;
			default:
				break;
		}
	});
}]);

app.controller('PluginsController', ['$scope', '$rootScope', 'pluginHttp', 'pluginAdd', 'restService', 'notificationService', 'plugins', 
                                     function($scope, $rootScope, pluginHttp, pluginAdd, restService, notificationService, plugins){
	$scope.newPluginURL = '';
	$rootScope.plugins = plugins;
	
	if(!(window.File && window.FileReader && window.FileList && window.Blob)){
		alert('The File APIs are not fully supported in this browser.');
	}
	
	$scope.refreshPlugins = function(){
		pluginHttp.getPlugins().then(function(data){
			$rootScope.plugins = data;
		});
	}
	
	$scope.rest = function(link){
		switch(link.target){
			case 'refresh':
				notificationService.notify("Refreshing List...", 0);
				pluginHttp.getPlugins().then(function(data){
					$rootScope.plugins = data;
					notificationService.notify("List Refreshed", 1);
				}, function(data){
					notificationService.notify("Error Refreshing List", -1);
				});
				break;
			case 'upload':
				pluginAdd.addFile(function(){
					$scope.refreshPlugins();
				});
				break;
			case 'add':
				pluginAdd.addURL($scope.newPluginURL, function(){
					$scope.refreshPlugins();
				});
				break;
			case 'delete':
				if(confirm("Are you sure you want to remove " + link.pluginName + " ? (id: " + link.pluginId + ")")){
					pluginHttp.deletePlugin(link.pluginId, link.plugin).then(function(){
						$scope.refreshPlugins();
					})
				}
				break;
			default:
				break;
		}
	};
}]);

app.controller('HistoryIndexController', ['$rootScope', '$scope', '$filter', '$state', 'crawlRecords', 
                                          function($rootScope, $scope, $filter, $state, crawlRecords){
	$rootScope.crawlRecords = crawlRecords;
	
	$scope.goToCrawl = function(status, id){
		if(status == 'success'){
			$state.go('crawl.pluginOutput', {crawlId: id, pluginId: 0});
		} else{
			$state.go('crawl.log', {crawlId: id});
		}
	};
}]);

app.controller('CrawlRecordController', ['$scope', '$rootScope', '$sce', 'historyHttp', 'socket', 'crawl', 
                                         function($scope, $rootScope, $sce, historyHttp, socket, crawl){
	$scope.crawl = crawl;
	$scope.log = '';
	
	$scope.$on('log-update', function(event, args){
		$scope.log = $sce.trustAsHtml(args.newLog);
		console.log('log-update');
		console.log(args.newLog);
		console.log($scope.log);
	});
	
	if ($scope.isLogging) {
		socket.sendMsg('stoplog');
	}
	setTimeout(function(){ 
		$('#logPanel').empty();
		socket.log = '';
		socket.sendMsg('startlog-' + $scope.crawl.id);
		$scope.isLogging = true;
	}, 0);
	
	$scope.$on('$destroy', function(){
		$scope.isLogging = false;
		socket.sendMsg('stoplog');
	});
	
	angular.element("#sideNav").scope().configId = crawl.configurationId;
}]);

app.controller('CrawlRecordPluginController', ['$scope', '$rootScope', '$sce', function($scope, $rootScope, $sce){
	$scope.trustedUrl = $sce.trustAsResourceUrl('/output/crawl-records/' + $rootScope.$stateParams.crawlId + '/plugins/' + $rootScope.$stateParams.pluginId)
}]);

app.controller('BreadcrumbController', ['$scope', function($scope){
	$scope.links = [];
}]);

app.controller('SideNavController', ['$scope', '$rootScope', 'restService', function($scope, $rootScope, restService){
	$scope.links = [];
	$scope.$on('restChanged', function(){
		$scope.rest = restService.rest;
	})
}]);

app.controller('CrawlQueueController', ['$scope', '$state', 'socket', function($scope, $state, socket){
	$scope.queue = angular.copy(socket.executionQueue);
	$scope.$on('queue-update', function(event, args){
		$scope.queue = args.newQueue;
		$scope.$apply();
	});
	
	$scope.goToCrawl = function(status, id){
		if(status == 'success'){
			$state.go('crawl.pluginOutput', {crawlId: id, pluginId: 0});
		} else{
			$state.go('crawl.log', {crawlId: id});
		}
	};
}]);