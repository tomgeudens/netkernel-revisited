/**
 * Library for interactive inbound links
 */

(function ($) {

    var lib = {

        /**
         * Starts the lib, called when the DOM is ready
         */
        init: function () {
            lib.base = $('base').attr('href') || '/';
            lib.resourceUri = document.querySelector('body').getAttribute('about');
            lib.pageSize = 8;
            lib.timeouts = {};
            lib.initEndpoint();
            lib.initWidgets();
            lib.initEvents();
        },

        /**
         * Debounces callbacks
         *
         * @param {Function} callback
         * @param {Number=1} delay Debounce-delay
         * @param {String=} callbackId Identifier for the callback function (optional)
         */
        debounce: function (callback, delay, callbackId) {
            var self = this;
            delay = delay || 1;
            //noinspection JSUnresolvedFunction
            callbackId = callbackId || callback.toString();

            if (this.timeouts[callbackId]) {
                clearTimeout(this.timeouts[callbackId]);
            }
            this.timeouts[callbackId] = setTimeout(function () {
                callback.call(self);
            }, delay);
        },

        /**
         * Adds separators to thousands
         *
         * @param {Number|String} value
         *
         * @return {String} Formatted number, e.g. "123 456"
         */
        formatNumber: function (value) {
            return value.toString().replace(/(.)(?=(\d{3})+$)/g,'$1 ');
        },

        /**
         * Initializes the SPARQL endpoint from the config, if available
         */
        initEndpoint: function () {
            lib.endpoint = null;
            if (window['milieuinfo-imjv-config'] && location.href.match(/\/imjv\//)) {
                lib.endpoint = window['milieuinfo-imjv-config'].sparqlEndpoint;
            } else if (window['milieuinfo-cbb-config'] && location.href.match(/\/cbb\//)) {
                lib.endpoint = window['milieuinfo-cbb-config'].sparqlEndpoint;
            }
        },

        /**
         * Enhances all sections with inbound links
         */
        initWidgets: function () {
            $('head').append($('<link rel="stylesheet" type="text/css" />').attr('href', lib.base + 'css/milieuinfo-inbound-links.css'));
            $('.links.inbound .predicate').each(function () {
                lib.initWidget(this);
            })
        },

        /**
         * Enhances the given section with inbound links
         *
         * @param {Element} widget
         */
        initWidget: function (widget) {
            var labelElement = widget.querySelector('.label');
            var predicate = labelElement.getAttribute('about');
            widget.setAttribute('data-predicate', predicate);
            // move label to right side
            widget.appendChild(labelElement);
            // epilogue
            var epilogue = document.createElement('span');
            epilogue.classList.add('epilogue');
            epilogue.innerHTML = document.querySelector('h1').innerHTML;
            labelElement.appendChild(epilogue);
            // retrieve link count
            if (lib.endpoint) {
                lib.countLinks(widget, lib.refreshList);
            }
        },

        /**
         * Adds a "controls" section with pagination and filter options to the given widget
         *
         * @param {Element} widget
         */
        initControls: function (widget) {
            var objects = widget.querySelector('.objects');
            // controls
            var controls = document.createElement('div');
            controls.classList.add('controls');
            widget.appendChild(controls);
            // filter
            var filter = document.createElement('input');
            filter.classList.add('filter');
            filter.setAttribute('type', 'text');
            filter.setAttribute('autocomplete', 'off');
            filter.setAttribute('data-value', '');
            filter.setAttribute('placeholder', 'Linken filteren');
            controls.appendChild(filter);
            // info
            var info = document.createElement('span');
            info.classList.add('info');
            controls.appendChild(info);
            // prev
            var prev = document.createElement('a');
            prev.classList.add('prev');
            controls.appendChild(prev);
            // next
            var next = document.createElement('a');
            next.classList.add('next');
            controls.appendChild(next);
        },

        /**
         * Renders link stats
         *
         * @param widget
         */
        updatePageInfo: function (widget) {
            var page = parseInt(widget.getAttribute('data-page'));
            var maxPage = parseInt(widget.getAttribute('data-max-page'));
            var linkCount = parseInt(widget.getAttribute('data-link-count'));
            var pageInfo = 'Pagina ' + Math.min(page + 1, maxPage + 1) + '/' + (maxPage + 1);
            var countInfo = lib.formatNumber(linkCount) + ' ' + (linkCount === 1 ? 'link' : 'linken');
            var info = widget.querySelector('.controls .info');
            info.innerHTML = pageInfo + '<span class="link-count"> (' + countInfo + ')</span>';
        },

        /**
         * Counts the inbound links for a given predicate
         *
         * @param {Element} widget
         * @param {Function} callback
         */
        countLinks: function (widget, callback) {
            // flag widget
            widget.classList.add('loading');
            $.ajax({
                url: lib.endpoint,
                dataType: "json",
                cache: true,
                method: 'POST',
                data: {
                    query: lib.getQuery(widget, true)
                },
                success: function (response) {
                    // de-flag widget
                    widget.classList.remove('loading');
                    //noinspection JSUnresolvedVariable
                    var bindings = response.results.bindings;
                    //noinspection JSUnresolvedVariable
                    var result = parseInt(bindings[0].linkCount.value);
                    // set link count
                    widget.setAttribute('data-link-count', result.toString());
                    // calculate max page
                    var maxOffset = result - 1;
                    var maxPage = Math.floor((maxOffset + lib.pageSize)/lib.pageSize) - 1;
                    widget.setAttribute('data-max-page', maxPage.toString());
                    //noinspection JSUnresolvedFunction
                    callback.call(lib, widget);
                }
            });
        },

        /**
         * Loads inbound links for the given widget
         *
         * @param {Element} widget
         * @param {Function} callback
         */
        loadLinks: function (widget, callback) {
            // flag widget
            widget.classList.add('loading');
            // run query
            $.ajax({
                url: lib.endpoint,
                dataType: "json",
                cache: true,
                method: 'POST',
                data: {
                    query: lib.getQuery(widget)
                },
                success: function (response) {
                    // de-flag widget
                    widget.classList.remove('loading');
                    //noinspection JSUnresolvedVariable
                    var bindings = response.results.bindings;
                    var result = bindings.map(function (binding) {
                        return {
                            uri: binding.uri.value,
                            label: binding.label.value
                        };
                    });
                    //noinspection JSUnresolvedFunction
                    callback.call(lib, widget, result);
                }
            });
        },

        /**
         * Builds a query snippet for count and retrieval operations
         *
         * @param {Element} widget
         * @param {Boolean=false} countOnly
         *
         * @return {String}
         */
        getQuery: function (widget, countOnly) {
            var predicate = widget.getAttribute('data-predicate');
            var page = parseInt(widget.getAttribute('data-page'));
            var rdfsLabel = 'http://www.w3.org/2000/01/rdf-schema#label';
            var skosLabel = 'http://www.w3.org/2004/02/skos/core#prefLabel';
            var dctTitle = 'http://purl.org/dc/terms/title';
            return "" +
                (countOnly
                    ? 'SELECT (COUNT(DISTINCT ?uri) AS ?linkCount) '
                    : 'SELECT DISTINCT ?uri ?label '
                ) +
                'WHERE { ' +
                '  ?uri <' + predicate + '> <' + lib.resourceUri + '> ; ' +
                '       ?p ?label . ' +
                '  FILTER(?p = <' + rdfsLabel + '> || ?p = <' + skosLabel + '> || ?p = <' + dctTitle + '>) ' +
                lib.getFilterPattern(widget) +
                '} ' +
                (countOnly
                    ? ''
                    : 'LIMIT ' + lib.pageSize + '  OFFSET ' + (page * lib.pageSize)
                )
            ;
        },

        /**
         * Builds a SPARQL FILTER string in case of defined keywords
         *
         * @param {Element} widget
         *
         * @return {String}
         */
        getFilterPattern: function (widget) {
            var result = '';
            var filter = widget.querySelector('input.filter');
            if (!filter) {
                return result;
            }
            filter.value.split(/\s/).forEach(function (keyword) {
                if (!keyword.match(/^\s*$/)) {
                    result += ' FILTER (regex(?label, "' + keyword + '", "i"))';
                }
            });
            return result;
        },

        /**
         * Reloads the link list (after counts are available)
         *
         * @param {Element} widget
         */
        refreshList: function(widget) {
            var linkCount = parseInt(widget.getAttribute('data-link-count'));
            // keep initial list if no pagination is needed
            if (!widget.querySelector('.controls') && linkCount <= lib.pageSize) {
                return;
            }
            // init controls
            if (!widget.querySelector('.controls')) {
                lib.initControls(widget);
            }
            // active pagination controls
            widget.classList.add('with-controls');
            // reset page offset
            widget.setAttribute('data-page', '0');
            lib.updatePageInfo(widget);
            // load first page
            lib.loadLinks(widget, lib.renderLinks);
        },

        /**
         * Renders a page of inbound links
         *
         * @param {Element} widget
         * @param {Array} links
         */
        renderLinks: function (widget, links) {
            var objects = widget.querySelector('.objects');
            objects.style.height = objects.offsetHeight;
            objects.innerHTML = '';
            var contentHeight = 0;
            links.forEach(function (link) {
                var p = document.createElement('p');
                var a = document.createElement('a');
                a.setAttribute('href', link.uri.replace(/^https?:\/\/[^\/]+\//, lib.base));
                a.innerHTML = link.label;
                p.appendChild(a);
                objects.appendChild(p);
                contentHeight += p.offsetHeight + 3;// 2 px padding, 1px border
            });
            // delay height rule for CSS transition to kick in
            setTimeout(function() {
                objects.style.height = contentHeight;
            }, 50);
            var page = parseInt(widget.getAttribute('data-page'));
            var maxPage = parseInt(widget.getAttribute('data-max-page'));
            if (page < maxPage) {
                widget.classList.remove('last-page');
            } else {
                widget.classList.add('last-page');
            }
        },

        /**
         * Activates the widget controls
         */
        initEvents: function () {
            var $widget, page, maxPage;
            // prev
            // noinspection JSJQueryEfficiency
            $('.links.inbound').on('click', 'a.prev', function (event) {
                event.preventDefault();
                $widget = $(this).closest('.predicate');
                page = parseInt($widget.attr('data-page'));
                if (page > 0) {
                    $widget.attr('data-page', page - 1);
                    lib.updatePageInfo($widget[0]);
                    lib.debounce(function() {
                        lib.loadLinks($widget[0], lib.renderLinks);
                    }, 500, 'load-links-for-' + $widget.attr('data-predicate'));
                }
            });
            // next
            // noinspection JSJQueryEfficiency
            $('.links.inbound').on('click', 'a.next', function (event) {
                event.preventDefault();
                $widget = $(this).closest('.predicate');
                page = parseInt($widget.attr('data-page'));
                maxPage = parseInt($widget.attr('data-max-page'));
                if (page < maxPage) {
                    $widget.attr('data-page', page + 1);
                    lib.updatePageInfo($widget[0]);
                    lib.debounce(function() {
                        lib.loadLinks($widget[0], lib.renderLinks);
                    }, 500, 'load-links-for-' + $widget.attr('data-predicate'));
                }
            });
            // filter
            // noinspection JSJQueryEfficiency
            $('.links.inbound').on('keyup change', 'input.filter', function () {
                var prevValue = this.getAttribute('data-value');
                var currentValue = this.value;
                if (prevValue !== currentValue) {
                    this.setAttribute('data-value', currentValue);
                    $widget = $(this).closest('.predicate');
                    lib.debounce(function() {
                        lib.countLinks($widget[0], lib.refreshList);
                    }, 250, 'count-links-for-' + $widget.attr('data-predicate'));
                }
            });
        }
    };

    $(lib.init);

})(jQuery);
