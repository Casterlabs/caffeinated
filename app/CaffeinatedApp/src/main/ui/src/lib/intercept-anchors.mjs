export default function hook(handler) {
	document.onclick = (e) => {
		let target = e.target || e.srcElement;

		while (true) {
			if (!target) return;
			if (target.tagName == "A") break;

			target = target.parentElement;
		}

		if (target.getAttribute('target') != '_blank') return;

		const href = target.getAttribute('href');
		e.preventDefault();
		handler(href);
	};
}
