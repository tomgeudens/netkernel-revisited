/* milieuinfo-cbb-config.js */

(function($) {

	window['milieuinfo-cbb-config'] = {
		// Lookup
		lookupExamples: '/txt/samples-lookup-milieuinfocbb.txt',          // path or url
		// SPARQL Query
		sparqlExamples: '/txt/samples-sparql-milieuinfocbb.txt',          // path or url
		sparqlEndpoint: 'http://localhost:8100/cbb/sparql',               // path or url
		// Keyword Search
		kwsExamples: '/txt/samples-keywordsearch-milieuinfocbb.txt',      // path or url
		kwsEndpoint: 'http://localhost:8100/cbb/keywordsearch'            // path or url
	};

})(jQuery);
