# Login Google (OAuth) di Kagenime

Kagenime memakai **OAuth 2.0 Authorization Code + PKCE** tanpa client secret. Setelah gerbang
usia **14+**, pengguna diarahkan ke layar login Google; profil Google (nama, email, foto) disimpan
lokal dan tidak ada token yang ditulis ke disk.

## 1. Buat client ID

1. Buka <https://console.cloud.google.com/apis/credentials>.
2. **Create credentials → OAuth client ID → Android**.
3. Package name: `com.fuwanime.app`.
4. SHA-1 sertifikat rilis (dari keystore repo):

   ```bash
   keytool -list -v -keystore keystore/kagenime-release.jks \
     -storepass kagenime123 -alias fuwanime | grep SHA1
   ```

5. Salin **Client ID** (bentuknya `xxxx.apps.googleusercontent.com`).
6. Di **Authorized redirect URIs**, tambahkan `kagenime://oauth2redirect` bila konsol meminta
   redirect URI (tipe client Android biasanya memakai package + SHA-1).

## 2. Masukkan ke build

Lewat environment (dipakai workflow):

```bash
export GOOGLE_OAUTH_CLIENT_ID="xxxx.apps.googleusercontent.com"
gradle assembleRelease -PabiSplits=false
```

Atau lewat `~/.gradle/gradle.properties` / `local.properties`:

```properties
googleOAuthClientId=xxxx.apps.googleusercontent.com
```

Di GitHub Actions, tambahkan repository secret `GOOGLE_OAUTH_CLIENT_ID` dan teruskan ke step build:

```yaml
env:
  GOOGLE_OAUTH_CLIENT_ID: ${{ secrets.GOOGLE_OAUTH_CLIENT_ID }}
```

Tanpa client ID, tombol Google tetap tampil tetapi menampilkan petunjuk konfigurasi; pengguna
bisa memilih **Lanjut sebagai tamu** agar aplikasi tetap bisa dipakai.

## 3. Alur

- `MainActivity` menerima deep link `kagenime://oauth2redirect`.
- `GoogleAuth` menukar authorization code di `https://oauth2.googleapis.com/token` dengan PKCE.
- Profil diambil dari `https://openidconnect.googleapis.com/v1/userinfo` dan disimpan lewat
  `SettingsStore.saveGoogleAccount(...)`.
- Kartu akun di Beranda menampilkan nama, foto/initial, badge **14+**, dan level lokal.
