import fs from 'fs';

// $ node _gen.js

// https://www.radix-ui.com/docs/colors/palette-composition/the-scales

const colors = [
    // Colors
    'tomato',
    'red',
    'crimson',
    'pink',
    'plum',
    'purple',
    'violet',
    'indigo',
    'blue',
    'cyan',
    'teal',
    'green',
    'grass',
    'orange',
    'brown',

    // Bright Colors
    'sky',
    'mint',
    'lime',
    'yellow',
    'amber',

    // Grays
    'gray',
    'mauve',
    'slate',
    'sage',
    'olive',
    'sand',

    // Metals
    'gold',
    'bronze'
];

const misc = [
    // Overlays
    'blackA',
    'whiteA'
];

for (const color of colors) {
    let css = '';

    for (let idx = 1; idx < 13; idx++) {
        css += `
            /* ${color} ${idx} */
            .text-${color}-${idx},
            .hover\\:text-${color}-${idx}:hover {
                color: var(--${color}${idx})
            }
            .bg-${color}-${idx},
            .hover\\:bg-${color}-${idx}:hover {
                background-color: var(--${color}${idx})
            }
        `;
    }

    fs.writeFile(`./${color}.css`, css, () => {});
}

for (const color of misc) {
    let css = `
    @import "@radix-ui/colors/${color}.css";
    `;

    for (let idx = 1; idx < 13; idx++) {
        css += `
            /* ${color} ${idx} */
            .text-overlay-${color.substring(0, color.length - 1)}-${idx},
            .hover\\:text-overlay-${color.substring(0, color.length - 1)}-${idx}:hover {
                color: var(--${color}${idx})
            }
            .bg-overlay-${color.substring(0, color.length - 1)}-${idx},
            .hover\\:.bg-overlay-${color.substring(0, color.length - 1)}-${idx}:hover {
                background-color: var(--${color}${idx})
            }
        `;
    }

    fs.writeFile(`./overlay-${color.substring(0, color.length - 1)}.css`, css, () => {});
}

{
    let css = ``;

    for (const color of colors) {
        css += `import '$lib/css/colors/${color}.css'\n`;
    }
    for (const color of misc) {
        css += `import '$lib/css/colors/overlay-${color.substring(0, color.length - 1)}.css'\n`;
    }

    console.log(css);
}

{
    let css = '';

    for (const color of colors) {
        css += `@import "@radix-ui/colors/${color}.css";\n`;
    }

    console.log(css);
}

{
    let css = '';

    for (const color of colors) {
        css += `@import "@radix-ui/colors/${color}Dark.css";\n`;
    }

    console.log(css);
}