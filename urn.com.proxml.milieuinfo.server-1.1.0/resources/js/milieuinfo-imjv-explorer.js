/* milieuinfo-cbb-explorer.js */

(function ($) {

    var lib = {

        initAccordion: function () {
            $('#explorer').find('.api h4')
                .on('click', lib.toggleApi)
                .each(function () {
                    // inject anchor
                    var type = $(this).parents('.api').attr('data-api');
                    $(this).before('<a name="' + type + '"></a>');
                    // expand
                    if (!$(this).parents('.api').is('.active') && (location.hash === '#' + type)) {
                        $(this).trigger('click');
                    }
                })
            ;
        },

        toggleApi: function () {
            var api = $(this).parents('.api');
            var wasActive = api.is('.active');
            // collapse prev api form
            $('#explorer .api.active .form-area').animate({'height': 0}, 250, function () {
                $(this).parents('.api').removeClass('active');
                $(this).css('height', 'auto');
            });
            // expand api form, if newly selected
            if (!wasActive) {
                var h = api.find('.form-area form').height();
                api.find('.form-area').height(0);
                api.addClass('active');
                api.find('.form-area').animate({'height': h}, function () {
                    $(this).css('height', 'auto');
                    var apiType = api.attr('data-api');
                    if (apiType === 'sparql') {
                        lib.initYasgui();
                    }
                });
            }
            // set state
            location.hash = api.attr('data-api');
        },

        initExamples: function () {
            lib.injectLookupExamples();
            lib.injectSparqlExamples();
            lib.injectKWSExamples();
            $('#explorer .api select.samples').on('change', function () {
                var select = $(this);
                var example = select.val().replace(/\&quot\;/g, '-');
                var input = $(this).prev();
                if (input.is('.yasqe')) {
                    lib.yasqe.setValue(example);
                } else {
                    input.val(example);
                }
                select.val('');
            });
        },

        injectLookupExamples: function () {
            var src = window['milieuinfo-imjv-config']['lookupExamples'];
            var url = lib.base + src.replace(/^\//, '');
            var container = $('#explorer .api[data-api="lookup"] select.samples');
            $.get(url, function (data) {
                var lines = data.split(/[\r\n]+/);
                $.each(lines, function (index, value) {
                    if (!value.match(/^#/) && !value.match(/^\s*$/)) {
                        var parts = value.split(/^([^\s]+)\s+(.+)$/);
                        container.append('<option value="' + parts[1] + '">' + parts[2] + '</option>');
                    }
                });
            });
        },

        injectSparqlExamples: function () {
            var src = window['milieuinfo-imjv-config']['sparqlExamples'];
            var url = lib.base + src.replace(/^\//, '');
            var container = $('#explorer .api[data-api="sparql"] select.samples');
            $.get(url, function (data) {
                var lines = data.replace(/\r\n/, "\n").replace(/\r/, "\n").split("\n");
                lines.push("##"); // make sure the buffer gets flushed at the end
                var inQuery = false;
                var title = '';
                var query = '';
                $.each(lines, function (index, value) {
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

        injectKWSExamples: function () {
            var src = window['milieuinfo-imjv-config']['kwsExamples'];
            var url = lib.base + src.replace(/^\//, '');
            var container = $('#explorer .api[data-api="kws"] select.samples');
            $.get(url, function (data) {
                var lines = data.split(/[\r\n]+/);
                $.each(lines, function (index, value) {
                    if (!value.match(/^#/) && !value.match(/^\s*$/)) {
                        container.append('<option value="' + value + '">' + value + '</option>');
                    }
                });
            });
        },

        loadYasgui: function () {
            $('head').append($('<link rel="stylesheet" type="text/css" />').attr('href', lib.base + 'css/yasqe.min.css'));
            var scripts = ["js/yasgui/codemirror.js", "js/yasgui/yasqe.min.js"];
            var loadScript = function (index) {
                $.ajax({
                    url: lib.base + scripts[index],
                    dataType: "script",
                    cache: true,
                    success: function () {
                        if (index < scripts.length - 1) {
                            loadScript(index + 1)
                        }
                    }
                });
            };
            loadScript(0);
        },

        initYasgui: function () {
            if (lib.yasqe) {
                return; // already initialized
            }
            //noinspection JSUnresolvedVariable
            if ((typeof YASQE === 'undefined'))  {// not loaded yet, try again later
                return setTimeout(lib.initYasgui, 250);
            }
            // replace textarea
            var element = document.querySelector('#explorer .api[data-api="sparql"] textarea');
            var config = {
                sparql: {
                    showQueryButton: false
                }
            };
            //noinspection JSUnresolvedVariable
            lib.yasqe = YASQE.fromTextArea(element, config);
        },

        initForms: function () {
            $('#explorer').find('.api form').on('submit', function (e) {
                e.preventDefault();
                var form = $(this);
                var api = $(this).parents('.api').attr('data-api');
                if (api === 'lookup') {
                    lib.submitLookup($(this).find('[name="identifier"]').val());
                }
                else if (api === 'sparql') {
                    var query = lib.yasqe ? lib.yasqe.getValue() : $(this).find('[name="query"]').val();
                    lib.submitSparql(query);
                }
                else if (api === 'kws') {
                    lib.submitKWS($(this).find('[name="keyword"]').val());
                }
                form.find('.buttons')
                    .find('.status').remove().end()
                    .append('<span class="status">Even geduld ...</span>')
                ;
                lib.pulsate.call(form.find('.status'));
            });
            $('#explorer').find('.api form').on('reset', function () {
                var form = $(this);
                var api = $(this).parents('.api').attr('data-api');
                if (api === 'sparql' && lib.yasqe) {
                    lib.yasqe.setValue('');
                }
            });
            $(window).on("beforeunload", function () {
                $('#explorer').find('.api form .status').remove();
            });
        },

        pulsate: function () {
            $(this).delay(500).fadeOut(500).delay(250).fadeIn(1000, lib.pulsate);
        },

        submitLookup: function (identifier) {
            if (identifier != "") {
                location.href = identifier;
            }
            else {
                location.reload();
            }
        },

        submitSparql: function (query) {
            var endpoint = window['milieuinfo-imjv-config']['sparqlEndpoint'];
            location.href = endpoint + '?query=' + encodeURIComponent(query);
        },

        submitKWS: function (keyword) {
            var endpoint = window['milieuinfo-imjv-config']['kwsEndpoint'];
            location.href = endpoint + '?search=' + encodeURIComponent(keyword);
        },

        init: function () {
            lib.base = $('base').attr('href') || '/';
            lib.initAccordion();
            lib.initExamples();
            lib.loadYasgui();
            lib.initForms();
        }
    };

    $(lib.init);

})(jQuery);
