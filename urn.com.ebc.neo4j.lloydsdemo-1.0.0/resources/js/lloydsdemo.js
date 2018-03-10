"use strict";
// setup connection to Neo4j database
// todo : make this configurable
var neo4j = window.neo4j.v1;
var driver = neo4j.driver("bolt://localhost", neo4j.auth.basic("neo4j", "Tomc008123"));
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
	});
});

// (re)Drawing the visualization layer
function reDraw() {
	$("#cy").height($(window).innerHeight() - 170);
	
	cy.resize();

	var layoutoptions = {
			name: 'cose-bilkent',

			// Called on `layoutready`
			ready: function () {
			},
			// Called on `layoutstop`
			stop: function () {
			},
			// number of ticks per frame; higher is faster but more jerky
			refresh: 30, 
			// Whether to fit the network view after when done
			fit: true,
			// Padding on fit
			padding: 10,
			// Whether to enable incremental mode
			randomize: true,
			// Node repulsion (non overlapping) multiplier
			nodeRepulsion: 4500,
			// Ideal (intra-graph) edge length
			idealEdgeLength: 150,
			// Divisor to compute edge forces
			edgeElasticity: 0.45,
			// Nesting factor (multiplier) to compute ideal edge length for inter-graph edges
			nestingFactor: 0.1,
			// Gravity force (constant)
			gravity: 0.25,
			// Maximum number of iterations to perform
			numIter: 2500,
			// Whether to tile disconnected nodes
			tile: true,
			// Type of layout animation. The option set is {'during', 'end', false}
			animate: 'end',
			// Amount of vertical space to put between degree zero nodes during tiling (can also be a function)
			tilingPaddingVertical: 10,
			// Amount of horizontal space to put between degree zero nodes during tiling (can also be a function)
			tilingPaddingHorizontal: 10,
			// Gravity range (constant) for compounds
			gravityRangeCompound: 1.5,
			// Gravity force (constant) for compounds
			gravityCompound: 1.0,
			// Gravity range (constant)
			gravityRange: 3.8,
			// Initial cooling factor for incremental layout
			initialEnergyOnIncremental:0.8
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
	
	$("#infopane").addClass("scrollClass");
}

//show content of the edge
function showEdge(edge) {
	$("#nodeview-thead").empty();
	$("#modalview").empty();
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
