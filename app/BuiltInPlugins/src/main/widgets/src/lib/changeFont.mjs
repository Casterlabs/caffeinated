const WEIGHTS = [
	"100",
	"200",
	"300",
	"400",
	"500",
	"600",
	"700",
	"800",
	"900",
	"1000",
	"100italic",
	"200italic",
	"300italic",
	"400italic",
	"500italic",
	"600italic",
	"700italic",
	"800italic",
	"900italic",
	"1000italic",
];

export default function changeFont(fontname) {
	if (typeof document == 'undefined') {
		return; // We're running on the server or worker, ignore.
	}

	fontname = fontname || 'Poppins';
	document.documentElement.style = "font-family: '" + fontname + "';";

	WebFont.load({
		google: {
			families: [`${fontname}:${WEIGHTS.join(",")}`]
		}
	});
}
