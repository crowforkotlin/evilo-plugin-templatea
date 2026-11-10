# evilo plugin template

A provider-neutral, multi-module Kotlin/Wasm template. Its remote is
`https://github.com/crowforkotlin/evilo-plugin-templatea`; the local directory
may remain `evilo-plugin-template`. No real content-provider implementation
belongs here. The bundled text/storage example is labeled demonstration content.

## Structure and naming

```text
evilo-plugin-core/                 Portable content, requests and parsing
evilo-plugin/                      WasmlineService adapter and WASI entrypoint
  plugin.json                      Release identity, permissions, catalog metadata
  icon.png                         Published icon; replace the placeholder
  icon.svg                         Editable icon source
evilo-plugin-build-logic/
  base/                            JVM/WASI library convention
  plugin/                          Descriptor, signing, source ZIP, catalog export
gradle/                            Version catalog and Gradle wrapper
```

The root is `evilo-plugin-template-root`. Explicit settings includes and prefixed
modules follow Wasmline's layout. Build conventions live in an included build,
not `buildSrc`. The core does not depend on the guest or its generated fields;
the guest passes the release descriptor to the portable implementation.

Fork this repository as `evilo-plugin-<provider>`. Name its guest module
`evilo-plugin-<provider>`, core `evilo-plugin-<provider>-core`, and root
`evilo-plugin-<provider>-root`. Update settings includes, type-safe dependencies,
packages, and the descriptor together. Use `crow.evilo.plugin.<provider>` or your
own reverse-DNS package. Implement the provider in that fork, not in this template
or the app. A stable signed plugin ID owns storage across versions.

## One maintained SDK

Modules consume `io.github.crowforkotlin.evilo:evilo-library:1.0.0`, which contains
both portable media models and the plugin/host protocol. This SDK is maintained
once in evilo's `evilo-library`. Never copy protocol/model source here.
Before SDK publication, use `-Pevilo.sdkPath=../evilo` to substitute the actual
SDK build. A third protocol repository is not required.

`EviloPluginService` exposes synchronous `describe`, `start`, and `resume` JSON
envelopes. `EviloHostService` exposes capabilities and private plugin storage.
The host performs asynchronous, permission-checked HTTP; the plugin constructs
requests and parses responses using explicit continuation state.

Bind services in the WASI entrypoint, but defer host lookup until an operation.
`describe()` has no network/storage side effects. Service methods cannot suspend,
overload, expose properties, or use generics, default arguments, varargs, or
service parameters. Unknown required capabilities are rejected; optional ones
need a defined fallback. SDK, protocol, plugin, and app versions are distinct.

Edit `evilo-plugin/plugin.json` for identity, media kinds, languages, API/media
permissions, authors, region, NSFW, introduction, dates, and update policy.
Generated guest fields and signed metadata use that one file. The descriptor
must fit Wasmline's 16 KiB metadata-string limit.

## Application authority

Users never enter keys. The app builder creates and preserves one Ed25519
signing authority per app release family from the evilo repository:

```sh
./gradlew :evilo-app:generatePluginSigningKeys
```

The default external directory is `~/.evilo/signing`. The app embeds only the
public key. The template checks and reuses `private.key`, `public.key`, and public
`authority.properties`; it cannot generate or replace that authority. Packages
signed by another authority are rejected even in custom catalogs.

Only the app authority operator exports signed releases. Independent authors
submit actual source for review/signing without receiving the private key.
Back up the authority and preserve it across app updates. For an override, pass
the same `-Pevilo.signingKeyDirectory=/external/directory` to both builds.
Never print, commit, upload, archive, or distribute private keys. Official review
and signatures do not prove that code is harmless.

## Storage and networking

Storage needs explicit permission and is isolated by signed plugin ID. Default
limits are 1 MiB total, 256 entries, 64 KiB/value, and 128-character keys. Plugins
own schema migrations. Storage is not an encrypted credential vault or file API.

Declare exact origins, paths, methods, and headers. Wildcards, cookies, ambient
credentials, and redirects are rejected. Signing does not authorize undeclared
network access. Browsers still need CORS and obey mixed-content rules; no proxy
is supplied. The current runtime exposes no guest fuel or linear-memory budget.

## Manual release export

Commands here are instructions, not recorded results or authorization to run
builds, tests, packaging, servers, uploads, installation, or screenshots. Use the
pinned JDK/Kotlin/Wasmline versions and compatible WASI Node.js. An optional
`wasmline.localPath` composite may execute upstream build tasks.

Commit the actual implementation first. Supply its full immutable source commit,
source snapshot, and directly served HTTPS release URLs without redirects:

```sh
./gradlew :evilo-plugin:exportPluginRelease \
  -Pevilo.sdkPath=../evilo \
  -Pplugin.sourceRevision="$SOURCE_COMMIT" \
  -Pplugin.releaseBaseUrl="$RELEASE_BASE_URL" \
  -Pplugin.sourceUrl="$SOURCE_REPOSITORY_URL" \
  -Pplugin.sourceSnapshotUrl="$SOURCE_SNAPSHOT_URL" \
  -Pplugin.publishedAt="$PUBLISHED_AT" \
  -Pwasmline.autoDownload=true
```

`SOURCE_COMMIT` must contain this implementation, not just a license or binaries.
`PUBLISHED_AT` is an ISO-8601 timestamp not earlier than creation. Downloads are
opt-in. Export writes a version-specific directory under
`evilo-plugin/build/release/` containing `manifest.wlm`, content-addressed
artifacts, actual source ZIP/hash, PNG, and `plugins.json`.

The reproducible ZIP includes both modules, build logic, specs, wrapper, docs,
icons, and license; it excludes build caches, Git, keys, and local properties.
The operator reviews working source versus the linked commit. Signed source
hashes do not prove build reproducibility or code safety.

Replace placeholder PNG/SVG icons before publication. The app loads bounded
PNG/JPEG/WebP, not remote SVG; icons must fit 1 MiB and 512-pixel dimensions.
Upload all artifacts and the exact source ZIP. Exported catalogs are not
certified. Never reuse a version code for changed code or permissions.

In evilo, add the JSON URL in Browse, choose a compatible release, approve its
permissions, and install. End users do not enter WLM URLs, key IDs, or keys.
See evilo's `docs/plugin-catalog.md` for catalog and mandatory-update semantics.
