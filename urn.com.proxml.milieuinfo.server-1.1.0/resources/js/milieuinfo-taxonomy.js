/**
 * Library for replacing skos:broader/narrower with a taxonomy widget
 */

(function ($) {

    var lib = {

        /**
         * Starts the lib, called when the DOM is ready
         */
        init: function () {
            lib.base = $('base').attr('href') || '/';
            // check if this is a concept page
            lib.isConcept = lib.hasType('http://www.w3.org/2004/02/skos/core#Concept');
            lib.isScheme = lib.hasType('http://www.w3.org/2004/02/skos/core#ConceptScheme');
            if (!lib.isConcept && !lib.isScheme) {
                return;
            }
            lib.initEndpoint();
            lib.initTaxonomy();
            lib.initEvents();
        },

        /**
         * Checks if the current resource has the given type
         *
         * @param {String} type
         *
         * @return {Boolean}
         */
        hasType: function(type) {
            var predicate = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type';
            return !!$('.properties .predicate a[href$="' + predicate + '"] + .objects a[href="' + type + '"]').length;
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
         * Extracts embedded taxonomy information and renders a tree widget
         */
        initTaxonomy: function () {
            $('head').append($('<link rel="stylesheet" type="text/css" />').attr('href', lib.base + 'css/milieuinfo-taxonomy.css'));
            lib.resource = {
                uri: lib.buildUri(location.pathname + '#id'),
                label: document.querySelector('h1').innerText
            };
            lib.broaderConcept = lib.extractBroaderConcept();
            lib.narrowerConcepts = lib.extractNarrowerConcepts();
            lib.initWidget();
        },

        /**
         * Builds a full concept URI from a given path, href, or URI
         *
         * @param {String} path
         *
         * @return {String}
         */
        buildUri: function (path) {
            if (path.indexOf(lib.base) === 0) {
                path = path.substr(lib.base.length);
            }
            if (path.indexOf('http://id.milieuinfo.be/') === 0) {
                path = path.substr('http://id.milieuinfo.be/'.length);
            }
            path = path.replace(/^(https?:\/\/[^\/]+)?\//, '');
            //noinspection UnnecessaryLocalVariableJS
            var uri = 'http://id.milieuinfo.be/' + path;
            return uri;
        },

        /**
         * Extracts an embedded broader concept
         *
         * @return {Object|null}
         */
        extractBroaderConcept: function () {
            var concepts = lib.extractConcepts('http://www.w3.org/2004/02/skos/core#broader');
            return concepts.length ? concepts[0] : null;
        },

        /**
         * Extracts embedded narrower concepts
         *
         * @return {Array}
         */
        extractNarrowerConcepts: function () {
            return lib.extractConcepts('http://www.w3.org/2004/02/skos/core#narrower');
        },

        /**
         * Extracts embedded concept information
         *
         * @param {String} predicate
         *
         * @return {Array}
         */
        extractConcepts: function (predicate) {
            var result = [];
            $('.links.outbound .predicate a[href$="' + predicate + '"] + .objects a').each(function () {
                result.push({
                    uri: lib.buildUri(this.getAttribute('href')),
                    label: this.innerText
                });
            });
            return result;
        },

        /**
         * Removes broader/narrower/hasTopConcept sections and injects a tree widget
         */
        initWidget: function () {
            // add taxonomy widget as first item in outbound links
            lib.widget = document.createElement('div');
            lib.widget.classList.add('predicate');
            lib.injectWidget();
            // widget label
            var label = document.createElement('span');
            label.classList.add('label');
            label.innerHTML = 'Concept Hierarchy';
            lib.widget.appendChild(label);
            // contents
            var contents = document.createElement('div');
            contents.classList.add('objects');
            contents.classList.add('taxonomy');
            lib.widget.appendChild(contents);
            // tree
            var tree = document.createElement('ul');
            contents.appendChild(tree);
            // add self
            var self = lib.addEntry({
                uri: lib.resource.uri,
                label: lib.resource.label,
                parent: tree,
                class: 'self expanded'
            });
            if (lib.isConcept) {
                // add narrower
                lib.addNarrowerConcepts(lib.resource.uri, lib.narrowerConcepts);
            } else if (lib.isScheme) {
                lib.loadTopConcepts(lib.resource.uri, lib.addNarrowerConcepts);
            }
        },

        /**
         * Injects the tree widget
         */
        injectWidget: function () {
            var container = document.querySelector('.links.outbound');
            if (!container) {
                container = document.createElement('div');
                container.classList.add('links');
                container.classList.add('outbound');
                document.querySelector('#content').insertBefore(container, document.querySelector('.links-heading'));
            }
            if (container.firstElementChild) {
                container.insertBefore(lib.widget, container.firstElementChild);
            } else {
                container.append(lib.widget);
            }
        },

        /**
         * Adds a concept entry to the tree widget
         *
         * @param {Object} entry
         *
         * @return {Element}
         */
        addEntry: function (entry) {
            // element
            var element = document.createElement('li');
            element.classList.add('entry');
            // icon
            var icon = document.createElement('a');
            icon.setAttribute('href', entry.uri);
            icon.classList.add('icon');
            entry.class.split(/\s+/).forEach(function (className) {
                element.classList.add(className);
            });
            // link
            var link = document.createElement('a');
            link.innerHTML = entry.label;
            link.setAttribute('href', entry.uri.replace(/^https?:\/\/[^\/]+\//, lib.base));
            // label
            var label = document.createElement('span');
            label.classList.add('concept-label');
            label.appendChild(icon);
            label.appendChild(link);
            element.appendChild(label);
            // append element
            var subList;
            if (entry.parent) {
                if (!entry.parent.tagName.match(/^ul$/i)) {
                    subList = entry.parent.querySelector('ul');
                    if (!subList) {
                        subList = document.createElement('ul');
                        entry.parent.appendChild(subList);
                    }
                    entry.parent = subList;
                }
                entry.parent.appendChild(element);
            } else if (entry.child) {
                subList = document.createElement('ul');
                element.appendChild(subList);
                entry.child.parentNode.insertBefore(element, entry.child);
                subList.appendChild(entry.child);
            }
            return element;
        },

        /**
         * Activates sub-tree toggling with on-demand loading of narrower concepts
         */
        initEvents: function () {
            $(lib.widget).on('click', '.icon', function (event) {
                if (lib.endpoint) {
                    event.preventDefault();
                }
                var $element = $(this);
                var $entry = $element.closest('.entry');
                // animate
                if ($entry.is('.collapsed')) {
                    $entry.removeClass('collapsed').addClass('expanded');
                } else if ($entry.is('.expanded')) {
                    $entry.removeClass('expanded').addClass('collapsed');
                }
                // load
                if ($entry.is('.expanded') && !$entry.find('ul').length) {
                    var uri = this.getAttribute('href');
                    lib.loadNarrowerConcepts(uri, lib.addNarrowerConcepts);
                }
            });
        },

        /**
         * Loads narrower concepts of the given concept URI
         *
         * @param {String} uri
         * @param {Function} callback
         */
        loadNarrowerConcepts: function (uri, callback) {
            var skos = 'http://www.w3.org/2004/02/skos/core#';
            var query = "".concat(
                'PREFIX skos: <' + skos + '> ',
                'SELECT ?concept ?label WHERE {',
                '   <' + uri + '> skos:narrower ?concept .',
                '   ?concept skos:prefLabel ?label .',
                '}'
            );
            $.ajax({
                url: lib.endpoint,
                dataType: "json",
                cache: true,
                method: 'POST',
                data: {
                    query: query
                },
                success: function (response) {
                    //noinspection JSUnresolvedVariable
                    var bindings = response.results.bindings;
                    var concepts = bindings.map(function (binding) {
                        //noinspection JSUnresolvedVariable
                        return {
                            uri: binding.concept.value,
                            label: binding.label.value
                        }
                    });
                    //noinspection JSUnresolvedFunction
                    callback.call(lib, uri, concepts);
                }
            });
        },

        /**
         * Adds narrower concept entries to the tree widget
         *
         * @param {String} uri
         * @param {Array} concepts
         */
        addNarrowerConcepts: function (uri, concepts) {
            var entry = lib.widget.querySelector('.entry a[href="' + uri + '"]').parentNode.parentNode;
            concepts.forEach(function (concept) {
                lib.addEntry({
                    uri: concept.uri,
                    label: concept.label,
                    parent: entry,
                    class: 'narrower collapsed'
                });
            });
            if (!concepts.length) {
                entry.classList.remove('expanded');
                entry.classList.remove('collapsed');
                entry.classList.add('final');
            }
        },

        /**
         * Loads the broader concept of the given concept URI
         *
         * @param {String} uri
         * @param {Function} callback
         */
        loadBroaderConcept: function (uri, callback) {
            var skos = 'http://www.w3.org/2004/02/skos/core#';
            var query = "".concat(
                'PREFIX skos: <' + skos + '> ',
                'SELECT ?concept ?label WHERE {',
                '   <' + uri + '> skos:broader ?concept .',
                '   ?concept skos:prefLabel ?label .',
                '}'
            );
            $.ajax({
                url: lib.endpoint,
                dataType: "json",
                method: 'POST',
                cache: true,
                data: {
                    query: query
                },
                success: function (response) {
                    //noinspection JSUnresolvedVariable
                    var bindings = response.results.bindings;
                    var concepts = bindings.map(function (binding) {
                        //noinspection JSUnresolvedVariable
                        return {
                            uri: binding.concept.value,
                            label: binding.label.value
                        }
                    });
                    //noinspection JSUnresolvedFunction
                    callback.call(lib, uri, concepts.length ? concepts[0] : null);
                }
            });
        },

        /**
         * Adds a broader concept entry to the tree widget
         *
         * @param {String} uri
         * @param {Object} concept
         */
        addBroaderConcept: function (uri, concept) {
            if (concept) {
                var entry = lib.widget.querySelector('.entry a[href="' + uri + '"]').parentNode.parentNode;
                lib.addEntry({
                    uri: concept.uri,
                    label: concept.label,
                    child: entry,
                    class: 'broader expanded'
                });
                lib.loadBroaderConcept(concept.uri, lib.addBroaderConcept);
            }
        },

        /**
         * Loads top concepts of the given scheme URI
         *
         * @param {String} uri
         * @param {Function} callback
         */
        loadTopConcepts: function (uri, callback) {
            var skos = 'http://www.w3.org/2004/02/skos/core#';
            var query = "".concat(
                'PREFIX skos: <' + skos + '> ',
                'SELECT ?concept ?label WHERE {',
                '   ?concept skos:topConceptOf <' + uri + '> ; ',
                '            skos:prefLabel ?label .',
                '}'
            );
            $.ajax({
                url: lib.endpoint,
                dataType: "json",
                cache: true,
                method: 'POST',
                data: {
                    query: query
                },
                success: function (response) {
                    //noinspection JSUnresolvedVariable
                    var bindings = response.results.bindings;
                    var concepts = bindings.map(function (binding) {
                        //noinspection JSUnresolvedVariable
                        return {
                            uri: binding.concept.value,
                            label: binding.label.value
                        }
                    });
                    //noinspection JSUnresolvedFunction
                    callback.call(lib, uri, concepts);
                }
            });
        }
    };

    $(lib.init);

})(jQuery);
