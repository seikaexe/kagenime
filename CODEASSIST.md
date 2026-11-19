# Kagenime / FuwaStream di CodeAssist

Panduan singkat supaya proyek ini enak diedit di CodeAssist, plus daftar preview yang sudah ditambahkan.

## 1. Preview

Semua preview memakai `@Preview` biasa dan dibungkus `PreviewFrame` (tema Yozakura, animasi dimatikan) dengan data palsu dari `Sample`.

- Pembungkus + data contoh: `app/src/main/java/com/fuwanime/ui/preview/PreviewSamples.kt`
- Preview ada di bagian paling bawah tiap file UI, di bawah penanda `// ---- Preview ... ----`
- Untuk cek tema terang: `PreviewFrame(dark = false) { ... }`
- Poster/cover sengaja kosong (placeholder tonal). Kalau mau gambar tampil di preview, isi `poster`/`cover` di `Sample`.

### Cakupan
Sudah ada preview: komponen bersama (PosterCard, PosterRow, Pill, TagChip, SectionHeader, ContinueWatchingCard, MessageState, ErrorState, snackbar, GenreSheet, indikator jaringan), Beranda (lengkap, memuat, error, hero, banner, sheet update), Detail anime (header, info, baris episode, tombol unduh, dialog), Library (riwayat, favorit, unduhan, kartu pekan), Jadwal, Statistik, Profil, Pengaturan (baris), Legal + gerbang usia, Manga (detail, sel chapter, pustaka, ubin genre, pembaca, sheet), Pemutar (tombol, overlay, terkunci, akhir episode, sheet pengaturan).

### Belum ada preview (butuh refactor)
Composable ini menerima `ViewModel` / `ExoPlayer` / `PlayerUiState` langsung, jadi tidak bisa dipanggil dengan data palsu tanpa mengubah kode produksi:
`DetailScreen`, `EpisodeHeader`, `ExploreScreen`, `FilterBar`, `ResultsHeader`, `ResultsGrid`, `MangaFilterBar`, `MangaResultsGrid`, semua `*Route`, `MangaLibraryTab`, `MangaTabScreen`, `PlayerScreen` (`VideoSurface`, `Controls`, `EpisodeInfo`, `DownloadRow`), `FuwaNavHost`, widget Glance.
Cara memperbaikinya: pecah tiap `XxxRoute` jadi `XxxRoute` (ambil VM) + `XxxContent(state, onXxx...)` (stateless), lalu beri `@Preview` ke `XxxContent`. Pola ini sudah dipakai `HomeScreen`.

## 2. Catatan build untuk CodeAssist

CodeAssist punya sistem build sendiri dan tidak menjalankan Gradle penuh; proyek Gradle hanya didukung sebagian. Build file proyek ini cukup berat:

| Bagian | Lokasi | Catatan |
|---|---|---|
| AGP 9.1.1, Gradle 9.3.1, compileSdk/targetSdk 37 | `gradle/libs.versions.toml`, `gradle-wrapper.properties` | versi sangat baru |
| KSP (Room compiler, Moshi codegen) | `app/build.gradle.kts` | butuh annotation processing |
| Modul `:baselineprofile` + plugin | `settings.gradle.kts`, `app/build.gradle.kts` | modul test khusus perangkat/emulator |
| `foojay-resolver-convention` | `settings.gradle.kts` | download JDK otomatis |
| Blok signing release, `splits.abi`, `lint` | `app/build.gradle.kts` | tidak perlu untuk edit/preview |

Kalau CodeAssist gagal mengimpor, salinan "ringan" untuk editing saja biasanya cukup: hapus `include(":baselineprofile")`, plugin `androidx.baselineprofile` + `baselineProfile(...)`, plugin foojay, blok `signingConfigs`/`splits`/`lint`. Jangan commit perubahan itu ke repo utama; CI (`.github/workflows`) masih mengandalkan konfigurasi aslinya.

Belum diuji di CodeAssist: lingkungan ini tidak punya Android SDK / Kotlin compiler, jadi preview hanya diperiksa statis (nama parameter, import).
