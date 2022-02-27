
// // These are all meant to be global.
// const MIN_ZOOM = -2;
// const MAX_ZOOM = 2;

// const { shell, remote, webFrame } = require("electron");
// const { app } = remote;
// const currentWindow = remote.getCurrentWindow();

// // Poll for updated zoom level, and make sure it's within proper bounds.
// {
//     const zoomChangedEvent = new Event("zoom_changed");

//     let expectedZoom = webFrame.getZoomLevel();

//     function checkZoom(force = false) {
//         const currentZoomLevel = webFrame.getZoomLevel();

//         if ((expectedZoom != currentZoomLevel) || force) {
//             if (currentZoomLevel < MIN_ZOOM) {
//                 console.warn("[UI]", "Zoom level was smaller than", MIN_ZOOM, "so it was reset.");
//                 webFrame.setZoomLevel(MIN_ZOOM);
//                 expectedZoom = MIN_ZOOM;
//             } else if (currentZoomLevel > MAX_ZOOM) {
//                 console.warn("[UI]", "Zoom level was larger than", MAX_ZOOM, "so it was reset.");
//                 webFrame.setZoomLevel(MAX_ZOOM);
//                 expectedZoom = MAX_ZOOM;
//             } else {
//                 console.debug("[UI]", "Zoom level has changed to", currentZoomLevel);
//                 expectedZoom = currentZoomLevel;
//             }


//             window.dispatchEvent(zoomChangedEvent);
//         }
//     }

//     setInterval(checkZoom, 750);
//     checkZoom(true);
// }

// // Helper
// function openLink(link) {
//     console.debug("[Framework]", "Opening", link);
//     shell.openExternal(link);
// }

function getUrlVars() {
    const vars = {};

    window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function (m, key, value) {
        vars[key] = value;
    });

    return vars;
}
