#!/bin/sh

# Copy the widget loader to the widgets dev server.
rm -rf app/BuiltInPlugins/src/main/widgets/static/loader
mkdir app/BuiltInPlugins/src/main/widgets/static/loader
cp -r widget-loader/* app/BuiltInPlugins/src/main/widgets/static/loader

# Copy the widget loader to the UI dev server.
rm -rf app/UI/static/loader
mkdir app/UI/static/loader
cp -r widget-loader/* app/UI/static/loader

# Run both the UI and widgets dev servers.
npx concurrently "cd app/UI && npm install && npm run dev" "cd app/BuiltInPlugins/src/main/widgets && npm install && npm run dev"
