const SAUCER = window.saucer = {
	internal: {
		send: (payload) => {
			window.cefQuery({
				// CEF has a weird internal way of handling strings, and unfortunately
				// the JNI wrapper mangles them. We've opted to just use URI encoding
				// to prevent manglage.
				request: encodeURIComponent(JSON.stringify(payload)),
				persistent: false,
				onSuccess() { },
				onFailure() { }
			});
		}
	}
};

const initData = %s;

const RPC = {
	__idx: 0,
	waiting: {},
	
	__wfmHandlers: {}, // "Watch for mutate handlers"

	send: function (data) {
		SAUCER.internal.send({ message: data });
	},
	sendWithPromise: async function (data) {
		// const start = Date.now();
		const requestId = RPC.__idx++;
		// console.debug("[Saucer]", "RPC.sendWithPromise", data, requestId);
		try {
			return await new Promise((resolve, reject) => {
				RPC.waiting[requestId] = { resolve, reject };
				SAUCER.internal.send({ message: { ...data, requestId } });
			});
		} finally {
			// console.debug("[Saucer]", "RPC sendWithPromise took ", Date.now() - start);
			delete RPC.waiting[requestId];
		}
	},

	get: function (objectId, propertyName) {
		return RPC.sendWithPromise({ type: "GET", objectId, propertyName });
	},
	set: async function (objectId, propertyName, newValue) {
		await RPC.sendWithPromise({ type: "SET", objectId, propertyName, newValue }); // Wait for the set() to complete (or throw).
		return newValue;
	},
	invoke: function (objectId, functionName, args) {
		return RPC.sendWithPromise({ type: "INVOKE", objectId, functionName, arguments: args });
	},
	checkForMutations: function () {
		return RPC.sendWithPromise({ type: "CHECK_MUTATION" });
	},
};
Object.defineProperty(SAUCER, "__rpc", {
	value: RPC,
	writable: false,
	configurable: true,
});

const MESSAGES = {
	__idx: 0,
	__listeners: {},

	onMessage: function (callback) {
		if (typeof callback != "function") {
			throw "Callback must be a fuction";
		}

		const registrationId = MESSAGES.__idx++;

		MESSAGES.__listeners[registrationId] = callback;

		return registrationId;
	},

	off: function (registrationId) {
		delete MESSAGES.__listeners[registrationId];
	},

	emit: function (data) {
		if (typeof data == "undefined") {
			throw "Data cannot be undefined!";
		}
		RPC.send({ type: "MESSAGE", data });
	},

	__internal: function (data) {
		Object.values(MESSAGES.__listeners).forEach((callback) => {
			try {
				callback(data);
			} catch (e) {
				console.error("[Saucer]", "A listener produced an exception: ");
				console.error(e);
			}
		});
	}

};
Object.defineProperty(SAUCER, "messages", {
	value: MESSAGES,
	writable: false,
	configurable: true,
});

Object.defineProperty(SAUCER, "openLinkInSystemBrowser", {
	value: function (link) {
		RPC.send({ type: "OPEN_LINK", link });
	},
	writable: false,
	configurable: true,
});

Object.defineProperty(SAUCER, "generateTypescriptDefinitions", {
	value: function () {
		return RPC.sendWithPromise({ type: "GENERATE_TS_DEFINITIONS" });
	},
	writable: false,
	configurable: true,
});

Object.defineProperty(SAUCER, "MUTATION_POLL_RATE", {
	value: 150,
	writable: true,
	configurable: true,
});
async function checkForMutations() {
	try {
		const mutations = await RPC.checkForMutations();
		
		for (const [toBeSplit, value] of Object.entries(mutations)) {
			const [objectId, propertyName] = toBeSplit.split("|");
			
			for (const handler of Object.values(RPC.__wfmHandlers[objectId][propertyName])) {
				try {
					handler(value);
				} catch (e) {
					console.error("[Saucer]", "A mutation listener produced an exception: ");
					console.error(e);
				}
			}
		}
	} catch (e) {
		console.error("[Saucer]", "An error occurred whilst checking for mutations:");
		console.error(e);
	} finally {
		setTimeout(checkForMutations, SAUCER.MUTATION_POLL_RATE);
	}
}
checkForMutations();

for (const [key, value] of Object.entries(initData)) {
	Object.defineProperty(SAUCER, key, {
		value: value,
		writable: false,
		configurable: true,
	});
}
