/* milieuinfo-cbb-config.js */

(function($) {

	window['milieuinfo-cbb-config'] = {
		// Lookup
		lookupExamples: '/txt/samples-lookup-milieuinfocbb.txt',          // path or url
		// SPARQL Query
		sparqlExamples: '/unparsedtxt/samples-sparql-milieuinfocbb.txt',  // path or url
		sparqlEndpoint: 'https://id.milieuinfo.be/cbb/sparql',             // path or url
		// Keyword Search
		kwsExamples: '/txt/samples-keywordsearch-milieuinfocbb.txt',      // path or url
		kwsEndpoint: 'https://id.milieuinfo.be/cbb/keywordsearch'          // path or url
	};

})(jQuery);