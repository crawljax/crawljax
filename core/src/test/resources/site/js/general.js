$(document).ready(function(){
	checkUrl();
});

function checkUrl(){
	var url = location.href;
	if(url.indexOf("#") != -1){
		var id = url.substring(url.indexOf("#") + 1);
		if(id.substring(0,4) != "test"){
			loadForm(id);		
		}else{
			openPage(id + ".html");	
		}
	}else{
		loadHome();		
	}
}

function loadTest(element){
	openPage(element.innerHTML + ".html");
}

function openPage(url){
	$('#content').load(url);
}

function loadRandomAllowedPage(){
	$('#content').html("Random valid page " + (Math.random()*1000000000000000000));
}

function loadForbiddenPage(){
	$('#content').html("FORBIDDEN_PAGE");
}

function loadHome(){
	$('#content').load("home.html", {}, function(){
		   document.getElementById("randomStyle").style.borderLeft = (Math.random()*800) + "px solid red";
		   document.getElementById("randomDate").innerHTML = Math.floor(Math.random()*31) + "-" + Math.floor(Math.random()*12) + "-20" + (10 + Math.floor(Math.random()*89));
	});
}

function getFormValues(id){
	var ret = "";
	ret += document.getElementById("text" + id).value + ";";
	ret += document.getElementById("text2" + id).value + ";";
	ret += document.getElementById("checkbox" + id).checked + ";";
	ret += document.getElementById("radio" + id).checked + ";";
	ret += document.getElementById("select" + id).options[document.getElementById("select" + id).selectedIndex].innerHTML + ";";
	ret += document.getElementById("textarea" + id).value;
	return ret;
}

function getForm(id){
	var ret = "";
	ret +='<input type="text" id="text' + id + '"/>';
	ret +='<input id="text2' + id + '"/>';
	ret +='<input type="checkbox" id="checkbox' + id + '" />';
	ret +='<input type="radio" id="radio' + id + '" />';
	ret +='<select id="select' + id + '">';
	ret +='<option>OPTION1</option>';
	ret +='<option>OPTION2</option>';
	ret +='<option>OPTION3</option>';
	ret +='<option>OPTION4</option>';
	ret +='</select>';
	ret +='<textarea id="textarea' + id + '"></textarea>';
	ret +="<a href='javascript:void(0)' onclick='submitForm" + id + "(\"" + id + "\")'>Submit " + id + "</a>";
	return ret;
}

function loadForm(id){
	$('#content').html(getForm(id));
}

function submitFormRandom(id){
	$("#content").html("RESULT_RANDOM_INPUT" + " " + getFormValues(id) + "");
}

function submitFormManual(id){
	$("#content").html("RESULT_MANUAL_INPUT" + " " + getFormValues(id) + "");
}

function submitFormMultiple(id){
	$("#content").html("RESULT_MULTIPLE_INPUT" + " " + getFormValues(id) + "");
}

/**
 * require a link set in #content to the next specified depth
 * 
 * @param id the path id (A,B,C,D or E)
 * @param newDepth the new depth to go to
 * @return nothing the contents of #content is updated
 */
function setDepth(id, newDepth){
	var name = 'Depth' + id + newDepth;
	var arguments = '\'' + id + '\',' + (newDepth + 1);
	
	if(newDepth == 3 && id == 'A'){
		/* Limit the A path to max depth of 3*/
		arguments = '\'' + id + '\',' + newDepth;
	}else if(newDepth == 4){
		/* Other paths limit at a depth of 4 to prevent infinit recursion when depth is broken*/
		arguments = '\'' + id + '\',' + newDepth;
	}else if(newDepth == 2 && id == 'C'){
		/* CLONE path */
		name = 'DepthB2';
		arguments = '\'B\', 3'
	}else if(newDepth == 2 && id == 'E'){
		/* Branch path */
		name = 'DepthEA2';
		arguments = '\'E\', 3'
	}else if(newDepth == 2 && id == 'EB'){
		/* Limit the branch path part B*/
		name = 'DepthEB2';
		arguments = '\'EB\', 2'
	}
	if(newDepth == 2 && id == 'E'){
		/*We want to branch*/
		$("#content").html('<a href="javascript:void(0)" onclick="setDepth(' + arguments + ')">' + name + '</a><br/><a href="javascript:void(0)" onclick="setDepth(\'E\', 2)">DepthEB2</a>');
	}else if(newDepth == 2 && id == 'F'){
		/*Dom not changed link*/
		$("#content").html('<a href="javascript:void(0)">NOP</a><a href="javascript:void(0)" onclick="setDepth(' + arguments + ')">' + name + '</a>');
	}else{
		/*Single link*/
		$("#content").html('<a href="javascript:void(0)" onclick="setDepth(' + arguments + ')">' + name + '</a>');
	}
}
