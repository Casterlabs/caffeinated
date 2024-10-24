// https://mjtdev.medium.com/how-to-create-a-single-file-bundle-of-a-large-typescript-project-in-2023-5693c8b6b142
import { build } from "esbuild";

const sharedConfig = {
    entryPoints: ["src/entry.ts"],
    bundle: true,
    minify: false
};

build({
    ...sharedConfig,
    platform: 'browser',
    format: 'iife',
    outfile: "../app/Caffeinated/src/main/resources/widget-environment.js",
});