package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.PpiViewModel

enum class ScreenNav(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    STUDENTS("Anak Inklusi", Icons.Default.Groups),
    WORKSPACE("PPI", Icons.Default.AutoStories),
    SCHOOL("Sekolah", Icons.Default.AccountBalance),
    USERS("Pengguna", Icons.Default.ManageAccounts)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: PpiViewModel
) {
    var currentScreen by remember { mutableStateOf(ScreenNav.DASHBOARD) }
    var showPrintPreview by remember { mutableStateOf(false) }
    var showRolePicker by remember { mutableStateOf(false) }

    val school by viewModel.school.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // School brand & logo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PpiGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "PPI",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }

                        Column {
                            Text(
                                text = school?.name ?: "Sistem PPI",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = school?.tagline ?: "Sekolah Inklusi",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // User role switcher badge
                    Surface(
                        modifier = Modifier
                            .clickable { showRolePicker = true }
                            .testTag("role_switcher_top_bar"),
                        shape = RoundedCornerShape(20.dp),
                        color = PpiGreenSoft
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PpiGreen)
                            )
                            Text(
                                text = currentUser?.role ?: "GPK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = PpiGreenDark
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = PpiGreenDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                ScreenNav.entries.forEach { item ->
                    val selected = currentScreen == item
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentScreen = item },
                        icon = {
                            BadgedBox(badge = {
                                if (item == ScreenNav.DASHBOARD && unreadCount > 0) {
                                    Badge(containerColor = PpiOrange) { Text("$unreadCount") }
                                }
                            }) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected) PpiGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) PpiGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = PpiGreenSoft
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                ScreenNav.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToStudents = { currentScreen = ScreenNav.STUDENTS },
                    onNavigateToPpi = { studentId ->
                        if (studentId != null) viewModel.selectStudent(studentId)
                        currentScreen = ScreenNav.WORKSPACE
                    },
                    onNavigateToSchool = { currentScreen = ScreenNav.SCHOOL },
                    onOpenPrintDialog = { showPrintPreview = true }
                )
                ScreenNav.STUDENTS -> StudentsScreen(
                    viewModel = viewModel,
                    onOpenPpiWorkspace = { studentId ->
                        viewModel.selectStudent(studentId)
                        currentScreen = ScreenNav.WORKSPACE
                    }
                )
                ScreenNav.WORKSPACE -> PpiWorkspaceScreen(
                    viewModel = viewModel,
                    onOpenPrintPreview = { showPrintPreview = true }
                )
                ScreenNav.SCHOOL -> SchoolScreen(viewModel = viewModel)
                ScreenNav.USERS -> UserManagementScreen(viewModel = viewModel)
            }
        }
    }

    // Role Picker Modal Bottom Sheet / Dialog
    if (showRolePicker) {
        AlertDialog(
            onDismissRequest = { showRolePicker = false },
            title = { Text("Ganti Peran Aktif", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pilih akun untuk menguji workflow sesuai wewenang:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    allUsers.forEach { user ->
                        val isSelected = currentUser?.userId == user.userId
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchUser(user)
                                    showRolePicker = false
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PpiGreenSoft else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RadioButton(selected = isSelected, onClick = {
                                    viewModel.switchUser(user)
                                    showRolePicker = false
                                })
                                Column {
                                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${user.role} • @${user.username}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRolePicker = false }) { Text("Tutup") }
            }
        )
    }

    // Printable Document A4 Preview Modal
    if (showPrintPreview) {
        PrintPreviewDialog(
            viewModel = viewModel,
            onDismiss = { showPrintPreview = false }
        )
    }
}
