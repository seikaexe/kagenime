package com.fuwanime.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.fuwanime.data.Anime
import com.fuwanime.data.DownloadItem
import com.fuwanime.data.DownloadState
import com.fuwanime.data.Episode
import com.fuwanime.data.StreamSource
import com.fuwanime.data.local.AppSettings
import com.fuwanime.data.local.DownloadEntity
import com.fuwanime.data.local.FavoriteEntity
import com.fuwanime.data.local.MangaHistoryEntity
import com.fuwanime.data.local.ThemeMode
import com.fuwanime.data.local.WatchHistoryEntity
import com.fuwanime.data.manga.Manga
import com.fuwanime.data.manga.MangaChapter
import com.fuwanime.data.manga.MangaStatus
import com.fuwanime.ui.motion.LocalReduceMotion
import com.fuwanime.ui.theme.FuwaTheme

/**
 * Pembungkus semua @Preview: tema Yozakura + latar sesuai tema + animasi dimatikan
 * (shimmer/transisi tidak jalan di preview). Ganti [dark] = false untuk cek tema terang.
 */
@Composable
internal fun PreviewFrame(dark: Boolean = true, content: @Composable () -> Unit) {
    FuwaTheme(AppSettings(themeMode = if (dark) ThemeMode.DARK else ThemeMode.LIGHT, dynamicColor = false)) {
        CompositionLocalProvider(LocalReduceMotion provides true) {
            Surface(color = MaterialTheme.colorScheme.background) {
                Box(Modifier.fillMaxWidth()) { content() }
            }
        }
    }
}

/** Data palsu untuk preview. Poster/cover sengaja kosong: NetImage menampilkan placeholder, tanpa jaringan. */
internal object Sample {
    val anime = Anime(
        id = "a1",
        title = "Yozakura no Kimi to Tsuki",
        synopsis = "Seorang pemuda desa menemukan gerbang tersembunyi di bawah pohon sakura yang selalu mekar tengah malam. " +
            "Di baliknya, ia bertemu gadis yang mengaku sudah menunggunya selama seratus tahun.",
        alternativeTitles = "Night Sakura, You and the Moon",
        type = "TV",
        year = "2026",
        status = "ONGOING",
        views = 1_250_000,
        favorites = 48_300,
        studio = "Studio Hoshizora",
        airedStart = "2026-10-04",
        genres = listOf("Fantasy", "Romance", "Slice of Life"),
        nextAirTime = "Sabtu 23:30",
    )

    val animeList: List<Anime> = listOf(
        anime,
        anime.copy(id = "a2", title = "Kagenime Chronicle", type = "TV", year = "2025", status = "COMPLETED", genres = listOf("Action", "Mystery")),
        anime.copy(id = "a3", title = "Hoshi no Kakera", type = "Movie", year = "2024", status = "COMPLETED", genres = listOf("Sci-Fi")),
        anime.copy(id = "a4", title = "Sekai no Owari Cafe", type = "TV", year = "2026", status = "UPCOMING", genres = listOf("Comedy")),
        anime.copy(id = "a5", title = "Tsukikage Academy", type = "OVA", year = "2023", status = "COMPLETED", genres = listOf("School", "Supernatural")),
        anime.copy(id = "a6", title = "Ame no Machi", type = "TV", year = "2026", status = "ONGOING", genres = listOf("Drama")),
    )

    /** Sama dengan [animeList] tapi punya poster/cover, supaya hero Beranda (yang membuang judul tanpa poster) tampil. Host `.invalid` tidak pernah resolve, jadi tidak ada trafik nyata. */
    val animeListWithArt: List<Anime> = animeList.map { it.copy(poster = "https://example.invalid/poster.jpg", cover = "https://example.invalid/cover.jpg") }

    val episodes: List<Episode> = (1..8).map {
        Episode(
            id = "e$it",
            number = it.toString(),
            title = "Episode $it: Gerbang di Bawah Sakura",
            releaseDate = "2026-10-${(it * 3).toString().padStart(2, '0')}",
            views = 12_000L * it,
            isNew = it == 8,
        )
    }

    val sources: List<StreamSource> = listOf(
        StreamSource("https://example.com/1080.m3u8", "1080p", "Server A"),
        StreamSource("https://example.com/720.m3u8", "720p", "Server A"),
        StreamSource("https://example.com/480.m3u8", "480p", "Server B"),
    )

    val watchHistory = WatchHistoryEntity(
        episodeId = "e3",
        animeId = "a1",
        animeTitle = anime.title,
        animePoster = "",
        animeCover = "",
        episodeNumber = "3",
        episodeTitle = "Episode 3: Gerbang di Bawah Sakura",
        episodeThumbnail = "",
        positionMs = 11 * 60_000L,
        durationMs = 24 * 60_000L,
        updatedAt = 1_760_000_000_000L,
    )

    val watchHistoryList: List<WatchHistoryEntity> = listOf(
        watchHistory,
        watchHistory.copy(episodeId = "e9", animeId = "a2", animeTitle = "Kagenime Chronicle", episodeNumber = "9", positionMs = 22 * 60_000L),
        watchHistory.copy(episodeId = "e1", animeId = "a3", animeTitle = "Hoshi no Kakera", episodeNumber = "1", positionMs = 2 * 60_000L),
    )

    val favorite = FavoriteEntity(
        animeId = "a1", title = anime.title, poster = "", cover = "", type = "TV",
        status = "ONGOING", year = "2026", genres = "Fantasy,Romance", addedAt = 1_760_000_000_000L,
    )

    private val downloadEntity = DownloadEntity(
        episodeId = "e3", animeId = "a1", animeTitle = anime.title, animePoster = "", episodeNumber = "3",
        episodeTitle = "Episode 3: Gerbang di Bawah Sakura", episodeThumbnail = "", quality = "720p",
        systemId = 1L, filePath = "/storage/emulated/0/Download/e3.mp4", createdAt = 1_760_000_000_000L,
    )

    val downloadDone = DownloadItem(downloadEntity, DownloadState.DONE, 180_000_000L, 180_000_000L)
    val downloadRunning = DownloadItem(downloadEntity.copy(episodeId = "e4", episodeNumber = "4"), DownloadState.RUNNING, 60_000_000L, 180_000_000L)
    val downloadFailed = DownloadItem(downloadEntity.copy(episodeId = "e5", episodeNumber = "5"), DownloadState.FAILED, 0L, 180_000_000L)

    val manga = Manga(
        id = "m1",
        title = "Tsuki no Michishirube",
        coverUrl = null,
        description = "Penjaga mercusuar terakhir di pulau terpencil menerima surat dari seseorang yang seharusnya sudah lama tiada.",
        tags = listOf("Drama", "Mystery", "Supernatural"),
        status = MangaStatus.ONGOING,
        year = 2025,
        author = "Aoi Hoshino",
        altTitles = listOf("Moon's Guide"),
        type = "Manga",
    )

    val mangaChapters: List<MangaChapter> = (1..6).map {
        MangaChapter(
            id = "c$it", number = it.toString(), title = "Surat dari Seberang $it", language = "id",
            group = "Sakura Scan", pages = 20 + it, externalUrl = null, publishAt = 1_760_000_000_000L + it * 86_400_000L,
        )
    }

    val mangaHistory = MangaHistoryEntity(
        chapterId = "c3", mangaId = "m1", mangaTitle = manga.title, coverUrl = null,
        chapterLabel = "Ch. 3", page = 8, pageCount = 23, updatedAt = 1_760_000_000_000L,
    )
}
