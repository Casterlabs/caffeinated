export default function changeFont(fontname) {
	if (typeof document == 'undefined') {
		return; // We're running on the server or worker, ignore.
	}

	fontname = fontname || 'Poppins';
	document.documentElement.style = "font-family: '" + fontname + "';";

	WebFont.load({
		google: {
			families: [fontname]
		}
	});
}
