# Release Process

## Branch flow

- Work happens on feature branches or `develop`.
- Pull requests targeting `master` run the `Android CI` workflow.
- `master` should be protected in GitHub with required status checks enabled for `Android CI`.

## CI on pull request

`Android CI` validates that the branch can become a release candidate:

- runs unit tests
- assembles `release` APK
- uploads the candidate APK as a workflow artifact

This artifact is for validation only. It is not the published release.

## Automatic release on master

After a merge to `master`, `Android Release`:

- recalculates the next version
- runs unit tests again
- builds the release APK
- publishes a GitHub Release
- attaches the APK asset

Versioning currently follows:

- `1.0.0`
- `1.1.0`
- `1.2.0`

The major version is controlled in `.github/release.properties`.

## Signing

If the repository contains these GitHub secrets, the release APK is signed:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Without those secrets, the pipeline still builds and publishes the release artifact, but it is not a production-signed APK.
