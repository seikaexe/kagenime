# Cara build Kagenime

Repo: https://github.com/seikaexe/kagenime

## 1. Key yang dipakai untuk build

Build **debug** tidak butuh key apa pun.

Build **release** memakai keystore yang sudah ada di repo:

| Item | Nilai |
| --- | --- |
| File | `keystore/kagenime-release.jks` |
| Alias | `fuwanime` |
| Password store & key | `kagenime123` |
| SHA-256 | `1E:A6:9E:91:19:B6:7F:DE:75:0A:57:09:31:8D:09:95:CF:AC:46:AF:E0:99:4D:B7:D1:D9:86:41:E0:66:F9:16` |

Environment yang dibaca `app/build.gradle.kts`:

| Env var | Isi |
| --- | --- |
| `KEYSTORE_PATH` | path file `.jks` |
| `KEYSTORE_PASSWORD` | password store **dan** key |
| `KEY_ALIAS` | `fuwanime` |

## 2. Build

```bash
export ANDROID_HOME=/root/android-sdk
export KEYSTORE_PATH=$PWD/keystore/kagenime-release.jks
export KEYSTORE_PASSWORD=kagenime123
export KEY_ALIAS=fuwanime

./gradlew :app:assembleRelease -PabiSplits=false   # APK release bertanda tangan
./gradlew :app:assembleDebug                       # APK debug, tanpa key
```

Hasil:
- `app/build/outputs/apk/release/app-release.apk`
- `app/build/outputs/apk/debug/app-debug.apk`

## 3. Login Google

Isi `GOOGLE_OAUTH_CLIENT_ID` (atau `googleOAuthClientId` di Gradle property) sebelum build.
Panduan lengkap: `docs/google-login.md`.

## 4. Workflow GitHub

- `.github/workflows/build.yml` — tes + build universal APK setiap push ke `main`; artifact
  `kagenime.apk`.
- `.github/workflows/release.yml` — dipicu tag `v*` atau manual; membuat GitHub Release dengan
  aset `kagenime.apk`.

Rilis versi ini: **v1.0.0**.
