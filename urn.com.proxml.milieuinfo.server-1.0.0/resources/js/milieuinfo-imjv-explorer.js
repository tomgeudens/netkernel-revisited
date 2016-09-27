/* milieuinfo-cbb-explorer.js */

(function($) {

	var lib = {

			initAccordion: function() {
				$('#explorer .api h4')
				.on('click', lib.toggleApi)
				.each(function() {
					// inject anchor
					var type = $(this).parents('.api').attr('data-api');
					$('<a name="' + type + '"></a>').insertBefore($(this));
					// expand
					if (!$(this).parents('.api').is('.active') && (location.hash === '#' + type)) {
						$(this).trigger('click');
					}
				})
				;
			},

			toggleApi: function() {
				var api = $(this).parents('.api');
				var wasActive = api.is('.active');
				// collapse prev api form
				$('#explorer .api.active .form-area').animate({'height': 0}, 250, function() {
					$(this).parents('.api').removeClass('active');
					$(this).css('height', 'auto');
				});
				// expand api form, if newly selected
				if (!wasActive) {
					var h = api.find('.form-area form').height();
					api.find('.form-area').height(0);
					api.addClass('active');
					api.find('.form-area').animate({'height': h}, function() {
						$(this).css('height', 'auto');
					});
				}
				// set state
				location.hash = api.attr('data-api');
			},

			initExamples: function() {
				lib.injectLookupExamples();
				lib.injectSparqlExamples();
				$('#explorer .api select.samples').on('change', function() {
					$(this).prev().val($(this).val().replace(/\&quot\;/, '-'));
					$(this).val('');
				});
			},

			injectLookupExamples: function() {
				var src = window['milieuinfo-imjv-config']['lookupExamples'];
				var container = $('#explorer .api[data-api="lookup"] select.samples');
				$.get(src, function(data) {
					var lines = data.split(/[\r\n]+/);
					$.each(lines, function(index, value) {
						if (!value.match(/^#/) && !value.match(/^\s*$/)) {
							var parts = value.split(/^([^\s]+)\s+(.+)$/);
							container.append('<option value="' + parts[1] + '">' + parts[2] + '</option>');
						}
					});
				});
			},

			injectSparqlExamples: function() {
				var src = window['milieuinfo-imjv-config']['sparqlExamples'];
				var container = $('#explorer .api[data-api="sparql"] select.samples');
				$.get(src, function(data) {
					var lines = data.replace(/\r\n/, "\n").replace(/\r/, "\n").split("\n");
					lines.push("##"); // make sure the buffer gets flushed at the end
					var inQuery = false;
					var title = '';
					var query = '';
					$.each(lines, function(index, value) {
						if (value.match(/^##/)) {
							// flush buffer
							if (inQuery && title && query) {
								container.append('<option value="' + query.replace(/\"/g, '&quot;') + '">' + title + '</option>');
							}
							// reset buffer
							inQuery = true;
							title = value.replace(/^[#\s]+(.+)\s*$/, '$1');
							query = '# ' + title + "\n";
						}
						else if (inQuery) {
							query += value + "\n";
						}
					});
				});
			},
			
			initForms: function() {
				$('#explorer .api form').on('submit', function(e) {
					e.preventDefault();
					var form = $(this);
					var api = $(this).parents('.api').attr('data-api');
					if (api === 'lookup') {
						lib.submitLookup($(this).find('[name="identifier"]').val());
					}
					else if (api === 'sparql') {
						lib.submitSparql($(this).find('[name="query"]').val());
					}
					form.find('.buttons')
					.find('.status').remove().end()
					.append('<span class="status">Even geduld ...</span>')
					;
					lib.pulsate.call(form.find('.status'));
				});
				$(window).on("beforeunload", function() {
					$('#explorer .api form .status').remove();
				});
			},

			pulsate: function() {
				$(this).delay(500).fadeOut(500).delay(250).fadeIn(1000, lib.pulsate);
			},
			
			submitLookup: function(identifier) {
				if (identifier != "") {
					location.href = identifier.replace(/\#.+$/, '') + '.html';
				}
				else {
					location.reload();
				}
			},

			submitSparql: function(query) {
				var endpoint = window['milieuinfo-imjv-config']['sparqlEndpoint'];
				var url = endpoint + '?query=' + encodeURIComponent(query);
				location.href = url;
			},

			init: function() {
				lib.initAccordion();
				lib.initExamples();
				lib.initForms();
			}

	};

	$(lib.init);  

})(jQuery);
