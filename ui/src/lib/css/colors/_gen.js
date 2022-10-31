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

    // Metals
    'gold',
    'bronze'
];

const bases = [
    // Grays
    'gray',
    'mauve',
    'slate',
    'sage',
    'olive',
    'sand'
];

const misc = [
    // Overlays
    'blackA',
    'whiteA'
];

fs.mkdirSync('./primary');
fs.mkdirSync('./base');

for (const color of colors) {
    const name = color;
    let css = '#css-intermediate {';

    for (let idx = 1; idx < 13; idx++) {
        css += `
    --primary${idx}: var(--${color}${idx});
        `;
    }

    css += '}';

    fs.writeFile(`./primary/${name}.css`, css, () => {});
}

for (const base of bases) {
    const name = base;
    let css = '#css-intermediate {';

    for (let idx = 1; idx < 13; idx++) {
        css += `
    --base${idx}: var(--${base}${idx});
        `;
    }

    css += '}';

    fs.writeFile(`./base/${name}.css`, css, () => {});
}

for (const color of misc) {
    const name = 'overlay-' + color.substring(0, color.length - 1);
    let css = `
    @import "@radix-ui/colors/${color}.css";
    `;

    for (let idx = 1; idx < 13; idx++) {
        css += `
            /* ${name} ${idx} */
            .text-${name}-${idx},
            .hover\\:text-${name}-${idx}:hover,
            .focus\\:text-${name}-${idx}:focus {
                color: var(--${color}${idx})
            }
            .border-${name}-${idx},
            .hover\\:border-${name}-${idx}:hover,
            .focus\\:border-${name}-${idx}:focus {
                border-color: var(--${color}${idx})
            }
            .ring-${name}-${idx},
            .hover\\:ring-${name}-${idx}:hover,
            .focus\\:ring-${name}-${idx}:focus {
                --tw-ring-color: var(--${color}${idx})
            }
            .bg-${name}-${idx},
            .hover\\:bg-${name}-${idx}:hover,
            .focus\\:bg-${name}-${idx}:focus {
                background-color: var(--${color}${idx})
            }
        `;
    }

    fs.writeFile(`./${name}.css`, css, () => {});
}