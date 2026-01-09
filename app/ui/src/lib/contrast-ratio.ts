/**
 * Represents a colour in HSL space.
 *  h – hue   (0–360)
 *  s – sat.  (0–100)
 *  l – light (0–100)
 */
export class HSL {
	constructor(
		public h: number,
		public s: number,
		public l: number
	) {
		this.h = ((h % 360) + 360) % 360; // wrap hue
		this.s = clamp01(s / 100) * 100;
		this.l = clamp01(l / 100) * 100;
	}

	/* ---------- convenience factories ---------- */

	/**
	 * Accepts 3‑ or 6‑digit hex (#abc or #aabbcc)
	 */
	static fromHex(hex: string): HSL {
		let h = hex.replace(/^#/, '');
		if (h.length === 3)
			h = h
				.split('')
				.map((c) => c + c)
				.join('');
		if (h.length !== 6 || /[^0-9a-f]/i.test(h)) throw new Error('Invalid hex colour');
		const r = parseInt(h.slice(0, 2), 16);
		const g = parseInt(h.slice(2, 4), 16);
		const b = parseInt(h.slice(4, 6), 16);
		return HSL.fromRgb(r, g, b);
	}

	/**
	 * Accepts rgb() CSS strings like "rgb(255, 0, 0)".
	 */
	static fromRgbString(rgb: string): HSL {
		const m = rgb.match(/rgb\(\s*([0-9]{1,3})\s*,\s*([0-9]{1,3})\s*,\s*([0-9]{1,3})\s*\)/i);
		if (!m) throw new Error('Invalid rgb() string');
		return HSL.fromRgb(+m[1], +m[2], +m[3]);
	}

	/**
	 * Accepts an RGB tuple [0–255, 0–255, 0–255]
	 */
	static fromRgb(r: number, g: number, b: number): HSL {
		r /= 255;
		g /= 255;
		b /= 255;
		const max = Math.max(r, g, b),
			min = Math.min(r, g, b);
		let h: number,
			s: number,
			l = (max + min) / 2;
		if (max === min) {
			h = 0;
			s = 0;
		} else {
			const d = max - min;
			s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
			switch (max) {
				case r:
					h = (g - b) / d + (g < b ? 6 : 0);
					break;
				case g:
					h = (b - r) / d + 2;
					break;
				default:
					h = (r - g) / d + 4;
					break;
			}
			h *= 60;
		}
		return new HSL(h, s * 100, l * 100);
	}

	/**
	 * Accepts rgb() CSS strings like "hsl(246, 6.0%, 9.0%)".
	 */
	static fromHslString(hsl: string): HSL {
		const m = hsl.match(/hsl\(\s*([0-9]{1,3}(?:\.[0-9]+)?)\s*,\s*([0-9]{1,3}(?:\.[0-9]+)?)%\s*,\s*([0-9]{1,3}(?:\.[0-9]+)?)%\s*\)/i);
		if (!m) throw new Error('Invalid hsl() string');
		return new HSL(+m[1], +m[2], +m[3]);
	}

	/**
	 * Smart constructor – infers type from the input.
	 */
	static from(value: string | [number, number, number]): HSL {
		if (typeof value === 'string') {
			if (value.startsWith('#')) return HSL.fromHex(value);
			if (value.startsWith('rgb')) return HSL.fromRgbString(value);
			if (value.startsWith('hsl')) return HSL.fromHslString(value);
			throw new Error('Unsupported colour string format');
		}
		if (Array.isArray(value) && value.length === 3) {
			return HSL.fromRgb(value[0], value[1], value[2]);
		}
		throw new Error('Unsupported colour input');
	}

	/* ---------- conversions ---------- */

	/** Returns an [r, g, b] tuple (0–255 each). */
	toRgb(): [number, number, number] {
		const h = this.h / 360;
		const s = this.s / 100;
		const l = this.l / 100;

		if (s === 0) {
			const x = Math.round(l * 255);
			return [x, x, x];
		}

		const hue2rgb = (p: number, q: number, t: number): number => {
			if (t < 0) t += 1;
			if (t > 1) t -= 1;
			if (t < 1 / 6) return p + (q - p) * 6 * t;
			if (t < 1 / 2) return q;
			if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6;
			return p;
		};

		const q = l < 0.5 ? l * (1 + s) : l + s - l * s;
		const p = 2 * l - q;

		const r = hue2rgb(p, q, h + 1 / 3);
		const g = hue2rgb(p, q, h);
		const b = hue2rgb(p, q, h - 1 / 3);

		return [Math.round(r * 255), Math.round(g * 255), Math.round(b * 255)];
	}

	/** Returns a valid CSS `hsl()` string. */
	toCss(): string {
		return `hsl(${Math.round(this.h)} ${Math.round(this.s)}% ${Math.round(this.l)}%)`;
	}

	/** WCAG relative luminance (0–1). */
	relativeLuminance(): number {
		return this.toRgb()
			.map((c) => {
				const v = c / 255;
				return v <= 0.03928 ? v / 12.92 : ((v + 0.055) / 1.055) ** 2.4;
			})
			.reduce((acc, v, i) => acc + v * [0.2126, 0.7152, 0.0722][i], 0);
	}

	/** Clone the current colour. */
	clone(): HSL {
		return new HSL(this.h, this.s, this.l);
	}
}

/* ---------- contrast helpers ---------- */

/**
 * Computes WCAG contrast ratio between two relative luminances.
 */
export function contrastRatio(l1: number, l2: number): number {
	return (Math.max(l1, l2) + 0.05) / (Math.min(l1, l2) + 0.05);
}

/**
 * Adjust `text` colour lightness (keeping hue & saturation) so that the contrast
 * with `bg` is at least `minRatio`. Returns a **new** HSL instance.
 * Falls back to black/white if the target cannot be met while preserving hue.
 */
export function adjustTextColor(bg: HSL, text: HSL, minRatio = 4.5): HSL {
	const bgLum = bg.relativeLuminance();
	const candidate = text.clone();

	// Binary search lightness 0–100
	let lo = 0,
		hi = 100,
		bestL = candidate.l;
	for (let i = 0; i < 12; i++) {
		// 2^12 ≈ 0.00024 precision
		const mid = (lo + hi) / 2;
		candidate.l = mid;
		const ratio = contrastRatio(candidate.relativeLuminance(), bgLum);

		const needMore = ratio < minRatio;
		if ((bgLum < 0.5 && needMore) || (bgLum >= 0.5 && !needMore)) {
			lo = mid; // bg is dark → lighten; bg is light → darken
		} else {
			hi = mid;
			if (!needMore) bestL = mid;
		}
	}

	candidate.l = bestL;
	/*if (contrastRatio(candidate.relativeLuminance(), bgLum) >= minRatio)*/ return candidate;

	// const black = HSL.fromHex('#000');
	// const white = HSL.fromHex('#fff');
	// return contrastRatio(black.relativeLuminance(), bgLum) >= minRatio ? black : white;
}

/* ---------- internal helpers ---------- */
function clamp01(v: number): number {
	return Math.min(1, Math.max(0, v));
}
