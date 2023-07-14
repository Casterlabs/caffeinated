export default function getAverageColor(src) {
	return new Promise((resolve) => {
		const img = new Image();

		img.src = src;
		img.crossOrigin = 'anonymous';
		img.addEventListener('load', () => {
			const canvas = document.createElement('canvas');

			canvas.width = img.width;
			canvas.height = img.height;

			const ctx = canvas.getContext('2d');
			ctx.drawImage(img, 0, 0);

			const imageData = ctx.getImageData(0, 0, img.width, img.height);
			const data = imageData.data;

			let pixelCount = data.length / 4;
			let r = 0;
			let g = 0;
			let b = 0;
			// let a = 0;

			for (let x = 0; x < data.length; x += 4) {
				r += data[x + 0];
				g += data[x + 1];
				b += data[x + 2];
				// a += data[x + 3];
			}

			r /= pixelCount;
			g /= pixelCount;
			b /= pixelCount;
			// a /= pixelCount;

			resolve([r, g, b]);
		});
	});
}
