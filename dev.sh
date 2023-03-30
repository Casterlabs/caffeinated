#!/bin/bash

# Copy the widget loader to the widgets dev server.
mkdir widgets/static/loader
cp -r widget-loader/* widgets/static/loader

# Copy the widget loader to the UI dev server.
mkdir ui/static/loader
cp -r widget-loader/* ui/static/loader

# Run both the UI and widgets dev servers.
npx concurrently "cd ui && npm install && npm run dev" "cd widgets && npm install && npm run dev"
