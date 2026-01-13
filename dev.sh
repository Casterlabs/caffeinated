#!/bin/sh

cd app

# Copy the widget loader to the appropriate places...
./mvnw generate-sources
# Then run the dev servers.
npx concurrently "cd ui && npm install && npm run dev" "cd built_in_plugins/src/main/widgets && npm install && npm run dev"
