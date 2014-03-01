//*******************************************
// validation.js
//
// Form Validation Code using HTML 5 validation
//********************************************

function validateForm(formId)
{
	var isValid = document.getElementById(formId).checkValidity();
	if (!isValid) $('input:invalid').first().focus();
	
	return isValid;
}