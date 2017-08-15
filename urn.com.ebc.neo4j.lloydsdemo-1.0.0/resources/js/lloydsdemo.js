"use strict";
// setup connection to Neo4j database
// todo : make this configurable
var neo4j = window.neo4j.v1;
var driver = neo4j.driver("bolt://localhost", neo4j.auth.basic("neo4j", "neo4j4ever"));
var cy;

document.addEventListener("DOMContentLoaded", function() {
	
	// create visualization layer
	cy = cytoscape({
		container: document.getElementById("cy"),
		style: fetch("/lloydsdemo/json/lloydsdemo_style.json", {mode: "no-cors"})
		.then(function(res) {
			return res.json()
		})
	});
	
	getJourneyNames()
	.then(function(names) {
		names.forEach(function(name) {
			$('#journeyselect').append('<option value="' + name + '">' +  name + '</option>');
		});
	});
	
	// wait for visualization layer to become available
	cy.ready(function(event) {
		
		// if browser window is resized, redraw the visualization
		$(window).resize(function() {
			if(this.resizeTO) clearTimeout(this.resizeTO);
			this.resizeTO = setTimeout(function() {
				$(this).trigger("resizeEnd");
			}, 500);
		});
		$(window).bind("resizeEnd", function() {
			reDraw();
		});
		
		// catch the button
		$("#journeyform").submit(function(ev) {
			ev.preventDefault();
			visualizeJourney($("#journeyselect").val());
		});
		
		// catch a tap on a node
		cy.on("select", "node", function(evt){
			showNode(evt.target);
		});
		
		// catch a tap on an edge
		cy.on("select", "edge", function(evt){
			showEdge(evt.target);
		});
		
		cy.on("unselect", "node", function(evt){
			freeNode();
		});
		
		cy.on("unselect", "edge", function(evt){
			freeEdge();
		});
		
		// dummy data
		/*
		event.cy.add({
			group: "nodes",
			data: {
				id: "j1",
				label: "Journey",
				name: "journey 1"
			}
		});
		
		event.cy.add({
			group: "nodes",
			data: {
				id: "s1",
				label: "Screen",
				name: "screen 1"
			}
		});
		
		event.cy.add({
			group: "edges",
			data: {
				id: "e1",
				source: "j1",
				target: "s1",
				type: "HAS"
			}
		});
		*/	
	});
});

// (re)Drawing the visualization layer
function reDraw() {
	$("#cy").height($(window).innerHeight() - 170);
	
	cy.resize();

	var layoutoptions = {
			name: 'cose',

			// Called on `layoutready`
			ready: function(){},

			// Called on `layoutstop`
			stop: function(){},

			// Whether to animate while running the layout
			animate: true,

			// The layout animates only after this many milliseconds
			// (prevents flashing on fast runs)
			animationThreshold: 250,

			// Number of iterations between consecutive screen positions update
			// (0 -> only updated on the end)
			refresh: 20,

			// Whether to fit the network view after when done
			fit: true,

			// Padding on fit
			padding: 30,

			// Constrain layout bounds; { x1, y1, x2, y2 } or { x1, y1, w, h }
			boundingBox: undefined,

			// Excludes the label when calculating node bounding boxes for the layout algorithm
			nodeDimensionsIncludeLabels: true,

			// Randomize the initial positions of the nodes (true) or use existing positions (false)
			randomize: false,

			// Extra spacing between components in non-compound graphs
			componentSpacing: 100,

			// Node repulsion (non overlapping) multiplier
			nodeRepulsion: function( node ){ return 400000; },

			// Node repulsion (overlapping) multiplier
			nodeOverlap: 10,

			// Ideal edge (non nested) length
			idealEdgeLength: function( edge ){ return 10; },

			// Divisor to compute edge forces
			edgeElasticity: function( edge ){ return 100; },

			// Nesting factor (multiplier) to compute ideal edge length for nested edges
			nestingFactor: 5,

			// Gravity force (constant)
			gravity: 80,

			// Maximum number of iterations to perform
			numIter: 1000,

			// Initial temperature (maximum node displacement)
			initialTemp: 200,

			// Cooling factor (how the temperature is reduced between consecutive iterations
			coolingFactor: 0.95,

			// Lower temperature threshold (below this point the layout will end)
			minTemp: 1.0,

			// Pass a reference to weaver to use threads for calculations
			weaver: false
	};

	
	var layout = cy.elements().layout(layoutoptions);
	layout.run();	
}

// getting the Journey names for the select field
function getJourneyNames() {
	var session = driver.session();
	
	return session.run("MATCH (j:Journey) RETURN j.name AS name;")
	/*
fetch('/lloydsdemo/cypher/getjourneynames.cypher', {mode: 'no-cors'})
			.then(function(res) {
				return res.text();
			})
	 */
	.then(result => {
		session.close();
		
		var names = [];
		
		result.records.forEach(res => {
			names.push(res.get("name"));
		});
		
		return names;
	})
	.catch (error => {
		session.close();
	});
}

// getting the counts per journey
function getJourneyCounts(journeyname, jn) {
	var session = driver.session();
	
	return session.run('MATCH (j:Journey)-[:HAS_SCREEN]->(s:System)-[:CONTAINS]->(d:DataItem) WHERE j.name = "' + journeyname + '"WITH s.name AS sname, COUNT(DISTINCT d) AS dcount RETURN "DataDetails" as name, collect( { name: sname, count: dcount } ) as count ' + 
			'UNION MATCH (j:Journey)-[:HAS_SCREEN]->(s:System)-[:CONTAINS]->(d:DataItem) WHERE j.name = "' + journeyname + '"WITH s.name AS sname, COUNT(DISTINCT d) AS dcount RETURN "MoreDetails" as name, collect( { name: sname, count: dcount } ) as count ')
		.then(result => {
			session.close();
				
			result.records.forEach(res => {
				jn.data(res.get("name"), res.get("count"));
			});
				
			reDraw();
				
			return {};		
		})
		.catch(error => {
			console.log("error " + error);
			session.close();
		});	
}

// adding the Journey node to the visualization
function visualizeJourney(journeyname) {
	// clear everything first
	$("#nodeview-thead").empty();
	$("#nodeview-tbody").empty();
	cy.remove(cy.$("*"));

	var session = driver.session();
	
	return session.run('MATCH (j:Journey) WHERE j.name = "' + journeyname + '" RETURN j;')
	.then(result => {
		session.close();
		
		if (result.records.length == 0) {
			console.log("Journey " + journeyname + " was not found in the database.");
			return {};
		}

		var journeynode = result.records[0].get("j");
		
		var output = {
				id: "node_" + journeynode.identity.low,
				neo4j_id: journeynode.identity.low,
				neo4j_label: journeynode.labels.join()
		}

		Object.keys(journeynode.properties).forEach(key => {
			output[key] = journeynode.properties[key];
		});
		
		var jn = cy.add({
			group: "nodes",
			data: output
		});
		
		getJourneyCounts(journeyname, jn);
		
		return output;
	})
	.catch (error => {
		session.close();
	});
}

function addModal(key, data, id) {
	
	var html = "";
	
	html = html + "<div id='" + id + "' class='modal fade' role='dialog'>";
	html = html + "<div class='modal-dialog'>"
	html = html + "<div class='modal-content'>"
	
	html = html + "<div class='modal-header'>";
	html = html + "<button type='button' class='close' data-dismiss='modal'>&times;</button>"
	html = html + "<h4 class='modal-title'>Details - " + key + "</h4>"
	html = html + "</div>"
	
	html = html + "<div class='modal-body'>"
	for (var i = 0; i < data.length; i++) {
		html = html + "<p>" + data[i]["name"] + " : " + data[i]["count"] + "</p>"
	} 
	html = html + "</div>"
	
	html = html + "<div class='modal-footer'>"
	html = html + "<button type='button' class='btn btn-default' data-dismiss='modal'>Close</button>"
	html = html + "</div>";
	
	html = html + "</div>"
	html = html + "</div>"
	html = html + "</div>"
	
	$("#modalview").append(html);
}

// show content of the node
function showNode(node) {
	$("#nodeview-thead").empty();
	$("#modalview").empty();
	$("#nodeview-thead").append("<tr><th>property</th><th>value</th></tr>");
	
	$("#nodeview-tbody").empty();
	Object.keys(node.data()).forEach(key => {
		if (typeof node.data()[key] === "object") {
			$("#nodeview-tbody").append("<tr><td>" + key + "</td><td><button type='button' class='btn' data-toggle='modal' data-target='#modal" + key + "'>open detail</button></td></tr>");
			addModal(key, node.data()[key], "modal" + key);
		} else {
			$("#nodeview-tbody").append("<tr><td>" + key + "</td><td>" + node.data()[key] + "</td></tr>");
		}
	});
	
	$("#expandnode").removeClass("invisible");
	$("#removenode").removeClass("invisible");
	$("#centernode").removeClass("invisible");
	if (node.outdegree() != 0) {
		$("#prunenode").removeClass("invisible");
	}

	$("#expandnode").on("click", node.data(), expandNode);
	$("#removenode").on("click", node.data(), removeNode);
	$("#prunenode").on("click", node.data(), pruneNode);
	$("#centernode").on("click", node.data(), centerNode);
}

//show content of the edge
function showEdge(edge) {
	$("#edgeview-thead").empty();
	$("#edgeview-thead").append("<tr><th>property</th><th>value</th></tr>");
	
	$("#edgeview-tbody").empty();
	Object.keys(edge.data()).forEach(key => {
		$("#edgeview-tbody").append("<tr><td>" + key + "</td><td>" + edge.data()[key] + "</td></tr>");
	});
}

function freeEdge() {
	$("#edgeview-thead").empty();
	$("#edgeview-tbody").empty();	
}

function freeNode() {
	$("#modalview").empty();
	$("#nodeview-thead").empty();
	$("#nodeview-tbody").empty();
	$("#expandnode").addClass("invisible");
	$("#removenode").addClass("invisible");
	$("#prunenode").addClass("invisible");
	$("#centernode").addClass("invisible");
	$("#expandnode").off();
	$("#prunenode").off();
	$("#removenode").off();
	$("#centernode").off();
}


// center on the node
function centerNode(event) {
	cy.center(cy.$id(event.data.id));
	$("#centernode").blur();
}

// removing the node
function removeNode(event) {
	freeNode();
	cy.$id(event.data.id).remove();
}

// expanding the node
function expandNode(event) {
	$("#expandnode").addClass("invisible");

	var session = driver.session();
	
	return session.run('MATCH p=(i)-[]-(o) WHERE id(i) = ' + event.data.neo4j_id + ' RETURN p;')
	.then(result => {
		session.close();
		result.records.forEach(res => {
			checkandaddNode(res.get("p")["segments"][0]["start"]);
			checkandaddNode(res.get("p")["segments"][0]["end"]);
			checkandaddEdge(res.get("p")["segments"][0]["relationship"]);
		});
		reDraw();
		if (cy.$id(event.data.id).outdegree() != 0) {
			$("#prunenode").removeClass("invisible");
		}
	})
	.catch (error => {
		session.close();
	});
}

//pruning the node
function pruneNode(event) {
	$("#prunenode").addClass("invisible");
	cy.$id(event.data.id).outgoers().remove();
	reDraw();
	$("#expandnode").removeClass("invisible");
}

// verify if node exists, add it if it doesn't
function checkandaddNode(node) {
	var found = cy.$id("node_" + node.identity.low);
	if (found["length"] == 0) {
		var output = {
				id: "node_" + node.identity.low,
				neo4j_id: node.identity.low,
				neo4j_label: node.labels.join()
		}

		Object.keys(node.properties).forEach(key => {
			output[key] = node.properties[key];
		});
		
		cy.add({
			group: "nodes",
			data: output
		});
	}
	else {
		// console.log("node_" + node.identity.low + " already exists");
	}
}

// verify if edge exists, add it if it doesn't
function checkandaddEdge(edge) {
	var found = cy.$id("edge_" + edge.identity.low);
	if (found["length"] == 0) {
		var output = {
				id: "edge_" + edge.identity.low,
				neo4j_id: edge.identity.low,
				neo4j_type: edge.type,
				source: "node_" + edge.start.low,
				target: "node_" + edge.end.low
		}

		Object.keys(edge.properties).forEach(key => {
			output[key] = edge.properties[key];
		});
		
		cy.add({
			group: "edges",
			data: output
		});
	}
	else {
		// console.log("edge_" + edge.identity.low + " already exists");
	}
}
