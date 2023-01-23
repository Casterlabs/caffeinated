#!/bin/bash

# Copy the widget loader to the widgets dev server.
mkdir widgets/static/loader
cp -r widget-loader/* widgets/static/loader

# Run both the UI and widgets dev servers.
npx concurrently "cd ui && npm run dev" "cd widgets && npm run dev"
