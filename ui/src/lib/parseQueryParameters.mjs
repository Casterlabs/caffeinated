export default function (url = window.location.href) {
	let vars = {};

	url.replace(/[?&]+([^=&]+)=([^&]*)/gi, (_, key, value) => {
		vars[key] = value;
	});

	return vars;
}
