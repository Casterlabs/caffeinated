#!/bin/sh

# Copy the widget loader to the appropriate places...
./mvnw generate-sources
# Then run the dev servers.
npx concurrently "cd CaffeinatedApp/src/main/ui && npm install && npm run dev" "cd BuiltInPlugins/src/main/widgets && npm install && npm run dev"
