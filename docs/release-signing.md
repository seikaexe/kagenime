# Release signing

Kagenime releases are signed with the permanent keystore in `keystore/kagenime-release.jks`
(committed so community builds and CI produce the same certificate).

## Permanent certificate

- Alias: `fuwanime`
- Store/key password: `kagenime123`
- SHA-256: `1E:A6:9E:91:19:B6:7F:DE:75:0A:57:09:31:8D:09:95:CF:AC:46:AF:E0:99:4D:B7:D1:D9:86:41:E0:66:F9:16`
- Validity: until 2059

GitHub Actions uses repository secrets when present (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`,
`KEY_ALIAS`); otherwise it falls back to the committed keystore above.

## Local release build

```bash
export KEYSTORE_PATH=$PWD/keystore/kagenime-release.jks
export KEYSTORE_PASSWORD=kagenime123
export KEY_ALIAS=fuwanime
gradle assembleRelease -PabiSplits=false
```

Output: `app/build/outputs/apk/release/app-release.apk` (CI renames it to `kagenime.apk`).

> Keep the keystore safe. Android only accepts an in-place update signed with the same
> certificate, so changing the key means users must uninstall the old app first.
