package com.azabost.transition

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

private const val TAG = "NavFlashBug"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TransitionApp()
        }
    }
}

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object ScreenA : Route

    @Serializable
    data object ScreenB : Route
}

@Composable
fun TransitionApp() {
    val backStack = remember { NavBackStack<Route>(Route.ScreenA) }

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
            entry<Route.ScreenA> {
                ScreenContent(
                    name = "Screen A",
                    backStack = backStack,
                    color = Color.Red,
                    buttonText = "Go to Screen B",
                    onNavigate = { backStack.add(Route.ScreenB) },
                )
            }
            entry<Route.ScreenB> {
                ScreenContent(
                    name = "Screen B",
                    backStack = backStack,
                    color = Color.Blue,
                    buttonText = "Go back to Screen A",
                    onNavigate = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}

@Composable
private fun ScreenContent(
    name: String,
    backStack: NavBackStack<Route>,
    color: Color,
    buttonText: String,
    onNavigate: () -> Unit,
) {
    val currentBackStack = backStack.toList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .drawBehind {
                Log.w(TAG, "$name draw (backStack=$currentBackStack)")
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = name,
                fontSize = 32.sp,
                color = Color.White,
            )
            Text(
                text = "backStack=$currentBackStack",
                fontSize = 14.sp,
                color = Color.White,
            )
            Button(onClick = onNavigate) {
                Text(buttonText)
            }
        }
    }
}
