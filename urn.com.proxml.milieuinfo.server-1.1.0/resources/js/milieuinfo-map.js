/**
 * Library for injecting an OpenLayers map widget into a resource page when geo data is available
 */

(function ($) {

    var lib = {

        /** Extent of Flanders */
        mapExtent:[9928.000000, 66928.000000, 272072.000000, 329072.000000],

        /**
         * Starts the lib, called when the DOM is ready
         */
        init: function () {
            lib.base =  $('base').attr('href') || '/';
            lib.rawCoordinates = lib.getRawCoordinates();
            if (!lib.hasCoordinates()) {
                return;
            }
            lib.loadDependencies(lib.renderMap);
        },

        /**
         * Checks if geo data is available (either as lambert72 or wgs84)
         *
         * @return {boolean}
         */
        hasCoordinates: function () {
            var coords = lib.rawCoordinates;
            return ((coords.x && coords.y) || (coords.lat && coords.lon)) ? true : false;
        },

        /**
         * Extracts geo coordinates from the resource page
         *
         * @return {Object} Coordinates with x/y and/or lat/lon keys
         */
        getRawCoordinates: function () {
            var rawCoords = {};
            var mapPredicates = {
                'y': 'def#lambert72_y',
                'x': 'def#lambert72_x',
                'lat': 'http://www.w3.org/2003/01/geo/wgs84_pos#lat',
                'lon': 'http://www.w3.org/2003/01/geo/wgs84_pos#long'
            };
            for (var name in mapPredicates) {
                if (!mapPredicates.hasOwnProperty(name)) {
                    continue;
                }
                var predicate = mapPredicates[name];
                var objectValue = $('.predicate a[href$="' + predicate + '"] + .objects > *:first-child').text().replace(/\s/g, '');
                if (objectValue.length) {
                    rawCoords[name] = parseFloat(objectValue);
                }
            }
            return rawCoords;
        },

        getLambertCoords: function () {
            var rawCoords = lib.rawCoordinates;
            // EPSG:31370 (LAMBERT 72) data available
            if (rawCoords['x'] && rawCoords['y']) {
                return [rawCoords['x'], rawCoords['y']];
            }
            // EPSG:4326 (WGS 84) data available
            if (rawCoords['lon'] && rawCoords['lat']) {
                return ol.proj.transform([rawCoords['lon'], rawCoords['lat']], 'EPSG:4326', 'EPSG:31370');
            }
            // fallback, return center
            return [140860.69299028325, 190532.7165957574];
        },

        /**
         * Loads all assets (CSS and scripts), then runs the given callback
         *
         * @param {function} callback
         */
        loadDependencies: function (callback) {
            $('head').append($('<link rel="stylesheet" type="text/css" />').attr('href', lib.base + 'css/ol3.css'));
            $('head').append($('<link rel="stylesheet" type="text/css" />').attr('href', lib.base + 'css/milieuinfo-map.css'));
            var scripts = ["js/ol3/ol.js", "js/proj4/proj4.js"];
            var scriptsLoaded = 0;
            scripts.forEach(function (url) {
                $.ajax({
                    url: lib.base + url,
                    dataType: "script",
                    cache: true,
                    success: function () {
                        scriptsLoaded++;
                        if (scriptsLoaded === scripts.length) {
                            callback.call(lib);
                        }
                    }
                });
            }, lib);
        },

        /**
         * Injects a map widget
         */
        renderMap: function () {
            lib.initMapContainer();
            lib.initProjection();
            lib.initResolutions();
            lib.initMatrixIds();
            lib.initMap();
            lib.initTilesLayer();
            lib.triggerResize();
            lib.initMarkerLayer();
            lib.addMarker();
        },

        initMapContainer: function () {
            this.$map = $('<div id="map"></div>').insertBefore($('#content > .properties').first());
        },

        initProjection: function () {
            var code = 'EPSG:31370';
            //noinspection JSCheckFunctionSignatures
            if (!ol.proj.get(code)) {
                var definition = '+proj=lcc +lat_1=51.16666723333333 +lat_2=49.8333339 +lat_0=90 +lon_0=4.367486666666666 +x_0=150000.013 +y_0=5400088.438 +ellps=intl +towgs84=106.869,-52.2978,103.724,-0.33657,0.456955,-1.84218,1 +units=m +no_defs';
                proj4.defs(code, definition);
            }
            //noinspection JSCheckFunctionSignatures
            this.projection = ol.proj.get(code);
        },

        initResolutions: function () {
            var resolutions = [];
            //noinspection JSCheckFunctionSignatures
            var maxResolution = ol.extent.getHeight(this.mapExtent) / 256;
            for (var i = 0; i < 12; i++) {
                resolutions[i] = maxResolution / Math.pow(2, i);
            }
            this.resolutions = resolutions;
        },

        initMatrixIds: function () {
            var ids = [];
            for (var i = 0; i < 12; i++) {
                ids[i] = i.toString();
            }
            this.matrixIds = ids;
        },

        initMap: function () {
            //noinspection JSCheckFunctionSignatures
            this.olMap = new ol.Map({
                target: this.$map[0],
                loadTilesWhileAnimating: false,
                interactions: ol.interaction.defaults({mouseWheelZoom: false}),
                controls: [
                    new ol.control.ScaleLine(),
                    new ol.control.Zoom(),
                    new ol.control.ZoomSlider()
                ],
                view: new ol.View({
                    center: [140860.69299028325, 190532.7165957574],
                    extent: this.mapExtent,
                    projection: this.projection,
                    resolutions: this.resolutions,
                    zoom: 2
                })
            });
        },

        initTilesLayer: function () {
            //noinspection JSCheckFunctionSignatures
            this.tilesLayer = new ol.layer.Tile({
                name: 'tiles',
                source: new ol.source.WMTS({
                    url: 'https://tile.informatievlaanderen.be/ws/raadpleegdiensten/wmts',
                    layer: 'grb_bsk_grijs',
                    matrixSet: 'BPL72VL',
                    format: 'image/png',
                    projection: 'EPSG:31370',
                    tileGrid: new ol.tilegrid.WMTS({
                        extent: this.mapExtent,
                        resolutions: this.resolutions,
                        matrixIds: this.matrixIds
                    }),
                    style: ''
                })
            });
            this.olMap.addLayer(this.tilesLayer);
        },

        /**
         * Triggers a (fake) resize event to fix any map distortions caused by flex-box (mac-only issue?)
         */
        triggerResize: function () {
            var evt = document.createEvent("HTMLEvents");
            evt.initEvent('resize', true, false);
            //noinspection JSUnresolvedFunction
            window.dispatchEvent(evt);
        },

        initMarkerLayer: function () {
            //noinspection JSCheckFunctionSignatures
            this.markerLayer = new ol.layer.Vector({
                name: 'marker',
                source: new ol.source.Vector({})
            });
            this.olMap.addLayer(this.markerLayer);
        },

        addMarker: function () {
            var coords = this.getLambertCoords();
            var feature = new ol.Feature({
                name: 'marker',
                geometry: new ol.geom.Point(coords)
            });
            var style = new ol.style.Style({
                image: new ol.style.Icon({
                    anchor: [0.5, 0.9],
                    size: [80, 90],
                    opacity: 1,
                    scale: 0.7,
                    src: lib.base + 'img/marker-selected.png'
                })
            });
            feature.setStyle(style);
            this.markerLayer.getSource().clear();
            this.markerLayer.getSource().addFeature(feature);
            // pan/zoom to marker
            var map = this.olMap;
            var view = map.getView();
            //noinspection JSCheckFunctionSignatures
            setTimeout(function () {
                var pan = ol.animation.pan({
                    source: view.getCenter(),
                    duration: 750
                });
                var zoom = ol.animation.zoom({
                    resolution: view.getResolution()
                });
                map.beforeRender(pan);
                map.beforeRender(zoom);
                view.setCenter(coords);
                view.setResolution(lib.resolutions[5]);
            }, 500);
        }
    };

    $(lib.init);

})(jQuery);
