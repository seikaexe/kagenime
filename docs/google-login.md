# Login Google di Kagenime

Setelah gerbang usia **14+**, pengguna masuk dengan Google. Pemilih akun tampil **langsung di
dalam aplikasi** lewat Android Credential Manager; tidak ada token yang ditulis ke disk.

Jika pemilih akun bawaan tidak tersedia, layar login menampilkan tombol cadangan
**Masuk lewat browser** yang memakai OAuth 2.0 Authorization Code + PKCE.

## 1. Siapkan Google Cloud

Buka <https://console.cloud.google.com/apis/credentials> pada project yang sama, lalu buat:

1. **OAuth client ID → Android**
   - Package name: `com.kagenime.app`
   - SHA-1 sertifikat rilis:
     `64:3C:44:B9:63:8A:43:78:8A:71:4F:DB:BF:41:F9:66:3C:8B:4B:79`
   - SHA-1 debug (opsional, untuk build lokal):
     jalankan `./gradlew signingReport` lalu salin SHA-1 varian debug.
2. **OAuth client ID → Web application**
   - Client ID inilah yang dipakai aplikasi sebagai `serverClientId` Credential Manager.
   - Android client di atas mengizinkan package + sertifikat ini memakai project tersebut.

> Client ID Android saja cukup untuk alur browser, tetapi **pemilih akun dalam aplikasi**
> memerlukan Web client ID. Gunakan client ID Web pada secret/env bila ingin mode native.

## 2. Client ID di build

Build ini sudah memakai client ID default:

```text
684800998743-mnt3hplc0la5dcpm1mi7t8uuq1aehuib.apps.googleusercontent.com
```

Client ID aplikasi terpasang bukan rahasia, jadi aman disimpan di repo. Untuk menggantinya:

```bash
export GOOGLE_OAUTH_CLIENT_ID="xxxx.apps.googleusercontent.com"
gradle assembleRelease -PabiSplits=false
```

Atau lewat `~/.gradle/gradle.properties` / `local.properties`:

```properties
googleOAuthClientId=xxxx.apps.googleusercontent.com
```

Di GitHub Actions, tambahkan repository secret `GOOGLE_OAUTH_CLIENT_ID`; workflow sudah
meneruskannya ke step build.

## 3. Redirect URI

Google menolak skema sembarang seperti `kagenime://oauth2redirect` dengan
`Error 400: invalid_request`. Untuk aplikasi terpasang, redirect yang benar adalah kebalikan
client ID:

```text
com.googleusercontent.apps.<client-id-tanpa-.apps.googleusercontent.com>:/oauth2redirect
```

Skema ini diturunkan otomatis dari client ID saat build, jadi tidak perlu didaftarkan manual
untuk tipe client Android. `AndroidManifest.xml` memakai placeholder `googleOAuthScheme` yang
nilainya dihitung di `app/build.gradle.kts`.

## 4. Alur

- Tombol **Masuk dengan Google** memanggil Credential Manager dan menampilkan pemilih akun
  Android. Profil (nama, email, foto) disimpan lewat `SettingsStore.saveGoogleAccount(...)`.
- Bila alur native gagal, tombol **Masuk lewat browser** membuka consent screen Google.
- `MainActivity` menerima deep link balik, lalu `GoogleAuth` menukar authorization code di
  `https://oauth2.googleapis.com/token` dengan PKCE dan mengambil profil dari
  `https://openidconnect.googleapis.com/v1/userinfo`.
- Kartu akun di Beranda menampilkan nama, foto/initial, badge **14+**, dan level lokal.
