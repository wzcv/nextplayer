package dev.anilbeesetti.nextplayer.feature.videopicker.screens.webdav

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anilbeesetti.nextplayer.core.model.WebDavServer
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.NextCenterAlignedTopAppBar
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun WebDavRoute(
    onNavigateUp: () -> Unit,
    onServerClick: (WebDavServer) -> Unit,
    onPlayVideo: (android.net.Uri, String?, String?) -> Unit,
    viewModel: WebDavViewModel = hiltViewModel(),
) {
    val servers by viewModel.filteredServers.collectAsStateWithLifecycle(initialValue = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val testConnectionResult by viewModel.testConnectionResult.collectAsStateWithLifecycle()
    val showHistory by viewModel.showHistory.collectAsStateWithLifecycle()
    val allHistory by viewModel.allHistory.collectAsStateWithLifecycle()

    WebDavScreen(
        servers = servers,
        historyItems = allHistory,
        showHistory = showHistory,
        searchQuery = searchQuery,
        isLoading = isLoading,
        testConnectionResult = testConnectionResult,
        onNavigateUp = {
            if (showHistory) {
                viewModel.hideHistory()
            } else {
                onNavigateUp()
            }
        },
        onServerClick = onServerClick,
        onHistoryClick = { history ->
            // Create correct URI for history playback
            val uri = viewModel.createHistoryPlaybackUri(history)
            val server = viewModel.getServerById(history.serverId)
            if (uri != null && server != null) {
                onPlayVideo(uri, server.username, server.password)
            }
        },
        onSearchQueryChange = viewModel::updateSearchQuery,
        onAddServer = viewModel::addServer,
        onUpdateServer = viewModel::updateServer,
        onDeleteServer = viewModel::deleteServer,
        onTestConnection = viewModel::testConnection,
        onClearTestResult = viewModel::clearTestConnectionResult,
        onToggleHistory = viewModel::toggleHistory,
        onRemoveFromHistory = viewModel::removeFromHistory,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WebDavScreen(
    servers: List<WebDavServer>,
    historyItems: List<dev.anilbeesetti.nextplayer.core.model.WebDavHistory>,
    showHistory: Boolean,
    searchQuery: String,
    isLoading: Boolean,
    testConnectionResult: TestConnectionResult?,
    onNavigateUp: () -> Unit,
    onServerClick: (WebDavServer) -> Unit,
    onHistoryClick: (dev.anilbeesetti.nextplayer.core.model.WebDavHistory) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAddServer: (WebDavServer) -> Unit,
    onUpdateServer: (WebDavServer) -> Unit,
    onDeleteServer: (String) -> Unit,
    onTestConnection: (WebDavServer) -> Unit,
    onClearTestResult: () -> Unit,
    onToggleHistory: () -> Unit,
    onRemoveFromHistory: (String) -> Unit,
) {
    var showAddServerDialog by rememberSaveable { mutableStateOf(false) }
    var editingServer by remember { mutableStateOf<WebDavServer?>(null) }

    Scaffold(
        topBar = {
            NextCenterAlignedTopAppBar(
                title = if (showHistory) "播放历史" else "WebDAV",
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = NextIcons.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_up),
                        )
                    }
                },
                actions = {
                    // 历史记录按钮
                    if (!showHistory) {
                        IconButton(onClick = onToggleHistory) {
                            Icon(
                                imageVector = NextIcons.History,
                                contentDescription = "查看历史记录",
                            )
                        }
                        
                        // 添加服务器按钮 - 只在非历史记录模式下显示
                        IconButton(onClick = { showAddServerDialog = true }) {
                            Icon(
                                imageVector = NextIcons.Add,
                                contentDescription = "Add Server",
                            )
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            if (showHistory) {
                // 历史记录视图
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (historyItems.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = NextIcons.History,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "暂无播放历史",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "播放过的视频会显示在这里",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(historyItems) { history ->
                            WebDavHistoryItem(
                                history = history,
                                onClick = { onHistoryClick(history) },
                                onRemove = { onRemoveFromHistory(history.id) },
                            )
                        }
                    }
                }
            } else {
                // 服务器列表视图
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search servers...") },
                    leadingIcon = {
                        Icon(
                            imageVector = NextIcons.Search,
                            contentDescription = "Search",
                        )
                    },
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (servers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = NextIcons.WebDav,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No WebDAV servers added",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap the + button to add a server",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(servers) { server ->
                            WebDavServerItem(
                                server = server,
                                onClick = { onServerClick(server) },
                                onEdit = { editingServer = server },
                                onDelete = { onDeleteServer(server.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddServerDialog) {
        AddServerDialog(
            onDismiss = {
                showAddServerDialog = false
                onClearTestResult()
            },
            onAddServer = { server ->
                onAddServer(server)
                showAddServerDialog = false
                onClearTestResult()
            },
            onTestConnection = onTestConnection,
            testConnectionResult = testConnectionResult,
            isTestingConnection = isLoading,
        )
    }

    editingServer?.let { server ->
        EditServerDialog(
            server = server,
            onDismiss = {
                editingServer = null
                onClearTestResult()
            },
            onUpdateServer = { updatedServer ->
                onUpdateServer(updatedServer)
                editingServer = null
                onClearTestResult()
            },
            onTestConnection = onTestConnection,
            testConnectionResult = testConnectionResult,
            isTestingConnection = isLoading,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebDavServerItem(
    server: WebDavServer,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = NextIcons.WebDav,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (server.isConnected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = server.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = server.url,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
                if (server.lastConnected > 0) {
                    Text(
                        text = "Last connected: ${formatTimestamp(server.lastConnected)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            Box {
                IconButton(onClick = { showOptionsMenu = true }) {
                    Icon(
                        imageVector = NextIcons.MoreVert,
                        contentDescription = "Server Options",
                    )
                }

                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            onEdit()
                            showOptionsMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = NextIcons.Edit,
                                contentDescription = null
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showDeleteDialog = true
                            showOptionsMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = NextIcons.Delete,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Server") },
            text = { Text("Are you sure you want to delete '${server.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebDavHistoryItem(
    history: dev.anilbeesetti.nextplayer.core.model.WebDavHistory,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = NextIcons.Video,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = history.fileName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                
                Text(
                    text = history.serverName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (history.fileSize > 0) {
                        Text(
                            text = formatFileSize(history.fileSize),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    
                    Text(
                        text = "• ${formatTimestamp(history.lastPlayed)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = NextIcons.Delete,
                    contentDescription = "Remove from history",
                    tint = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove from history") },
            text = { Text("Are you sure you want to remove this item from your history?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemove()
                        showDeleteDialog = false
                    },
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
    return String.format("%.1f %s", bytes / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
}


private fun formatTimestamp(timestamp: Long): String {
    // Simple timestamp formatting - you might want to use a proper date formatter
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val hours = diff / (1000 * 60 * 60)
    val days = hours / 24

    return when {
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        else -> "Recently"
    }
}
