
App.BreadcrumbView = Ember.CollectionView.extend({
	tagName: "ul",
 	classNames: "breadcrumb well",
 	createChildView: function(view, attrs) {
 		if (attrs.contentIndex === (this.content.length - 1)) {
 			view = this.lastItemViewClass || view;
 		} return this._super(view, attrs);
 	},
	itemViewClass: Ember.View.extend({
		template: Ember.Handlebars.compile('<a {{bindAttr href="view.content.target"}}>{{view.content.text}}</a><span class="divider">/</span>')
	}),
	lastItemViewClass: Ember.View.extend({
		classNames: "active",
		template: Ember.Handlebars.compile("{{view.content.text}}")
	}),
	contentBinding: "controller.breadcrumb"
});

App.SideNavView = Ember.CollectionView.extend({
	  	tagName: "ul",
	  	classNames: "nav nav-list",
	  	itemViewClass: Ember.View.extend({ 
	  		template: Ember.Handlebars.compile([
	  			'{{#if view.content.action}}',
	  			'	{{#if view.disabled}}',
	  			'		{{#if view.content.icon}}<i {{bindAttr class="view.content.icon" }}></i>{{/if}}',
		  		'		{{view.content.text}}',
	  			'	{{else}}',	
	  			'		<a href="#" {{action "rest" view.content}}>',
	  			'   	{{#if view.content.icon}}<i {{bindAttr class="view.content.icon" }}></i>{{/if}}',
	  			'		{{view.content.text}}</a>',
	  			'	{{/if}}',
	  			'{{else}}',
	  			'	<a {{bindAttr href="view.content.target"}}>',
	  			'   {{#if view.content.icon}}<i {{bindAttr class="view.content.icon" }}></i>{{/if}}',
	  			'	{{view.content.text}}</a>',
	  			'{{/if}}'].join("\n"))
	  	}),
	  	contentBinding: "controller.sidenav"
});

App.NavView = Ember.View.extend({
	tagName: 'li',
	classNameBindings: ['active'],
	route: null,
	routeParam: null,
	didInsertElement: function() {
		this.routeParam = this.get('content.' + this.routeParam);
		var _this = this;
		$(window).on('hashchange', function(){
			_this.notifyPropertyChange('active');
		});
		this.notifyPropertyChange('active');
	},
	active: function() {
		var path = this.get('controller.controllers.application.currentPath');
		var matchIndex = path.indexOf(this.route);
		var active = (matchIndex >= 0 && matchIndex == path.length - this.route.length); //path.endsWith(this.route)
		if(this.routeParam) {
			var components = location.hash.split('/');
			active = active && (components[components.length - 1] == this.routeParam); //currentHash.endsWith(this.routeParam)
		}
		return active;
	}.property()
	/*currentPathChange: function() {
		console.log('currentPathChange');
		this.notifyPropertyChange('active');
	}.observes('controller.controllers.application.currentPath')*/
});

App.FormField = Ember.View.extend({
	tagName: 'div',
	classNameBindings: ['isForm: control-group'],
	errorText: null,
	label: null,
	dynamicLabel: false,
	isForm: true,
	template: Ember.Handlebars.compile([
	    '{{#if view.label}}{{view view.labelView viewName="labelView"}}{{/if}}',
	    '<div {{bindAttr class="isForm: controls"}}>',
	    '  {{view view.inputField viewName="inputField"}}',
	    '  {{view view.errorView viewName="errorView"}}',
	    '</div>'].join("\n")),
	labelView: Ember.View.extend({
		tagName: 'label',
		classNames: ['control-label'],
		template: Ember.Handlebars.compile('{{view.value}}'),
		valueBinding: 'parentView.label',
		inputElementId: '',
		forBinding: 'inputElementId',
		attributeBindings: ['for']
	}),
	inputField: Ember.View.extend({
		tagName: 'span',
		classNames: ['uneditable-input'],
		template: Ember.Handlebars.compile('{{view.value}}'),
		valueBinding: 'parentView.value'
	}),
	errorView: Ember.View.extend({
		tagName: 'span',
		classNameBindings: ['errorText:hint'],
		template: Ember.Handlebars.compile('{{view.errorText}}'),
		errorTextBinding: 'parentView.errorText'
	}),
	didInsertElement: function() {
	    this.set('labelView.inputElementId', this.get('inputField.elementId'));
		if(this.get('dynamicLabel')) {
			this.set('label', this.get('label').replace("{{dynamicLabel}}", this.get('content.' + this.get('dynamicLabel'))));
		}
	}
});

//Text Field
App.FormTextField = App.FormField.extend({
	type: 'text',
	inputField: Ember.TextField.extend({
		valueBinding: 'parentView.value',
		placeholderBinding: 'parentView.placeholder',
		disabledBinding: 'parentView.disabled',
		requiredBinding: 'parentView.required',
		patternBinding: 'parentView.pattern',
		typeBinding: 'parentView.type',
		minBinding: 'parentView.min',
		maxlengthBinding: 'parentView.maxlength',
		classNameBindings: 'parentView.inputClassNames',
		attributeBindings: ['required', 'pattern', 'type', 'min', 'max']
	})
});

//Checkbox
App.FormCheckbox = App.FormField.extend({
	template: Ember.Handlebars.compile([
	    '<div class="controls">',
	    '<label class="checkbox">',
	    '  	{{view view.inputField viewName="inputField"}}',
	    '	{{view.label}}',
	    '</label>',
	    '</div>'].join("\n")),
	labelView: null,
	inputField: Ember.Checkbox.extend({
		checkedBinding:   'parentView.checked',
	    disabledBinding: 'parentView.disabled',
	    classNameBindings: 'parentView.inputClassNames'
	}),
	didInsertElement: function() {}
});

//Select
App.FormSelect = App.FormField.extend({
	optionLabelPath: 'content',
	optionValuePath: 'content',
	inputField: Ember.Select.extend({
		contentBinding: 'parentView.content',
		optionLabelPathBinding: 'parentView.optionLabelPath',
	    optionValuePathBinding: 'parentView.optionValuePath',
		selectionBinding: 'parentView.selection',
		valueBinding: 'parentView.value',
		disabledBinding: 'parentView.disabled',
		classNameBindings: 'parentView.inputClassNames',
		requiredBinding: 'parentView.required',
		attributeBindings: ['required']
	})
});

App.PluginSelect = App.FormField.extend({
	optionLabelPath: 'content',
	optionValuePath: 'content',
	onChange: null,
	change: function(evt) {
		if(this.get('onChange')) {
			this.get('controller.' + this.onChange).call(this.get('controller'), this.get('value'));
		}
	},
	inputField: Ember.Select.extend({
		contentBinding: 'parentView.content',
		optionLabelPathBinding: 'parentView.optionLabelPath',
	    optionValuePathBinding: 'parentView.optionValuePath',
		selectionBinding: 'parentView.selection',
		valueBinding: 'parentView.value',
		disabledBinding: 'parentView.disabled',
		classNameBindings: 'parentView.inputClassNames',
		requiredBinding: 'parentView.required',
		attributeBindings: ['required']
	}),
	didInsertElement: function() {
		if(this.get('onChange')) {
			this.get('controller.' + this.onChange).call(this.get('controller'), this.get('value'));
		}
	}
});


App.FileUploadView = Ember.TextField.extend({
	type: 'file',
	attributeBindings: ['name'],
	change: function(evt) {
		var _this = this;
		_this.get('controller').set(_this.get('name'), false);
		var input = evt.target;
		if (input.files && input.files[0]) {
			_this.get('controller').set(_this.get('name'), input.files[0]);
		}
	}
});