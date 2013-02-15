var App = window.App;

//Wrapper for Input Form Fields
App.FormField = Ember.View.extend({
	tagName: 'div',
	classNames: ['control-group'],
	template: Ember.Handlebars.compile([
	    '{{view view.labelView viewName="labelView"}}',
	    '<div class="controls">',
	    '  {{view view.inputField viewName="inputField"}}',
	    '</div>'].join("\n")),
	label: null,
	labelView: Ember.View.extend({
		tagName: 'label',
		classNames: ['control-label'],
		template: Ember.Handlebars.compile('{{view.value}}'),
		valueBinding: 'parentView.label',
		inputElementId: '',
		forBinding: 'inputElementId',
		attributeBindings: ['for']
	}),
	inputField: null,
	didInsertElement: function() {
	    this.set('labelView.inputElementId', this.get('inputField.elementId'));
	}
});

//Text Field
App.FormTextField = App.FormField.extend({
	inputField: Ember.TextField.extend({
		valueBinding: 'parentView.value',
		placeholderBinding: 'parentView.placeholder',
		disabledBinding: 'parentView.disabled',
		maxlengthBinding: 'parentView.maxlength'
	})
})