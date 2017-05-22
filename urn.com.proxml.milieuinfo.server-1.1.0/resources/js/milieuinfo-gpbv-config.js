/* milieuinfo-gpbv-config.js */

(function($) {

	window['milieuinfo-gpbv-config'] = {
		// Lookup
		lookupExamples: '/txt/samples-lookup-milieuinfogpbv.txt',          // path or url
		// SPARQL Query
		sparqlExamples: '/unparsedtxt/samples-sparql-milieuinfogpbv.txt',  // path or url
		sparqlEndpoint: 'http://id.milieuinfo.be/gpbv/sparql',             // path or url
		// Keyword Search
		kwsExamples: '/txt/samples-keywordsearch-milieuinfogpbv.txt',      // path or url
		kwsEndpoint: 'http://id.milieuinfo.be/gpbv/keywordsearch'          // path or url
	};

})(jQuery);
