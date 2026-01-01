import fs from 'fs';

// $ node _gen.js

// https://www.radix-ui.com/docs/colors/palette-composition/the-scales

const colors = [
	// Grays
	'gray',
	'mauve',
	'slate',
	'sage',
	'olive',
	'sand',

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

// Reset.
fs.rmdirSync('./primary', { recursive: true, force: true });
fs.rmdirSync('./base', { recursive: true, force: true });

// Create our dirs.
fs.mkdirSync('./primary');
fs.mkdirSync('./base');

for (const color of colors) {
	let css = `@import "@radix-ui/colors/${color}.css"; @import "@radix-ui/colors/${color}Dark.css";`;
	css += `#css-intermediate[data-theme-primary="${color}"] {`;

	for (let idx = 1; idx < 13; idx++) {
		css += `
    --primary${idx}: var(--${color}${idx});
        `;
	}

	css += '}';

	console.log(`import "$lib/css/colors/primary/${color}.css"`);
	fs.writeFile(`./primary/${color}.css`, css, () => {});
}

for (const color of colors) {
	let css = `@import "@radix-ui/colors/${color}.css"; @import "@radix-ui/colors/${color}Dark.css";`;
	css += `#css-intermediate[data-theme-base="${color}"] {`;

	for (let idx = 1; idx < 13; idx++) {
		css += `
    --base${idx}: var(--${color}${idx});
        `;
	}

	css += '}';

	console.log(`import "$lib/css/colors/base/${color}.css"`);
	fs.writeFile(`./base/${color}.css`, css, () => {});
}
