(function () {
    const header = document.querySelector('.landing-header');
    if (!header) {
        return;
    }

    const topThreshold = 76;
    const scrollDelta = 6;
    let lastScrollY = window.scrollY;
    let ticking = false;

    function updateHeader() {
        const scrollY = window.scrollY;

        if (scrollY <= topThreshold) {
            header.classList.add('landing-header--top');
            header.classList.remove('landing-header--hidden');
        } else {
            header.classList.remove('landing-header--top');

            if (scrollY > lastScrollY + scrollDelta) {
                header.classList.add('landing-header--hidden');
            } else if (scrollY < lastScrollY - scrollDelta) {
                header.classList.remove('landing-header--hidden');
            }
        }

        lastScrollY = scrollY;
        ticking = false;
    }

    window.addEventListener('scroll', function () {
        if (!ticking) {
            window.requestAnimationFrame(updateHeader);
            ticking = true;
        }
    }, { passive: true });

    updateHeader();
})();
