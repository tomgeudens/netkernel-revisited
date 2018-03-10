var neo4j = window.neo4j.v1;
var driver = neo4j.driver("bolt://localhost", neo4j.auth.basic("neo4j", "Tomc008123"));

(function () {
	var myConnector = tableau.makeConnector();
	
	myConnector.getSchema = function (schemaCallback) {
		var cols = [
			{id: "title", alias: "title", dataType: tableau.dataTypeEnum.string},
			{id: "actors", alias: "actors", dataType: tableau.dataTypeEnum.int}
		];
		var tableInfo = {
			id : "actorcountFeed",
			alias : "Number of actors per movie",
			columns : cols
		};
		
		schemaCallback([tableInfo]);
	};
	
	myConnector.getData = function (table, doneCallback) {
		var session = driver.session();

		return session.run('MATCH (m:Movie)-[:ACTED_IN]-(p:Person) RETURN m.title AS title, count(p) AS actors')
		.then(function(result) {
			session.close();
			
			var tableData = [];
			result.records.forEach(function (record) {
				tableData.push({
					"title": record.get("title"),
					"actors": record.get("actors").toInt()
				});				
			});
				
			table.appendRows(tableData);
			doneCallback();
			return {};
		})
		.catch(function(error) {
			tableau.log("error " + error);
			session.close();
			doneCallback();
		});			
	};
	
	tableau.registerConnector(myConnector);
	
	$(document).ready(function () {
		$("#submitButton").click(function () {
			tableau.log("In the custom code");
			tableau.connectionName = "Neo4j Data";
			tableau.submit();
		});
	});
})();
