
function awaitGoto() {
    return new Promise((resolve) => {
        function wait() {
            if (window.goto) {
                resolve();
            }

            setTimeout(wait, 100);
        }

        wait();
    });
}

async function goto() {
    await awaitGoto();
    window.goto(...arguments);
}

const Router = {

    navigateSignin() {
        if (!location.pathname.startsWith("/welcome/")) {
            goto("/signin");
        }
    },

    goto() {
        goto(...arguments);
    },

    navigateHome() {
        if (getUrlVars().homeGoBack) {
            history.back();
        } else {
            goto("/home");
        }
    },

    tryHomeGoBack() {
        if (getUrlVars().homeGoBack) {
            history.back();
        }
    }

};

Object.freeze(Router);

export default Router;
