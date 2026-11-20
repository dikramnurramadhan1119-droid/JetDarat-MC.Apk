package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.automirrored.outlined.Announcement
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.configureAppCheck
import com.example.ui.components.BackendConfigDialog
import com.example.ui.components.HqActionBanner
import com.example.ui.components.JdmcSplashOverlay
import com.example.ui.components.JdmcTopHeaderBar
import com.example.ui.screens.ClipperAdminHubScreen
import com.example.ui.screens.ClipperHubSubTab
import com.example.ui.screens.ClubHubScreen
import com.example.ui.screens.ClubSubTab
import com.example.ui.screens.DashboardQuickDestination
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.JdmcAiAssistantScreen
import com.example.ui.screens.MediaGalleryScreen
import com.example.ui.screens.NewsEventsScreen
import com.example.ui.screens.NewsSubTab
import com.example.ui.theme.JdmcCarbonSurface
import com.example.ui.theme.JdmcGunmetal
import com.example.ui.theme.JdmcHqTheme
import com.example.ui.theme.JdmcLightYellow
import com.example.ui.theme.JdmcObsidianBlack
import com.example.ui.viewmodel.JdmcAiViewModel
import com.example.ui.viewmodel.JdmcClipperViewModel
import com.example.ui.viewmodel.JdmcHqViewModel

enum class JdmcMainTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME(
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_tab_home"
    ),
    CLUB(
        label = "Club",
        selectedIcon = Icons.Filled.Groups,
        unselectedIcon = Icons.Outlined.Groups,
        testTag = "nav_tab_club"
    ),
    NEWS(
        label = "News",
        selectedIcon = Icons.AutoMirrored.Filled.Announcement,
        unselectedIcon = Icons.AutoMirrored.Outlined.Announcement,
        testTag = "nav_tab_news"
    ),
    MEDIA(
        label = "Media",
        selectedIcon = Icons.Filled.Collections,
        unselectedIcon = Icons.Outlined.Collections,
        testTag = "nav_tab_media"
    ),
    CLIPPER(
        label = "Clipper",
        selectedIcon = Icons.Filled.Movie,
        unselectedIcon = Icons.Outlined.Movie,
        testTag = "nav_tab_clipper"
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(JdmcObsidianBlack.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(JdmcCarbonSurface.toArgb())
        )
        setContent {
            JdmcHqTheme {
                JdmcDigitalHqRootApp()
            }
        }
    }
}

@Composable
fun JdmcDigitalHqRootApp(
    hqViewModel: JdmcHqViewModel = viewModel(),
    clipperViewModel: JdmcClipperViewModel = viewModel(),
    aiViewModel: JdmcAiViewModel = viewModel()
) {
    val hqState by hqViewModel.uiState.collectAsStateWithLifecycle()
    val clockState by hqViewModel.clockState.collectAsStateWithLifecycle()
    val clipperState by clipperViewModel.uiState.collectAsStateWithLifecycle()
    val filteredClips by clipperViewModel.filteredPublishedClips.collectAsStateWithLifecycle()
    val aiUiState by aiViewModel.uiState.collectAsStateWithLifecycle()
    val aiMessages by aiViewModel.messages.collectAsStateWithLifecycle()
    val aiSelectedImage by aiViewModel.selectedImageUri.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(JdmcMainTab.HOME) }
    var clubSubTab by rememberSaveable { mutableStateOf(ClubSubTab.MEMBERS) }
    var newsSubTab by rememberSaveable { mutableStateOf(NewsSubTab.ANNOUNCEMENTS) }
    var clipperSubTab by rememberSaveable { mutableStateOf(ClipperHubSubTab.PUBLIC_CLIPS) }
    var showBackendDialog by remember { mutableStateOf(false) }
    var showAiAssistant by rememberSaveable { mutableStateOf(false) }

    val navigateQuick: (DashboardQuickDestination) -> Unit = { destination ->
        when (destination) {
            DashboardQuickDestination.MEMBERS -> {
                clubSubTab = ClubSubTab.MEMBERS
                currentTab = JdmcMainTab.CLUB
            }
            DashboardQuickDestination.ORGANIZATION -> {
                clubSubTab = ClubSubTab.ORGANIZATION
                currentTab = JdmcMainTab.CLUB
            }
            DashboardQuickDestination.ARCHIVE -> {
                clubSubTab = ClubSubTab.ARCHIVE
                currentTab = JdmcMainTab.CLUB
            }
            DashboardQuickDestination.ANNOUNCEMENTS -> {
                newsSubTab = NewsSubTab.ANNOUNCEMENTS
                currentTab = JdmcMainTab.NEWS
            }
            DashboardQuickDestination.EVENTS -> {
                newsSubTab = NewsSubTab.EVENTS
                currentTab = JdmcMainTab.NEWS
            }
            DashboardQuickDestination.MEDIA -> {
                currentTab = JdmcMainTab.MEDIA
            }
            DashboardQuickDestination.CLIPPER_ROOM -> {
                clipperSubTab = ClipperHubSubTab.CLIPPER_ROOM
                currentTab = JdmcMainTab.CLIPPER
            }
            DashboardQuickDestination.ADMIN_PANEL -> {
                clipperSubTab = ClipperHubSubTab.ADMIN_PANEL
                currentTab = JdmcMainTab.CLIPPER
            }
            DashboardQuickDestination.ROAD_AI -> {
                showAiAssistant = true
            }
        }
    }

    if (currentTab != JdmcMainTab.HOME) {
        BackHandler {
            currentTab = JdmcMainTab.HOME
        }
    }

    if (showBackendDialog) {
        BackendConfigDialog(
            currentUrl = clipperState.backendBaseUrl,
            backendStatus = clipperState.backendStatus,
            onSaveUrl = { url -> clipperViewModel.updateBackendBaseUrl(url) },
            onRetest = { clipperViewModel.refreshConnectionAndPublicData() },
            onDismiss = { showBackendDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(JdmcObsidianBlack)
        ) {
            val useNavigationRail = maxWidth >= 600.dp

            Scaffold(
                containerColor = JdmcObsidianBlack,
                contentWindowInsets = WindowInsets.safeDrawing,
                topBar = {
                    JdmcTopHeaderBar(
                        backendStatus = clipperState.backendStatus,
                        onConfigureBackendClick = { showBackendDialog = true },
                        onOpenAiClick = { showAiAssistant = true }
                    )
                },
                bottomBar = {
                    if (!useNavigationRail) {
                        NavigationBar(
                            containerColor = JdmcCarbonSurface,
                            contentColor = JdmcLightYellow
                        ) {
                            JdmcMainTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = { Text(tab.label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = JdmcObsidianBlack,
                                        selectedTextColor = JdmcLightYellow,
                                        indicatorColor = JdmcLightYellow,
                                        unselectedIconColor = JdmcGunmetal,
                                        unselectedTextColor = JdmcGunmetal
                                    ),
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail) {
                        NavigationRail(
                            containerColor = JdmcCarbonSurface,
                            contentColor = JdmcLightYellow,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            JdmcMainTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = { Text(tab.label) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = JdmcObsidianBlack,
                                        selectedTextColor = JdmcLightYellow,
                                        indicatorColor = JdmcLightYellow,
                                        unselectedIconColor = JdmcGunmetal,
                                        unselectedTextColor = JdmcGunmetal
                                    ),
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 840.dp)
                        ) {
                            if (!hqState.bannerMessage.isNullOrBlank()) {
                                HqActionBanner(
                                    message = hqState.bannerMessage,
                                    onDismiss = hqViewModel::clearBanner,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }

                            Crossfade(
                                targetState = currentTab,
                                label = "jdmc_hq_tab_transition",
                                modifier = Modifier.weight(1f)
                            ) { tab ->
                                when (tab) {
                                    JdmcMainTab.HOME -> DashboardScreen(
                                        hqState = hqState,
                                        clock = clockState,
                                        onQuickNavigate = navigateQuick
                                    )

                                    JdmcMainTab.CLUB -> ClubHubScreen(
                                        activeSubTab = clubSubTab,
                                        onSelectSubTab = { clubSubTab = it },
                                        members = hqState.members,
                                        organizationRoles = hqState.organizationRoles,
                                        archiveEntries = hqState.archiveEntries,
                                        isOfficerMode = hqState.settings.officerModeActive,
                                        onPickAndImportImage = hqViewModel::importAvatarOrPosterImage,
                                        onSaveMember = hqViewModel::saveMember,
                                        onDeleteMember = hqViewModel::deleteMember,
                                        onSaveOrganizationRole = hqViewModel::saveOrganizationRole,
                                        onSaveArchiveEntry = hqViewModel::saveArchiveEntry,
                                        onDeleteArchiveEntry = hqViewModel::deleteArchiveEntry
                                    )

                                    JdmcMainTab.NEWS -> NewsEventsScreen(
                                        activeSubTab = newsSubTab,
                                        onSelectSubTab = { newsSubTab = it },
                                        announcements = hqState.announcements,
                                        events = hqState.events,
                                        activeMemberAlias = hqState.settings.activeMemberAlias,
                                        isOfficerMode = hqState.settings.officerModeActive,
                                        onPickAndImportImage = hqViewModel::importAvatarOrPosterImage,
                                        onSaveAnnouncement = hqViewModel::saveAnnouncement,
                                        onTogglePinAnnouncement = hqViewModel::toggleAnnouncementPin,
                                        onDeleteAnnouncement = hqViewModel::deleteAnnouncement,
                                        onSaveEvent = hqViewModel::saveEvent,
                                        onToggleEventAttendance = { event -> hqViewModel.toggleEventAttendance(event) },
                                        onToggleEventStatus = hqViewModel::toggleEventStatus,
                                        onDeleteEvent = hqViewModel::deleteEvent
                                    )

                                    JdmcMainTab.MEDIA -> MediaGalleryScreen(
                                        mediaItems = hqState.mediaItems,
                                        isImporting = hqState.isImportingMedia,
                                        activeMemberAlias = hqState.settings.activeMemberAlias,
                                        isOfficerMode = hqState.settings.officerModeActive,
                                        onSavePickedMedia = { uri, title, caption, album, onDone ->
                                            hqViewModel.savePickedMediaToGallery(
                                                pickerUri = uri,
                                                title = title,
                                                caption = caption,
                                                album = album,
                                                onComplete = onDone
                                            )
                                        },
                                        onDeleteMediaItem = hqViewModel::deleteMediaItem
                                    )

                                    JdmcMainTab.CLIPPER -> ClipperAdminHubScreen(
                                        activeSubTab = clipperSubTab,
                                        onSelectSubTab = { clipperSubTab = it },
                                        hqState = hqState,
                                        clipperState = clipperState,
                                        filteredClips = filteredClips,
                                        onConfigureBackend = { showBackendDialog = true },
                                        onQuickJumpFromAdmin = navigateQuick,
                                        onOfficerLogin = hqViewModel::loginOfficerAdmin,
                                        onOfficerLogout = hqViewModel::logoutOfficerAdmin,
                                        onSetOfficerPin = hqViewModel::updateOfficerPin,
                                        onSaveClubSettings = hqViewModel::updateClubSettings,
                                        onSearchQueryChange = clipperViewModel::setGallerySearchQuery,
                                        onPlatformSelected = clipperViewModel::setPlatformFilter,
                                        onStreamerSelected = clipperViewModel::setStreamerFilter,
                                        onRefreshPublicGallery = clipperViewModel::refreshPublicGallery,
                                        onVerifyAccessCode = { code -> clipperViewModel.verifyClipperAccessCode(code) },
                                        onValidateSession = clipperViewModel::validateClipperSessionWithServer,
                                        onLogoutClipper = clipperViewModel::logoutClipper,
                                        onRefreshSubmissions = clipperViewModel::refreshMySubmissions,
                                        onUploadThumbnail = clipperViewModel::uploadThumbnail,
                                        onSubmitClip = clipperViewModel::submitNewClip,
                                        onUpdateClip = clipperViewModel::updateOwnClip,
                                        onWithdrawClip = clipperViewModel::withdrawOwnClip,
                                        onClearClipperBanner = clipperViewModel::clearClipperBanner,
                                        onAuthenticateRemoteAdmin = clipperViewModel::authenticateAdmin,
                                        onLogoutRemoteAdmin = clipperViewModel::logoutAdmin,
                                        onRefreshRemoteAdmin = clipperViewModel::refreshAdminWorkspace,
                                        onModerateClip = clipperViewModel::moderateClip,
                                        onRotateAccessCode = clipperViewModel::rotateSharedAccessCode,
                                        onRevokeAllClipperSessions = clipperViewModel::revokeAllClipperSessions,
                                        onSaveStreamers = clipperViewModel::saveStreamerRoster,
                                        onSaveJabatanPositions = clipperViewModel::saveJabatanPositions,
                                        onClearAdminBanner = clipperViewModel::clearAdminBanner
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAiAssistant) {
            JdmcAiAssistantScreen(
                uiState = aiUiState,
                messages = aiMessages,
                selectedImageUri = aiSelectedImage,
                onSelectImage = aiViewModel::setSelectedImage,
                onSendPrompt = aiViewModel::sendPrompt,
                onResetChat = aiViewModel::clearConversation,
                onClose = { showAiAssistant = false }
            )
        }

        JdmcSplashOverlay(
            visible = hqState.isSplashVisible,
            onDismiss = hqViewModel::dismissSplash
        )
    }
}
