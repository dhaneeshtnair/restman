var projectName = getCurrentProject();
var userName = "";
var server = getServer();
var targetHosts = null;

function logOff() {
	localStorage.removeItem("userInfo");
	location.reload();
}

function renderProjects(callBack) {
	var command = server + "/herest/projects/project?userName=" + userName;
	$.getJSON(command, function(data) {
		var hits =data&& data.hits? data.hits.hits:[];
		for ( var k in hits) {
			var name = ((hits[k])["_source"]).name;
			$("#dropdown2").append(
					'<li class="divider"></li><li><a href="#">' + name
							+ '</a></li>');
		}

		callBack();
	});
}

function checkLogin() {

	var user = {
		email : "test@test.com"
	};//getCurrentUser();

	if (!user || !user.email) {

		$("#loginModal").openModal({
			dismissible : false,
			ready : function() {
				Materialize.updateTextFields();
			}
		});

	} else {
		userName = user.email;

		$("#userDisplayName").html(user.displayName);
		$("#userEmail").html(user.email);
	}

	$("#login_b").click(function() {

		var user = {
			userName : $("#userName").val(),
			password : $("#password").val()
		};
		var command = server + "/auth/login";

		execCommand(command, "POST", JSON.stringify(user), function(out) {
			if (!out.email) {
				alert("Failed to Authenticate");
			} else {
				setCurrentUser(out);
				checkLogin();
				$("#loginModal").closeModal();
			}
		});

	});
}

function addHost() {
	var command = server + "/herest/" + projectName + "/hosts/"
			+ $("#nameInput").val() + "?userName=" + userName;

	var host = {
		name : $("#nameInput").val(),

		host : $("#hostInput").val()
	}

	execCommand(command, "POST", JSON.stringify(host), function(out, status) {
		if (status === 200 || status === 201) {

			setTimeout(function() {
				renderHosts();
			}, 2000);
		}
	});

}

function addProject() {

	var command = server + "/herest/projects/project/"
			+ $("#nprojectName").val().toLowerCase() + "?userName=" + userName;

	var project = {

		name : $("#nprojectName").val(),

		description : $("#npdescription").val()
	};

	execCommand(command, "POST", JSON.stringify(project),
			function(out, status) {
				if (status === 200 || status === 201) {

					alert("Project created Successfully : " + project.name)

					$("#openProjectModal").closeModal();
				}
			});

}

function renderHosts() {
	$("#hostTable tbody").empty();

	var command = server + "/herest/" + projectName + "/hosts?userName="
			+ userName;

	$.getJSON(command, function(data) {
		var recs = data.hits ? data.hits.hits : new Array();
		targetHosts = new Array();
		for ( var k in recs) {

			var o = recs[k];

			o = o["_source"];

			targetHosts.push(o);

			$("#hostTable tbody").append(
					"<tr><td>" + o.name + "</td><td>" + o.host + "</td></tr>");

		}
		populateServers();
	});
}

function populateServers() {
	$('#sselect').empty();
	$('#sselect').append(
			'<option value="" disabled selected>Choose your option</option>');
	for ( var k in targetHosts) {
		var op = '<option value="' + targetHosts[k].name + '">'
				+ targetHosts[k].name + '(' + targetHosts[k].host
				+ ')</option>';

		$('#sselect').append(op);
	}

	$('#sselect').material_select();
}

function setServer(){
    server=getSplittedUrl($("#serverURL").val());
    localStorage.setItem("serverURL",server);


    initProjectTree();
    $("#openSettingsModal").closeModal();


}

function getSplittedUrl(url){
    var to = url.lastIndexOf('/');
    if(to==url.length-1){
        to = to == -1 ? url.length : to;
        url = url.substring(0, to);
    }
    return url;
}

function getServer(){
    var serverURL = localStorage.getItem("serverURL");
    if(!serverURL)
        serverURL = getRootUrl();

    return serverURL;
}

function getRootUrl() {
  var defaultPorts = {"http:":80,"https:":443};

  return window.location.protocol + "//" + window.location.hostname
   + (((window.location.port)
    && (window.location.port != defaultPorts[window.location.protocol]))
    ? (":"+window.location.port) : "");
}

$(document)
		.ready(

				function() {


                    $("#serverURL").val(server);

					$("#addHost").click(function() {

						addHost();
					});

					renderProjects(function() {

						$("#dropdown2 a").click(function() {

							$("#projectName").html($(this).html());

							projectName = $("#projectName").html();

							setCurrentProject(projectName);

							initProjectTree();

							renderHosts();

						});
					});

					$('.modal-execute').leanModal({
						dismissible : true, // Modal can be dismissed by clicking outside of the modal
						opacity : .5, // Opacity of modal background
						in_duration : 300, // Transition in duration
						out_duration : 200, // Transition out duration
						starting_top : '4%', // Starting top style attribute
						ending_top : '10%', // Ending top style attribute
						ready : function() {

						},
						complete : function() {

						}
					});

					$('.modal-trigger').leanModal(
							{
								dismissible : true, // Modal can be dismissed by clicking outside of the modal
								opacity : .5, // Opacity of modal background
								in_duration : 300, // Transition in duration
								out_duration : 200, // Transition out duration
								starting_top : '4%', // Starting top style attribute
								ending_top : '10%', // Ending top style attribute
								ready : function() {

									$(".modal-content iframe").remove();

									// $(".modal-content .testcase").empty();

									// $(".modal-content .testcase").append('<div name="childDoc" id="childDoc" style="width:100%;height:100%;border: 1px solid #ddd;"></div>');

									$("#projectNameText").val(projectName);

									initProjectTree();

									$("#testcaseJson").val(
											JSON.stringify(getTestCaseJson(),
													null, 2));

									//alert(JSON.stringify(fetchMetaInfo()));
								}, // Callback for Modal open
								complete : function() {

								} // Callback for Modal close
							});

					//$('select').material_select();

					/* $(".abc").select2({
						tags : true,
						tokenSeparators : [ ',', ' ' ]
					}) */
					//projectName = $("#projectName").html();
					if (projectName && projectName != "")
						initProjectTree();
					$("#dropdown1 a").click(function() {

						if ($(this).html() == "GET") {

							disableReqBody();
						} else {

							enableReqBody();
						}

						$("#request").html($(this).html());
					});

					$("#send")
							.click(
									function() {
										$("#queryTable tbody").empty();
										sendData(function() {
											if (cObject) {
												var asserts = cObject._source.steps[0].assertions;

												for ( var k in asserts) {

													assert(asserts[k].expect,
															true, true);
												}
												$("#openAssert").click();
											}
											isRunning = false;
										});
									});

					if ($("#dropdown1 a").html() == "GET") {
						disableReqBody();
					}

					$("#executeTest")
							.click(
									function() {
										var tags = $('#execTags')
												.material_chip('data');
										var mt = "";
										var app = "";
										for (var i = 0; i < tags.length; i++) {
											mt += app + tags[i].tag;
											app = ","
										}
										var command = server
												+ "/herest/execute?userName="
												+ userName + "&projectName="
												+ projectName + "&tags=" + mt;
										$("#console")
												.html(
														"Running .......................................");
										execCommand(
												command,
												"POST",
												"",
												function(out) {
													//$("#console").html(JSON.stringify(out,null,3));
													if (out.status == "SUCCESS") {
														var $toastContent = $('<span><a class="btn-floating btn-large waves-effect waves-light green"><i class="material-icons">done</i></a>Success</span>');
														Materialize.toast(
																$toastContent,
																5000);
														$("#execURL")
																.html(
																		"Result:"
																				+ out.executionId);
														$("#execURL")
																.attr(
																		"href",
																		"/teststatus.html?executionId="
																				+ out.executionId);

													} else {
														var $toastContent = $('<span><a class="btn-floating btn-large waves-effect waves-light red"><i class="material-icons">shuffle</i></a>Failed</span>');
														Materialize.toast(
																$toastContent,
																5000);
													}
												});
									});
					$("#execute")
							.click(
									function() {
										var command = server + "/rtest/execute";
										$("#console")
												.html(
														"Running .......................................");
										execCommand(
												command,
												"POST",
												$("#testcaseJson").val(),
												function(out) {
													$("#console").html(
															JSON.stringify(out,
																	null, 3));
													if (out.status == "SUCCESS") {
														var $toastContent = $('<span><a class="btn-floating btn-large waves-effect waves-light green"><i class="material-icons">done</i></a>Success</span>');
														Materialize.toast(
																$toastContent,
																5000);
													} else {
														var $toastContent = $('<span><a class="btn-floating btn-large waves-effect waves-light red"><i class="material-icons">shuffle</i></a>Failed</span>');
														Materialize.toast(
																$toastContent,
																5000);
													}
												});
									});

					$("#saveTest")
							.click(
									function() {
										var command = server + "/herest/"
												+ projectName
												+ "/testCase?userName="
												+ userName;
										$("#console")
												.html(
														"Running .......................................");
										execCommand(
												command,
												"POST",
												$("#testcaseJson").val(),
												function(out, status) {
													$("#console").html(
															JSON.stringify(out,
																	null, 3));
													if (status == 200
															|| status == 201) {
														var $toastContent = $('<span><a class="btn-floating btn-large waves-effect waves-light green"><i class="material-icons">done</i></a>Success</span>');
														Materialize.toast(
																$toastContent,
																5000);
													} else {
														var $toastContent = $('<span><a class="btn-floating btn-large waves-effect waves-light red"><i class="material-icons">shuffle</i></a>Failed</span>');
														Materialize.toast(
																$toastContent,
																5000);
													}
												});
									});

					$('#tags').material_chip({
						data : [ {
							tag : 'smoke',
						}, {
							tag : 'read',
						}, {
							tag : 'write',
						} ],
					});

					$("#inform input").change(
							function() {

								$("#testcaseJson").val(
										JSON.stringify(getTestCaseJson(), null,
												2));
							});
					$("#inform textarea").change(
							function() {

								$("#testcaseJson").val(
										JSON.stringify(getTestCaseJson(), null,
												2));
							});

					$('#tags').on(
							'chip.add',
							function(e, chip) {
								$("#testcaseJson").val(
										JSON.stringify(getTestCaseJson(), null,
												2));
							});

					$('#tags').on(
							'chip.delete',
							function(e, chip) {
								$("#testcaseJson").val(
										JSON.stringify(getTestCaseJson(), null,
												2));
							});

					checkLogin();

					renderHosts();

				});
$("#addAssertion").click(function() {
	var cv = $("#queryInput").val();
	assert(cv, true);
});
$("#addHeader").click(function() {
	var key = $("#headerName").val();
	var val=$("#headerValue").val();
	if(key!="" && val!=""){
		addHeader(key,val);
		$("#headerName").val("");$("#headerValue").val("");
	}else
		alert("HeaderName or HeaderValue is empty");
		
});
function addHeaders(){
	
	
}
function addHeader(key,val){
	
	$("#headers tbody")
			.append(
					"<tr>"

					+ "<td>"+key+"</td>"
					+ "<td>"+val+"</td>"
							+ "</td>"
							+ " <td onclick=\"$(this).parent().remove();\"><i  id=\"removeHeader\" class=\"btn-floating waves-effect waves-light material-icons center\">delete</i></td></tr>");

	
}



$("#queryInput").change(function() {
	var cv = $("#queryInput").val();
	assert(cv);
});
var start_time = 0;

function assert(cv, addNow, ignoreFail,noStatus) {

	try {
		var prefix = ""
		if (cv.indexOf("responseCode") == -1) {
			//prefix="currentOut.";
		}
		var out = eval(prefix + cv);

		$("#queryOutput").html(
				(out == null) ? "undefined" : JSON.stringify(out));

		if ((out == true && addNow) || ignoreFail) {

			var strO = out ? "check" : "error";
			var color = out ? "green" : "red";

			var statusButton = "<td ><i  class=\"btn-floating waves-effect waves-light "
					+ color + " material-icons center\">" + strO + "</i></td>";

			if(noStatus){
			    statusButton = "<td></td>";
			}

			$("#queryTable tbody")
					.append(
							"<tr><td class='assert'>"
									+ cv
									+ "</td>"
									+ statusButton
									+ " <td onclick=\"$(this).parent().remove();\"><i  id=\"removeAssertion\" class=\"btn-floating waves-effect waves-light material-icons center\">delete</i></td></tr>");
		} else if (addNow) {

			$("#queryOutput").html("Evaluates to a non boolean value");
		}
	} catch (e) {
		$("#queryOutput").html(JSON.stringify(e.message));
	}
}

function sendData(callBack) {
	if (isRunning) {

		return "";
	}
	isRunning = true;
	send($("#url").val(), $("#request").html(), $("#requestBody").val(),
			function(out) {

				$("#responseBody").val(JSON.stringify(out, null, "\t"));
				if (callBack) {
					callBack();
				}
				isRunning = false;
			});
}
function send(url, method, data, callBack) {
	$
			.ajax({
				url : url,
				type : method,
				data : data,
				contentType : "application/json",
				beforeSend : function(request, settings) {
					$("#progress").css("display", "block");
					$("#timeTaken span").html("------");
					$("#responseCode span").html("-----");
					$("#responseIcon").html('');

					start_time = new Date().getTime();

				},
				success : function(data, textStatus, jqXHR) {
					var request_time = new Date().getTime() - start_time;
					$("#responseCode span").html(jqXHR.status);
					$("#timeTaken span").html(request_time);

					if (jqXHR.status >= 200 && jqXHR.status <= 300) {
						$("#responseIcon")
								.html(
										'<i class="btn-floating waves-effect waves-light green material-icons center">done</i>');

					} else {
						$("#responseIcon")
								.html(
										'<i class="btn-floating waves-effect waves-light red material-icons center">error</i>');

					}
					responseCode = jqXHR.status;

					response = data;
					callBack(data);
					$("#progress").css("display", "none");
				},
				error : function(jqXHR, textStatus, errorThrown) {
					if (jqXHR.status == 404 || errorThrown == 'Not Found') {
						console.log('There was a 404 error.');
					}
					var request_time = new Date().getTime() - start_time;
					$("#timeTaken span").html(request_time);
					responseCode = (jqXHR.status == 0) ? 0 : jqXHR.status;
					$("#responseCode span").html(responseCode);
					//alert(jqXHR.getResponseHeader("Access-Control-Allow-Headers")	);
					$("#responseIcon")
							.html(
									'<i class="btn-floating waves-effect waves-light red material-icons center">error</i>');
					if (callBack) {
						if (jqXHR.responseText) {
							var ret = jqXHR.responseText;
							try {
								ret = JSON.parse(jqXHR.responseText)
							} catch (e) {

							}
							callBack(ret);
						} else {
							callBack(textStatus);
						}
					}
					$("#progress").css("display", "none");

				},
				statusCode : {
					404 : function(response) {
						console.log('Invalid Transaction details');
					},
					200 : function(response) {
						//response processing code here
					}
				},
			});
}

function execCommand(url, method, data, callBack) {
	$.ajax({
		url : url,
		type : method,
		data : data,
		contentType : "application/json",
		beforeSend : function(request, settings) {

			start_time = new Date().getTime();

		},
		success : function(data, textStatus, jqXHR) {
			var request_time = new Date().getTime() - start_time;
			callBack(data, jqXHR.status);
		},
		error : function(jqXHR, textStatus, errorThrown) {
			if (jqXHR.status == 404 || errorThrown == 'Not Found') {
				console.log('There was a 404 error.');
			}
			var request_time = new Date().getTime() - start_time;

			callBack(errorThrown, jqXHR.status);

		}
	});
}
//var currentOut = null;
var response = null;
var responseCode = 0;

function htmlDecode(value) {
	return $('<div/>').html(value).text();
}

function getTestCaseJson() {

	var url = $("#url").val();
	var urlS = url.split("?");
	var tokens = urlS[0].split("/");
	var layerName = tokens[3];
	var method = $("#request").html();
	var assertions = new Array();
	$("#queryTable tbody tr td.assert").each(function(i, td) {
		var objAssert = (cObject) && cObject._source.steps[0].assertions[i] ? cObject._source.steps[0].assertions[i] : new Object();
		var ms = Babel.transform(htmlDecode($(td).html()), {
                  presets: ['es2015', 'react', 'stage-0']
                }).code;
		objAssert.expect = ms.substr(ms.indexOf(";")+1).trim();//htmlDecode(ms);
		if(!objAssert.name)
		    objAssert.name = "";
		objAssert.message = "Expected: ,Actual : {{" + ms + "}}";
		assertions.push(objAssert);
	});
	var headers = new Array();
	$("#headers tbody tr").each(function(i, tr) {
		var objAssert = new Object();
		objAssert.name = htmlDecode($(tr).find('td:first').html());
		objAssert.value = htmlDecode($(tr).find('td:nth-child(2)').html());
		headers.push(objAssert);
	});
	
	var tags = $('#tags').material_chip('data');
	var mt = new Array();
	for (var i = 0; i < tags.length; i++) {
		mt.push(tags[i].tag);
	}
	var inj = {};//new Object();
	try {
		inj = JSON.parse($("#requestBody").val());
	} catch (e) {
		//inj = $("#requestBody").val();

	}
	var group = $("#testcaseGroup").val();
	if (!group || group == "") {
		group = "General";
	}
	return {
		name : $("#testcaseName").val(),
		group : group,
		description : $("#description").val(),
		id : $("#testcaseId").val(),
		tags : mt,
		steps : [ {
			stepIndex : 1,
			name : $("#testcaseName").val(),
			url : url,
			id : $("#testcaseId").val(),
			description : $("#description").val(),

			input : inj,
			headers:headers,
			method : $("#request").html(),
			assertions : assertions
		} ]
	}

}
function fetchMetaInfo() {
	var url = $("#url").val();
	var urlS = url.split("?");
	var tokens = urlS[0].split("/");
	var layerName = tokens[3];
	var method = $("#request").html();
	var operation = "unknown";
	var operationType = "unknown";

	var boundingbox = {};
	var userName = "";

	var params = urlS[1].split("&");
	for ( var k in params) {
		var kv = k.split("=");
		if (kv[0] == "userName") {
			userName = kv[1];
		}
	}

	var id = "";
	if (urlS.length > 1 && method == "GET") {
		if (urlS[1].indexOf("minx") != -1) {
			operation = "read_boundingbox";
			var tok = urlS[1].split("&");
			for ( var k in tok) {

				var kv = tok[k].split("=");
				if (kv[0] == "minx") {
					boundingbox.minx = kv[1];
				}
				if (kv[0] == "maxx") {
					boundingbox.maxx = kv[1];
				}
				if (kv[0] == "miny") {
					boundingbox.miny = kv[1];
				}
				if (kv[0] == "maxy") {
					boundingbox.maxy = kv[1];
				}
			}

		} else if (tokens.length == 5) {
			operation = "single_read";
			id = tokens[4];
		}
		operationType = "Read";
	} else if (urlS.length > 1 && method == "PUT") {
		operationType = "Write";
		if (tokens.length == 5) {
			if (tokens[4] == "batch") {
				operation = "batch_update";
			} else {
				operation = "single_update";
				id = tokens[4];
			}
		}

	} else if (urlS.length > 1 && method == "POST") {
		operationType = "Write";
		if (tokens.length == 5) {

			if (tokens[4] == "batch") {
				operation = "batch_create";
			} else {
				operation = "unknown";
			}
		} else {

			operation = "single_create";
		}

	} else if (urlS.length > 1 && method == "DELETE") {
		operationType = "Write";
		if (tokens.length == 5) {

			if (tokens[4] == "batch") {
				operation = "batch_delete";
			} else {
				operation = "single_delete";
				id = tokens[4];
			}
		}

	}
	var assertions = [];

	$("#queryTable tbody tr td.assert").each(function(i, td) {
		assertions.push($(td).html());

	});
	var asserts = "";

	for ( var a in assertions) {

		asserts += "assert " + assertions[a] + "\n	";
	}
	var context = {
		url : $("#url").val(),
		layerName : layerName,
		serverName : tokens[2],
		operation : operation,
		operationType : operationType,
		method : method,
		boundingbox : boundingbox,
		id : id,
		asserts : asserts,
		userName : userName
	};
	return createCMSTestCase(context);
}

function createCMSTestCase(context) {
	var template = $('#' + context.operation).html();
	Mustache.parse(template); // optional, speeds up future uses
	var rendered = Mustache.render(template, context);
	context.payload = rendered;
	//alert(rendered);
	template = $('#testCaseTemplate').html();
	Mustache.parse(template); // optional, speeds up future uses
	rendered = Mustache.render(template, context);
	return rendered;

}

function disableReqBody() {

	$("#requestBody").css("background", "#aaa");
	$("#requestBody").attr("disabled", true);
	$("#requestBody").val("");

}
function enableReqBody() {

	$("#requestBody").css("background", "#123456");
	$("#requestBody").attr("disabled", false);

}

function initProjectTree() {

	$(".search-input").keyup(function() {
		var searchString = $(this).val();
		console.log(searchString);
		$('#jstree').jstree('search', searchString);
	});

	var url = server+"/herest/" + projectName
			+ "/testCase/_search?size=5000";

	$.getJSON(url, function(data) {
		var recs = data.hits ? data.hits.hits : new Array();

		var children = new Array();
		var groupsMap = [];
		for ( var k in recs) {
			var rec = recs[k]._source;
			var group = rec.group;
			if (!group || group == "") {
				group = "General"
			}
			var gp = groupsMap[group];
			if (!gp) {
				gp = new Array();
				groupsMap[group] = gp;
			}
			gp.push({
				"id" : group+":"+rec.id,
				"text" : rec.name,
				"icon" : "assets/images/eye.png",
				"state" : {
					"opened" : true,
					"disabled" : false,
					"selected" : false
				},
				"children" : false,
				"liAttributes" : null,
				"aAttributes" : null
			});
		}
		for ( var k in groupsMap) {

			children.push({
				"id" : k,
				"text" : k,
				"icon" : "",
				"state" : {
					"opened" : true,
					"disabled" : false,
					"selected" : false
				},
				"children" : groupsMap[k],
				"liAttributes" : null,
				"aAttributes" : null
			});
		}

		renderJSTree(children);

		initTreeEvents();

	}).fail(function() {
	       renderJSTree([]);
           console.log( "error" );
     })

}

function renderJSTree(children){

    $('#jstree').jstree("destroy")
    		$('#jstree').jstree({
    			'core' : {

    				'data' : [ {
    					"id" : "1.0",
    					"text" : "All",
    					"icon" : "",
    					"state" : {
    						"opened" : true,
    						"disabled" : false,
    						"selected" : false
    					},
    					"children" : children,
    					"liAttributes" : null,
    					"aAttributes" : null
    				} ]

    			},
    			"search" : {

    				"case_insensitive" : true,
    				"show_only_matches" : true,
    				"fuzzy" : false

    			},

    			"plugins" : [ "search" ]

    		});

}
var treeInited = false;

var isRunning = false;
var cObject = null;
function initTreeEvents() {

	treeInited = true;

	$('#jstree')
			.on(
					"select_node.jstree",
					function(e, data) {

						if (data.node.children.length > 0) {
							console.log("not leaf");
							$("#executeModal").openModal({
								ready : function() {

									$('#execTags').material_chip({
										data : [ {
											tag : 'all',
										} ],
									});

									$("#pname").val(projectName);

									$("#uname").val(userName);

									$("#execURL").html("");

									Materialize.updateTextFields();

								}
							});
							return;
						} else {
							console.log("leaf");

							$("#responseBody").val("");

							if ($("#respCard .card-reveal").css("display") == "block") {

								$("#respCard .card-reveal").css("display",
										"none");
							}

						}

						var ur = server+"/herest/" + projectName
								+ "/testCase/" + data.node.id.split(":")[1];

						$
								.getJSON(
										ur,
										function(object) {

											//alert(JSON.stringify(object));

											$("#queryTable tbody").empty();

											$("#headers tbody").empty();

											object._source.steps[0].assertions.forEach(a=>assert(a.expect,false,true,true));

											object._source.steps[0].headers.forEach(a=>addHeader(a.name,a.value));

											$("#url")
													.val(
															object._source.steps[0].url);

											$("#request")
													.html(
															object._source.steps[0].method);

											if ($("#request").html() != "GET") {

												$("#requestBody")
														.val(
																JSON
																		.stringify(
																				object._source.steps[0].input,
																				null,
																				2));

												enableReqBody();

											} else {
												$("#requestBody").val("");

												disableReqBody();
											}

											$("#testcaseId").val(
													object._source.id);

											$("#testcaseName").val(
													object._source.name);

											$("#projectNameText").val(
													projectName);

											$("#description").val(
													object._source.description);

											$("#testcaseGroup").val(object._source.group);

											Materialize.updateTextFields();

											var tags = object._source.tags;
											var tgA = new Array();
											for ( var x in tags) {
												tgA.push({
													tag : tags[x]
												});

											}

											$('#tags').remove();

											$('#tagc')
													.append(
															'<div class="chips chips-initial" id="tags"></div>');

											$('#tags').material_chip({
												data : tgA
											});

											$('#tags')
													.on(
															'chip.add',
															function(e, chip) {
																$(
																		"#testcaseJson")
																		.val(
																				JSON
																						.stringify(
																								getTestCaseJson(),
																								null,
																								2));
															});

											$('#tags')
													.on(
															'chip.delete',
															function(e, chip) {
																$(
																		"#testcaseJson")
																		.val(
																				JSON
																						.stringify(
																								getTestCaseJson(),
																								null,
																								2));
															});

											cObject = object;

										});

					});
}

function getCurrentProject() {
	var project = localStorage.getItem("projectName", "Select Project");
	document.getElementById("projectName").innerHTML = project;
	//alert(project);
	return project;
}
function setCurrentProject(name) {

	localStorage.setItem("projectName", name);
}

function getCurrentUser() {
	var project = localStorage.getItem("userInfo", null);
	if (project) {
		try {
			project = JSON.parse(project);
		} catch (e) {
			console.log(e);
			project = null;
		}
	}
	return project;
}
function setCurrentUser(user) {

	localStorage.setItem("userInfo", JSON.stringify(user));
}
