Promise.all([
	fetch('/cytodemo/json/cytodemo.json', {mode: 'no-cors'})
		.then(function(res) {
			return res.json()
		}),
	fetch('/cytodemo/json/cytodemo_style.json', {mode: 'no-cors'})
		.then(function(res) {
			return res.json()
		})	
])
.then (function(dataArray) {
	var cy = window.cy = cytoscape({
		
		container: document.getElementById('cy'),
		elements: dataArray[0],
		layout: {
			"name": "cose"
		},
		style: dataArray[1]
	});
});
