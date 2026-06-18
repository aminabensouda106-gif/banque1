(function () {
    function bindConfirmForms() {
        document.querySelectorAll('form[data-confirm]').forEach(function (form) {
            if (form.dataset.confirmBound === 'true') {
                return;
            }
            form.dataset.confirmBound = 'true';
            form.addEventListener('submit', function (event) {
                var message = form.getAttribute('data-confirm');
                if (message && !window.confirm(message)) {
                    event.preventDefault();
                }
            });
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', bindConfirmForms);
    } else {
        bindConfirmForms();
    }
})();
