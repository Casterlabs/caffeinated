export default function hook(handler) {
	document.onclick = (e) => {
		const target = e.target || e.srcElement;

		if (target.tagName == 'A' && target.getAttribute('target') == '_blank') {
			const href = target.getAttribute('href');

			e.preventDefault();
			handler(href);
		}
	};
}
