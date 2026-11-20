package com.fuwanime.ui.home

import com.fuwanime.R
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Visibility
import com.fuwanime.data.ProfileUi
import com.fuwanime.ui.theme.KageColors
import com.fuwanime.util.typeLabel
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.res.stringResource
import com.fuwanime.ui.settings.rememberRestoreFlow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material.icons.rounded.Restore
import com.fuwanime.ui.components.FuwaSnackbarHost
import com.fuwanime.ui.components.IconWithTooltip
import androidx.compose.foundation.layout.heightIn
import com.fuwanime.ui.components.LocalNavBarPadding
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SystemUpdateAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.fuwanime.ui.theme.PlusJakarta
import com.fuwanime.ui.components.ImageScrim
import com.fuwanime.ui.components.Pill
import com.fuwanime.ui.components.TagChip
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.LocalIndication
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import com.fuwanime.ui.motion.Motion
import com.fuwanime.ui.motion.sharedPosterKey
import com.fuwanime.ui.motion.sharedKey
import com.fuwanime.ui.motion.pressScale
import com.fuwanime.ui.motion.OpenAnime
import com.fuwanime.data.Anime
import com.fuwanime.data.UpdateRepository
import com.fuwanime.data.local.MangaHistoryEntity
import com.fuwanime.data.local.WatchHistoryEntity
import com.fuwanime.ui.components.ContinueWatchingCard
import com.fuwanime.ui.components.ErrorState
import com.fuwanime.ui.components.NetImage
import com.fuwanime.ui.components.RefreshHairline
import com.fuwanime.ui.components.PosterRow
import com.fuwanime.ui.components.SectionHeader
import com.fuwanime.ui.components.SkeletonBox
import com.fuwanime.util.airTime
import com.fuwanime.util.compactNumber
import com.fuwanime.util.relativeTime
import com.fuwanime.util.fuwaViewModel
import kotlinx.coroutines.delay


@Composable
fun HomeRoute(
    onAnimeClick: OpenAnime,
    onContinue: (WatchHistoryEntity) -> Unit,
    onMangaContinue: (MangaHistoryEntity) -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onSchedule: () -> Unit,
    onExplore: (genre: String?) -> Unit,
    onHistory: () -> Unit = {},
) {
    val vm: HomeViewModel = fuwaViewModel { c, _ ->
        HomeViewModel(
            c.animeRepository, c.libraryRepository, c.updateRepository, c.settingsStore,
            c.networkStatus, c.backupRepository, c.mangaLibrary, c.mangaRepository, c.appInstaller,
            c.profileRepository,
        )
    }
    val state by vm.state.collectAsStateWithLifecycle()
    val update by vm.update.collectAsStateWithLifecycle()
    val install by vm.install.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { vm.onPermissionResult() }
    LaunchedEffect(install) {
        if (install is HomeViewModel.Install.NeedsPermission) vm.permissionIntent()?.let { permissionLauncher.launch(it) }
    }
    val profile by vm.profile.collectAsStateWithLifecycle()
    val favoriteIds by vm.favoriteIds.collectAsStateWithLifecycle()
    val showRestore by vm.showRestorePrompt.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.onOpened() }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val restore = rememberRestoreFlow(
        vm.backup,
        onRestored = { vm.dismissRestorePrompt() },
        onMessage = { message -> scope.launch { snackbar.showSnackbar(message) } },
    )
    Box(Modifier.fillMaxSize()) {
        HomeScreen(
            state = state,
            profile = profile,
            favoriteIds = favoriteIds,
            onToggleFavorite = vm::toggleFavorite,
            todayLabel = stringResource(vm.today.full),
            onRefresh = vm::refresh,
            update = update,
            install = install,
            onInstallUpdate = vm::installUpdate,
            onDismissUpdate = vm::dismissUpdate,
            showRestore = showRestore,
            onRestore = restore,
            onDismissRestore = { vm.dismissRestorePrompt() },
            onAnimeClick = onAnimeClick,
            onContinue = onContinue,
            onMangaContinue = onMangaContinue,
            onHideContinue = vm::hideFromContinue,
            onSearch = onSearch,
            onSettings = onSettings,
            onSchedule = onSchedule,
            onExplore = onExplore,
            onHistory = onHistory,
        )
        FuwaSnackbarHost(
            snackbar,
            Modifier.align(Alignment.BottomCenter).padding(bottom = LocalNavBarPadding.current),
        )
    }
}


private val CardBg = Color(0xFF292A2C)
private val CardBorder = Color(0xFF45464A)
private val SearchBg = Color(0xFF2B2B31)
private val SearchBorder = Color(0xFF494951)
private val ChipBg = Color(0xFF3C3D41)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun HomeScreen(
    state: HomeUiState,
    profile: ProfileUi?,
    favoriteIds: Set<String>,
    onToggleFavorite: (Anime) -> Unit,
    todayLabel: String,
    onRefresh: () -> Unit,
    update: UpdateRepository.UpdateInfo?,
    install: HomeViewModel.Install,
    onInstallUpdate: () -> Unit,
    onDismissUpdate: () -> Unit,
    showRestore: Boolean,
    onRestore: () -> Unit,
    onDismissRestore: () -> Unit,
    onAnimeClick: OpenAnime,
    onContinue: (WatchHistoryEntity) -> Unit,
    onMangaContinue: (MangaHistoryEntity) -> Unit,
    onHideContinue: (HomeContinue) -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onSchedule: () -> Unit,
    onExplore: (String?) -> Unit,
    onHistory: () -> Unit,
) {
    val feed = state.feed
    var showUpdateSheet by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    // Hot Anime = the 50 most watched titles, ranked 1..50, scrolled from left to right.
    val hot = remember(feed.popular) { feed.popular.take(50) }
    val top = remember(hot) { hot.filter { it.poster.isNotBlank() || it.cover.isNotBlank() }.take(5) }

    Box(Modifier.fillMaxSize()) {
        PullToRefreshBox(isRefreshing = feed.refreshing, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 96.dp + LocalNavBarPadding.current),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(key = "account") {
                    AccountCard(
                        profile = profile,
                        unread = feed.latest.size,
                        onBell = { showNotifications = true },
                        modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
                item(key = "search") {
                    SearchBar(onSearch, Modifier.padding(horizontal = 16.dp))
                }
                item(key = "top") {
                    Spacer(Modifier.height(16.dp))
                    val heroLoading = feed.loading && top.isEmpty()
                    Crossfade(heroLoading, animationSpec = tween(Motion.Medium), label = "hero") { loading ->
                        if (loading) {
                            SkeletonBox(
                                Modifier.padding(horizontal = 16.dp).fillMaxWidth().aspectRatio(2.2f),
                                shape = RoundedCornerShape(28.dp),
                            )
                        } else if (top.isNotEmpty()) {
                            TopPager(top) { onAnimeClick(it, "home-hero") }
                        }
                    }
                }

                if (update != null) {
                    item(key = "updateBanner") { UpdateBanner(update, onClick = { showUpdateSheet = true }, onDismiss = onDismissUpdate) }
                }
                if (showRestore) {
                    item(key = "restoreBanner") { RestoreBanner(onRestore, onDismissRestore) }
                }
                if (feed.error != null && feed.popular.isEmpty()) {
                    item { ErrorState(feed.error, onRefresh) }
                }

                if (state.continueItems.isNotEmpty()) {
                    item(key = "history") {
                        KageSection(stringResource(R.string.kage_recent), stringResource(R.string.kage_see_more), onHistory) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(
                                    state.continueItems,
                                    key = {
                                        when (it) {
                                            is HomeContinue.Anime -> "a-" + it.entry.episodeId
                                            is HomeContinue.Manga -> "m-" + it.entry.chapterId
                                        }
                                    },
                                ) { item ->
                                    when (item) {
                                        is HomeContinue.Anime -> HistoryCard(item.entry) { onContinue(item.entry) }
                                        is HomeContinue.Manga -> MangaContinueCard(
                                            item.entry,
                                            onClick = { onMangaContinue(item.entry) },
                                            onHide = { onHideContinue(item) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (feed.loading || hot.isNotEmpty()) {
                    item(key = "hot") {
                        KageSection(stringResource(R.string.kage_hot), stringResource(R.string.kage_see_rank), { onExplore(null) }) {
                            if (hot.isEmpty()) {
                                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(4) { SkeletonBox(Modifier.width(150.dp).height(260.dp), shape = RoundedCornerShape(20.dp)) }
                                }
                            } else {
                                HotRow(hot) { onAnimeClick(it, "home-popular") }
                            }
                        }
                    }
                }

                if (feed.loading || feed.latest.isNotEmpty()) {
                    item(key = "latest") {
                        KageSection(stringResource(R.string.kage_new_update), null, null) {
                            val releases = remember(feed.latest) { feed.latest.associateBy { it.anime.id } }
                            val res = LocalContext.current.resources
                            PosterRow(feed.latest.map { it.anime }, feed.loading, { onAnimeClick(it, "home-latest") }, origin = "home-latest", isNew = { true }, caption = { a ->
                                releases[a.id]?.let { r ->
                                    listOf(r.episodeLabel.trim(), relativeTime(res, r.updatedAt))
                                        .filter { it.isNotBlank() }
                                        .joinToString(" · ")
                                        .ifBlank { null }
                                }
                            })
                        }
                    }
                }
            }
        }

        // Round white settings button, bottom-right, like the design.
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp + LocalNavBarPadding.current)
                .size(56.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onSettings),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Settings, stringResource(R.string.browse_settings), tint = Color(0xFF161719), modifier = Modifier.size(30.dp))
        }
    }

    if (showUpdateSheet && update != null) {
        UpdateSheet(update, install, onInstall = onInstallUpdate, onDismiss = { showUpdateSheet = false })
    }
    if (showNotifications) {
        NotificationsSheet(feed.latest, onDismiss = { showNotifications = false }, onOpen = { showNotifications = false; onAnimeClick(it, "home-bell") })
    }
}

/** Account card: avatar, name, level chip, bell, and the EXP bar. */
@Composable
private fun AccountCard(profile: ProfileUi?, unread: Int, onBell: () -> Unit, modifier: Modifier = Modifier) {
    val name = profile?.name?.takeIf { it.isNotBlank() } ?: stringResource(R.string.kage_default_user)
    val level = profile?.level ?: 1
    val xp = profile?.xp ?: 0L
    val start = profile?.levelStartXp ?: 0L
    val next = profile?.nextLevelXp
    val fraction = if (next != null && next > start) ((xp - start).toFloat() / (next - start)).coerceIn(0f, 1f) else 1f
    val nf = remember { java.text.NumberFormat.getInstance(java.util.Locale("id", "ID")) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(60.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(54.dp).clip(CircleShape).background(KageColors.Blue), contentAlignment = Alignment.Center) {
                        Text(name.take(1).uppercase(), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Column(Modifier.weight(1f).padding(start = 16.dp)) {
                    Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.kage_level, level),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE6E1E9),
                        modifier = Modifier.clip(RoundedCornerShape(50)).background(Color(0xFF3F3E43)).padding(horizontal = 14.dp, vertical = 5.dp),
                    )
                }
                Box(
                    Modifier.size(60.dp).clip(CircleShape).background(ChipBg).clickable(onClick = onBell),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Notifications, stringResource(R.string.kage_notifications), tint = Color.White, modifier = Modifier.size(30.dp))
                    if (unread > 0) {
                        Box(Modifier.align(Alignment.TopEnd).padding(14.dp).size(10.dp).clip(CircleShape).background(KageColors.Red))
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.kage_exp), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFFCFC9D5))
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp).height(8.dp).clip(CircleShape),
                    color = KageColors.Blue,
                    trackColor = Color(0xFF4B4C50),
                    drawStopIndicator = {},
                    gapSize = 0.dp,
                )
                Text(
                    if (next != null) "${nf.format(xp)} / ${nf.format(next)}" else nf.format(xp),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFCFC9D5),
                )
            }
        }
    }
}

/** Full-width search field (the minigame button is gone, so it runs all the way to the right edge). */
@Composable
private fun SearchBar(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(20.dp),
        color = SearchBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, SearchBorder),
    ) {
        Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Search, null, tint = Color(0xFFCFC9D5), modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.kage_search_hint), style = MaterialTheme.typography.titleMedium, color = Color(0xFF9A9AA0))
        }
    }
}

/** The #1..#5 most watched titles, one big card each, with page dots. Auto-advances every 6 s. */
@Composable
private fun TopPager(items: List<Anime>, onOpen: (Anime) -> Unit) {
    val pager = rememberPagerState { items.size }
    LaunchedEffect(pager, items.size) {
        while (items.size > 1) {
            delay(6_000)
            if (!pager.isScrollInProgress) pager.animateScrollToPage((pager.currentPage + 1) % items.size)
        }
    }
    val nf = remember { java.text.NumberFormat.getInstance(java.util.Locale("id", "ID")) }
    Column {
        HorizontalPager(
            state = pager,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
            beyondViewportPageCount = 1,
        ) { page ->
            val anime = items[page]
            val press = remember { MutableInteractionSource() }
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(2.2f)
                    .pressScale(press)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF222327))
                    .clickable(interactionSource = press, indication = null) { onOpen(anime) },
            ) {
                NetImage(anime.backdrop, Modifier.fillMaxSize(), anime.title)
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(0.35f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.85f)),
                    ),
                )
                Text(
                    "#${page + 1}",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 8.dp),
                )
                Row(
                    Modifier.align(Alignment.TopEnd).padding(12.dp).clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.7f)).padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Visibility, null, tint = Color.White, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(nf.format(anime.views), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
                Text(
                    anime.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                )
            }
        }
        Row(
            Modifier.align(Alignment.CenterHorizontally).padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            repeat(items.size) { i ->
                Box(
                    Modifier.size(11.dp).clip(CircleShape)
                        .background(if (pager.currentPage == i) KageColors.Blue else Color(0xFF7B7B83)),
                )
            }
        }
    }
}

/** Section title (bold, large) with an optional rounded "Lihat Lainnya >" chip on the right. */
@Composable
private fun KageSection(title: String, action: String?, onAction: (() -> Unit)?, content: @Composable () -> Unit) {
    Column(Modifier.padding(top = 24.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color.White, modifier = Modifier.weight(1f))
            if (action != null && onAction != null) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF292A2C))
                        .border(1.dp, CardBorder, RoundedCornerShape(50))
                        .clickable(onClick = onAction)
                        .padding(start = 16.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(action, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color(0xFFCFC9D5))
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = Color(0xFFCFC9D5), modifier = Modifier.size(20.dp))
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        content()
    }
}

/** Small history card: thumbnail with "Eps N", a thin progress line and the title below. */
@Composable
private fun HistoryCard(entry: WatchHistoryEntity, onClick: () -> Unit) {
    val press = remember { MutableInteractionSource() }
    Column(Modifier.width(140.dp).pressScale(press).clickable(interactionSource = press, indication = null, onClick = onClick)) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.8f).clip(RoundedCornerShape(16.dp))) {
            NetImage(entry.episodeThumbnail.ifBlank { entry.animeCover.ifBlank { entry.animePoster } }, Modifier.fillMaxSize(), entry.animeTitle)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.7f))))
            Text(
                stringResource(R.string.kage_eps_short, entry.episodeNumber),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 10.dp, bottom = 8.dp),
            )
        }
        LinearProgressIndicator(
            progress = { entry.progress },
            modifier = Modifier.fillMaxWidth().padding(top = 3.dp).height(3.dp).clip(CircleShape),
            color = KageColors.Blue,
            trackColor = Color(0xFF4B4C50),
            drawStopIndicator = {},
            gapSize = 0.dp,
        )
        Text(
            entry.animeTitle,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFE6E1E9),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/** Ranked 1..50 row: yellow rank tab, poster, views and title; scrolls left to right. */
@Composable
private fun HotRow(items: List<Anime>, onClick: (Anime) -> Unit) {
    val res = LocalContext.current.resources
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(items, key = { _, a -> a.id }) { index, anime ->
            val press = remember { MutableInteractionSource() }
            Column(
                Modifier
                    .width(158.dp)
                    .pressScale(press)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E1F23))
                    .clickable(interactionSource = press, indication = null) { onClick(anime) },
            ) {
                Box {
                    NetImage(
                        anime.poster.ifBlank { anime.cover },
                        Modifier.fillMaxWidth().aspectRatio(2f / 3f).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 14.dp, bottomEnd = 14.dp)),
                        anime.title,
                    )
                    Text(
                        "#${index + 1}",
                        color = Color(0xFF161719),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(topStart = 20.dp, bottomEnd = 16.dp))
                            .background(KageColors.Yellow)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }
                Row(Modifier.padding(start = 10.dp, end = 10.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Visibility, null, tint = Color(0xFF9A9AA0), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.kage_views, compactNumber(res, anime.views)), style = MaterialTheme.typography.bodySmall, color = Color(0xFF9A9AA0), maxLines = 1)
                }
                Text(
                    anime.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2,
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 12.dp),
                )
            }
        }
    }
}

/** Bell sheet: the newest episode releases, tap to open the anime. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsSheet(releases: List<com.fuwanime.data.LatestRelease>, onDismiss: () -> Unit, onOpen: (Anime) -> Unit) {
    val res = LocalContext.current.resources
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFF222327)) {
        Text(stringResource(R.string.kage_notifications), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
        if (releases.isEmpty()) {
            Text(stringResource(R.string.kage_notif_empty), color = Color(0xFF9A9AA0), modifier = Modifier.padding(20.dp))
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                items(releases, key = { it.anime.id + it.episodeLabel }) { r ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onOpen(r.anime) }.padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        NetImage(r.anime.poster, Modifier.size(width = 48.dp, height = 64.dp).clip(RoundedCornerShape(10.dp)), r.anime.title)
                        Column(Modifier.padding(start = 14.dp)) {
                            Text(r.anime.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                listOf(r.episodeLabel.trim(), relativeTime(res, r.updatedAt)).filter { it.isNotBlank() }.joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9A9AA0),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Tonal update banner: a primaryContainer card, whole card taps through to the release notes. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateBanner(info: UpdateRepository.UpdateInfo, onClick: () -> Unit, onDismiss: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.padding(horizontal = 20.dp).padding(top = 12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.SystemUpdateAlt, null, modifier = Modifier.size(24.dp))
            Text(
                stringResource(R.string.browse_update_banner, info.tag.removePrefix("v")),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Rounded.Close, stringResource(R.string.browse_update_dismiss_cd), modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** Manga half of the Lanjutkan shelf: a tonal surfaceContainer card mirroring ContinueWatchingCard. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MangaContinueCard(entry: MangaHistoryEntity, onClick: () -> Unit, onHide: () -> Unit) {
    val progress = if (entry.pageCount > 0) ((entry.page + 1f) / entry.pageCount).coerceIn(0f, 1f) else 0f
    val press = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = Modifier.width(240.dp).pressScale(press),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        interactionSource = press,
    ) {
        Column(Modifier.padding(8.dp)) {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(MaterialTheme.shapes.small)) {
                NetImage(
                    entry.coverUrl.orEmpty(),
                    Modifier.fillMaxSize(),
                    "${entry.mangaTitle} — ${entry.chapterLabel}",
                    contentScale = ContentScale.Crop,
                )
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to ImageScrim.copy(alpha = 0.75f))))
                Text(
                    entry.chapterLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 10.dp, bottom = 10.dp),
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.25f),
                    drawStopIndicator = {},
                    gapSize = 0.dp,
                )
                IconButton(
                    onClick = onHide,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                        .background(ImageScrim.copy(alpha = 0.55f), CircleShape)
                        .size(28.dp),
                ) {
                    Icon(Icons.Rounded.Close, stringResource(R.string.ux_hide_continue), tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Text(
                entry.mangaTitle,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp),
            )
            Text(
                stringResource(R.string.manga_library_progress, entry.chapterLabel, entry.page + 1, entry.pageCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 4.dp),
            )
        }
    }
}

/** Tonal restore banner: tertiaryContainer card with a fully rounded filled-tonal action. */
@Composable
private fun RestoreBanner(onRestore: () -> Unit, onDismiss: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.padding(horizontal = 20.dp).padding(top = 12.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Restore, null, modifier = Modifier.size(24.dp))
                Text(
                    stringResource(R.string.browse_restore_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, stringResource(R.string.browse_restore_dismiss_cd), modifier = Modifier.size(18.dp))
                }
                FilledTonalButton(onClick = onRestore, contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(stringResource(R.string.browse_restore), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateSheet(info: UpdateRepository.UpdateInfo, install: HomeViewModel.Install, onInstall: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.SystemUpdateAlt, null, tint = MaterialTheme.colorScheme.primary)
                Text(info.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 10.dp))
            }
            Text(info.tag.removePrefix("v"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(
                if (info.notes.isBlank()) AnnotatedString(stringResource(R.string.browse_update_no_notes)) else releaseNotesText(info.notes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState()),
            )
            when (install) {
                is HomeViewModel.Install.Downloading -> {
                    Text(stringResource(R.string.browse_update_downloading), style = MaterialTheme.typography.labelLarge)
                    if (install.progress >= 0f) LinearProgressIndicator(progress = { install.progress }, modifier = Modifier.fillMaxWidth())
                    else LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                else -> {
                    if (install == HomeViewModel.Install.Failed) {
                        Text(stringResource(R.string.browse_update_failed), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = onInstall,
                        enabled = info.apkUrl != null,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 12.dp),
                    ) {
                        Icon(Icons.Rounded.SystemUpdateAlt, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.browse_update_install))
                    }
                    Text(
                        stringResource(R.string.browse_update_install_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** One feed section: emphasized shared header, 24dp rhythm between sections, 12dp down to the row. */
@Composable
private fun Section(
    title: String,
    subtitle: String? = null,
    onSeeAll: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(Modifier.padding(top = 24.dp)) {
        SectionHeader(title, subtitle = subtitle, onSeeAll = onSeeAll)
        Spacer(Modifier.height(12.dp))
        content()
    }
}