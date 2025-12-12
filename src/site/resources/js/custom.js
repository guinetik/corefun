/**
 * CoreFun Site - Mobile Navigation & Syntax Highlighting
 */
(function() {
  'use strict';

  document.addEventListener('DOMContentLoaded', function() {
    // =========================================================================
    // Syntax Highlighting with Prism.js
    // =========================================================================
    initSyntaxHighlighting();

    // =========================================================================
    // Mobile Navigation
    // =========================================================================
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
          if (window.innerWidth <= 992) {
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

      // Close nav when window resizes to desktop
      window.addEventListener('resize', function() {
        if (window.innerWidth > 992) {
          nav.classList.remove('open');
          nav.querySelectorAll('.dropdown').forEach(function(dd) {
            dd.classList.remove('open');
          });
        }
      });
    }
  });

  /**
   * Initialize Prism.js syntax highlighting
   * Maven Site generates code blocks with language classes from markdown fences.
   * We also detect language for blocks without classes and ensure Prism runs.
   */
  function initSyntaxHighlighting() {
    // Find all code blocks
    var codeBlocks = document.querySelectorAll('pre code, pre.source');

    codeBlocks.forEach(function(code) {
      var pre = code.tagName === 'PRE' ? code : code.parentElement;
      var content = code.textContent || '';

      // Check if already has a language class
      var hasLanguageClass = code.className && code.className.match(/language-/);

      if (!hasLanguageClass) {
        // Detect language from content or existing class
        var language = detectLanguage(content, code.className);

        // Add Prism classes
        if (language) {
          code.classList.add('language-' + language);
          pre.classList.add('language-' + language);
        }
      }

      // Ensure pre also has the language class for proper styling
      if (code.className && code.className.match(/language-(\w+)/)) {
        var lang = code.className.match(/language-(\w+)/)[1];
        if (!pre.classList.contains('language-' + lang)) {
          pre.classList.add('language-' + lang);
        }
      }
    });

    // Wait for Prism to be available, then highlight
    waitForPrism(function() {
      Prism.highlightAll();
    });
  }

  /**
   * Wait for Prism to be loaded (handles defer/async loading)
   */
  function waitForPrism(callback, attempts) {
    attempts = attempts || 0;
    if (typeof Prism !== 'undefined') {
      callback();
    } else if (attempts < 50) {
      // Retry up to 50 times (5 seconds total)
      setTimeout(function() {
        waitForPrism(callback, attempts + 1);
      }, 100);
    }
  }

  /**
   * Detect programming language from code content
   */
  function detectLanguage(content, className) {
    // Check for explicit language in class
    if (className) {
      if (className.indexOf('java') !== -1) return 'java';
      if (className.indexOf('xml') !== -1) return 'xml';
      if (className.indexOf('bash') !== -1 || className.indexOf('shell') !== -1) return 'bash';
      if (className.indexOf('json') !== -1) return 'json';
    }

    // Detect from content patterns
    var trimmed = content.trim();

    // XML/Maven POM
    if (trimmed.match(/^<\?xml/) || trimmed.match(/^<(dependency|plugin|project|groupId|artifactId)/)) {
      return 'xml';
    }

    // Java patterns
    if (trimmed.match(/^(package|import|public\s+class|public\s+interface|@\w+)/m) ||
        trimmed.match(/\.(map|filter|fold|flatMap)\s*\(/) ||
        trimmed.match(/Result<|Try\.|Computable\./)) {
      return 'java';
    }

    // Bash/shell
    if (trimmed.match(/^(\$|#!\/bin\/(ba)?sh|mvn |npm |git )/m)) {
      return 'bash';
    }

    // JSON
    if (trimmed.match(/^\s*[\[{]/) && trimmed.match(/[\]}]\s*$/)) {
      return 'json';
    }

    // Default to Java for this project
    return 'java';
  }
})();
