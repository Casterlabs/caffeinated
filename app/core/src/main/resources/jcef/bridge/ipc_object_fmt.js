const RPC = window.saucer.__rpc;

const id = %s;
const path = %s.split(".");

const functionNames = %s;
const propertyNames = %s;

// console.debug(id, path, functionNames, propertyNames);

RPC.__wfmHandlers[id] = {};

const object = {
	onMutate: function(propertyName, handler) {
		const registrationId = Math.random().toString(28);
		RPC.__wfmHandlers[id][propertyName][registrationId] = handler;
		return `${propertyName}|${registrationId}`;
	},
	offMutate(toBeSplit) {
		const [propertyName, registrationId] = toBeSplit.split("|");
		delete RPC.__wfmHandlers[id][propertyName][registrationId];
	}
};

for (const functionName of functionNames) {
	Object.defineProperty(object, functionName, {
		value: function () {
			return RPC.invoke(id, functionName, Array.from(arguments));
		},
	});
}

for (const propertyName of propertyNames) {
	RPC.__wfmHandlers[id][propertyName] = {};

	// Listen for WFM events for this property.
	object.onMutate(propertyName, (value) => {
		object[propertyName] = Promise.resolve(value);
	});
	
	Object.defineProperty(object, propertyName, {
		value: undefined, // Placeholder so that the Dev Tools will see this property.
		writable: true,
		configurable: true,
	});
}

const proxy = new Proxy(object, {
    get(obj, propertyName) {
		if (typeof obj[propertyName] !== "undefined") {
		    return obj[propertyName]; // For functions & watchForMutate fields.
		}

        return RPC.get(id, propertyName);
    },
    set(obj, propertyName, value) {
        return RPC.set(id, propertyName, value);
    },
});

const propertyName = path.pop();

// Resolve the root object.
let root = window;
for (const part of path) {
	if (!root[part]) root[part] = {}; // Allow setting nested on things that don't exist (yet).
	root = root[part];
}

Object.defineProperty(root, propertyName, {
	value: proxy,
	writable: true,
	configurable: true,
});