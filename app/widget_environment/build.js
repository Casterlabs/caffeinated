// https://mjtdev.medium.com/how-to-create-a-single-file-bundle-of-a-large-typescript-project-in-2023-5693c8b6b142
import { build } from "esbuild";

build({
  entryPoints: ["src/entry.ts"],
  bundle: true,
  minify: false,
  platform: "browser",
  format: "iife",
  outfile: "./target/classes/widget-environment.js",
});
