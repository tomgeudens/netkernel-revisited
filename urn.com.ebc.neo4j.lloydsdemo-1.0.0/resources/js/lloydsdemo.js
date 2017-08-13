"use strict";
// setup connection to Neo4j database
// todo : make this configurable
var neo4j = window.neo4j.v1;
var driver = neo4j.driver("bolt://localhost", neo4j.auth.basic("neo4j", "XXX"));
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
		
		cy.on("unselect", "node", function(evt){
			freeNode();
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
	$("#cy").height($(window).innerHeight() - 150);
	
	cy.resize();
	
	var layout = cy.elements().layout({
		name: "cose"
	});
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
				local_id: journeynode.identity.low,
				label: journeynode.labels[0]
		}

		Object.keys(journeynode.properties).forEach(key => {
			output[key] = journeynode.properties[key];
		});
		
		cy.add({
			group: "nodes",
			data: output
		});
		
		reDraw();
		
		return output;
	})
	.catch (error => {
		session.close();
	});
}

// show content of the node
function showNode(node) {
	$("#nodeview-thead").empty();
	$("#nodeview-thead").append("<tr><th>property</th><th>value</th></tr>");
	
	$("#nodeview-tbody").empty();
	Object.keys(node.data()).forEach(key => {
		$("#nodeview-tbody").append("<tr><td>" + key + "</td><td>" + node.data()[key] + "</td></tr>");
	});
	
	$("#expandnode").removeClass("invisible");
	$("#removenode").removeClass("invisible");
	if (node.outdegree() != 0) {
		$("#prunenode").removeClass("invisible");
	}

	$("#expandnode").on("click", node.data(), expandNode);
	$("#removenode").on("click", node.data(), removeNode);
	$("#prunenode").on("click", node.data(), pruneNode);
}

function freeNode() {
	$("#nodeview-thead").empty();
	$("#nodeview-tbody").empty();
	$("#expandnode").addClass("invisible");
	$("#removenode").addClass("invisible");
	$("#prunenode").addClass("invisible");
	$("#expandnode").off();
	$("#prunenode").off();
	$("#removenode").off();
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
	
	return session.run('MATCH p=(i)-[]-(o) WHERE id(i) = ' + event.data.local_id + ' RETURN p;')
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
				local_id: node.identity.low,
				label: node.labels[0]
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
				local_id: edge.identity.low,
				type: edge.type,
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
