/**
 * CoreFun Site - Mobile Navigation
 */
(function() {
  'use strict';

  document.addEventListener('DOMContentLoaded', function() {
    var hamburger = document.querySelector('#topbar .btn-navbar');
    var nav = document.querySelector('#topbar ul.nav:not(.pull-right)');

    if (hamburger && nav) {
      // Toggle menu on hamburger click
      hamburger.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        nav.classList.toggle('open');
      });

      // Close menu when clicking outside
      document.addEventListener('click', function(e) {
        if (nav.classList.contains('open')) {
          if (!nav.contains(e.target) && !hamburger.contains(e.target)) {
            nav.classList.remove('open');
          }
        }
      });

      // Handle dropdown toggles on mobile
      var dropdownToggles = nav.querySelectorAll('.dropdown-toggle');
      dropdownToggles.forEach(function(toggle) {
        toggle.addEventListener('click', function(e) {
          if (window.innerWidth <= 1200) {
            e.preventDefault();
            e.stopPropagation();
            var parent = this.parentElement;
            var wasOpen = parent.classList.contains('open');

            // Close all other dropdowns
            nav.querySelectorAll('.dropdown').forEach(function(dd) {
              dd.classList.remove('open');
            });

            // Toggle this one
            if (!wasOpen) {
              parent.classList.add('open');
            }
          }
        });
      });
    }
  });
})();
