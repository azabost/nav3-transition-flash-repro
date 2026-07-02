package com.azabost.transition

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.Serializable

private const val TAG = "NavFlashBug"

class MainActivity : ComponentActivity() {

    val homeEvents = Channel<Unit>(Channel.BUFFERED)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LauncherApp(homeEvents)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            Log.w(TAG, "Home button pressed")
            homeEvents.trySend(Unit)
        }
    }
}

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Home : Route

    @Serializable
    data object AppList : Route
}

data class AppInfo(
    val label: String,
    val packageName: String,
)

@Composable
fun LauncherApp(homeEvents: Channel<Unit>) {
    val backStack = remember { NavBackStack<Route>(Route.Home) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            homeEvents.receiveAsFlow().collect {
                Log.w(TAG, "Clearing backStack to [Home]")
                while (backStack.size > 1) {
                    backStack.removeLastOrNull()
                }
            }
        }
    }

    Log.w(TAG, "Compose: backStack=${backStack.toList()}")

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        predictivePopTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        entryProvider = entryProvider {
            entry<Route.Home> {
                HomeScreen(
                    backStack = backStack,
                    onOpenAppList = { backStack.add(Route.AppList) },
                )
            }
            entry<Route.AppList> {
                AppListScreen(backStack = backStack)
            }
        },
    )
}

@Composable
private fun HomeScreen(
    backStack: NavBackStack<Route>,
    onOpenAppList: () -> Unit,
) {
    val currentBackStack = backStack.toList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .drawBehind {
                Log.w(TAG, "Home draw (backStack=$currentBackStack)")
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "Home", fontSize = 32.sp)
            Text(text = "backStack=$currentBackStack", fontSize = 12.sp, color = Color.Gray)
            Button(onClick = onOpenAppList) {
                Text("Apps")
            }
        }
    }
}

@Composable
private fun AppListScreen(backStack: NavBackStack<Route>) {
    val context = LocalContext.current
    val currentBackStack = backStack.toList()

    val apps = remember {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(intent, 0)
            .map { AppInfo(label = it.loadLabel(pm).toString(), packageName = it.activityInfo.packageName) }
            .sortedBy { it.label.lowercase() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.statusBars)
            .drawBehind {
                Log.w(TAG, "AppList draw (backStack=$currentBackStack)")
            },
    ) {
        Column {
            Text(
                text = "Apps",
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp),
            )
            Text(
                text = "backStack=$currentBackStack",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(apps, key = { it.packageName }) { app ->
                    Text(
                        text = app.label,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                context.packageManager
                                    .getLaunchIntentForPackage(app.packageName)
                                    ?.let { context.startActivity(it) }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
