/**
 * Library for toggling link sections on resource pages
 */

(function ($) {

    var lib = {

        /**
         * Starts the lib, called when the DOM is ready
         */
        init: function () {
            lib.base = $('base').attr('href') || '/';
            lib.injectToggles();
            lib.initEvents();
        },

        injectToggles: function () {
            $('head').append($('<link rel="stylesheet" type="text/css" />').attr('href', lib.base + 'css/milieuinfo-section-toggles.css'));
            $('.links-heading').each(function () {
                lib.injectToggle(this);
            });
        },

        injectToggle: function (heading) {
            var toggle = document.createElement('a');
            toggle.classList.add('toggle');
            heading.insertBefore(toggle, heading.firstChild);
        },

        /**
         * Activates sub-tree toggling with on-demand loading of narrower concepts
         */
        initEvents: function () {
            $('.links-heading').on('click', '.toggle', function (event) {
                event.preventDefault();
                var heading = this.parentNode;
                heading.classList.toggle('collapsed');
            });
        }
    };

    $(lib.init);

})(jQuery);
