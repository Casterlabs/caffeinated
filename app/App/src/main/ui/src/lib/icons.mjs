const iconCache = {};
let baseUrl;

function replaceIcon(/** @type {HTMLElement} */ element) {
	if (element.hasAttribute('data-replaced')) return;

	const icon = element.getAttribute('data-icon');
	element.setAttribute('data-replaced', 'true');

	if (iconCache[icon]) {
		element.innerHTML = iconCache[icon];
		console.debug('[App]', 'Loaded icon from cache:', icon);
	} else {
		const [iconType, iconPath] = icon.split('/');

		(async () => {
			try {
				let svg;

				switch (iconType) {
					case 'icon':
						const promises = [
							fetch(`${baseUrl}/images/icons/solid/${iconPath}.svg`)
								.then((res) => {
									if (res.ok) {
										return res.text();
									} else {
										throw 'Status: ' + res.status;
									}
								})
								.then((s) => s.replace('<svg', '<svg class="icon-solid"')),

							fetch(`${baseUrl}/images/icons/outline/${iconPath}.svg`)
								.then((res) => {
									if (res.ok) {
										return res.text();
									} else {
										throw 'Status: ' + res.status;
									}
								})
								.then((s) => s.replace('<svg', '<svg class="icon-outline"'))
						];

						svg = (await Promise.all(promises)).join('');
						break;

					case 'service':
						svg = await fetch(`${baseUrl}/images/services/${iconPath}/icon.svg`)
							.then((res) => {
								if (res.ok) {
									return res.text();
								} else {
									throw 'Status: ' + res.status;
								}
							})
							.then((s) => s.replace('<svg', '<svg class="icon-service"'));
						break;
				}

				if (svg) {
					element.innerHTML = iconCache[icon] = svg;
					console.debug('[App]', 'Loaded icon:', icon);
				} else {
					throw 'Unknown icon: ' + icon;
				}
			} catch (e) {
				element.innerHTML = iconCache[icon] =
					'<div class="bg-red-500 h-full w-full text-white" title="MISSING ICON">X</div>'; // Visual error.
				console.error('[App]', 'Could not load icon', icon, 'due to an error:');
				console.error(e);
			}

			element.setAttribute('data-icon-type', iconType);
		})();
	}
}

export default function hook(base = '') {
	baseUrl = base;

	new MutationObserver(() => {
		// for (const record of records) {
		// 	if (record.addedNodes.length > 0) {
		// 		for (const element of record.addedNodes) {
		// 			if (element.nodeName.toLowerCase() == 'icon') {
		// 				replaceIcon(element);
		// 				break;
		// 			}
		// 		}
		// 	}

		// 	// SvelteKit likes to undo all of our hard work. Let's prevent that.
		// 	const element = record.target;
		// 	if (element.nodeName.toLowerCase() == 'icon') {
		// 		replaceIcon(element);
		// 	}
		// }

		document.querySelectorAll('icon').forEach(replaceIcon);
	}).observe(document.body, {
		subtree: true,
		attributes: true,
		childList: true
	});

	document.querySelectorAll('icon').forEach(replaceIcon);
}
