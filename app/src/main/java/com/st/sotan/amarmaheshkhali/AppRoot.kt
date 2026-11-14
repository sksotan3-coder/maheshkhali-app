package com.st.sotan.amarmaheshkhali

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.st.sotan.amarmaheshkhali.ui.AboutScreen
import com.st.sotan.amarmaheshkhali.ui.AppTheme
import com.st.sotan.amarmaheshkhali.ui.DetailScreen
import com.st.sotan.amarmaheshkhali.ui.FavouritesScreen
import com.st.sotan.amarmaheshkhali.ui.HomeScreen
import com.st.sotan.amarmaheshkhali.ui.ListScreen
import com.st.sotan.amarmaheshkhali.ui.LocalLang
import com.st.sotan.amarmaheshkhali.ui.SearchScreen
import com.st.sotan.amarmaheshkhali.ui.SettingsScreen
import com.st.sotan.amarmaheshkhali.ui.tr

private data class Tab(val route: String, val bn: String, val en: String, val icon: ImageVector)

private val TABS = listOf(
    Tab("home", "হোম", "Home", Icons.Default.Home),
    Tab("search", "সার্চ", "Search", Icons.Default.Search),
    Tab("favourites", "প্রিয়", "Favourites", Icons.Default.Favorite),
    Tab("settings", "সেটিংস", "Settings", Icons.Default.Settings)
)

@Composable
fun AppRoot(vm: AppViewModel) {
    val lang by vm.lang.collectAsState()
    val theme by vm.theme.collectAsState()

    CompositionLocalProvider(LocalLang provides lang) {
        AppTheme(theme) {
            Surface {
                val nav = rememberNavController()
                val backStack by nav.currentBackStackEntryAsState()
                val current = backStack?.destination?.route

                Scaffold(
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        NavigationBar {
                            TABS.forEach { t ->
                                NavigationBarItem(
                                    selected = current == t.route,
                                    onClick = {
                                        nav.navigate(t.route) {
                                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(t.icon, contentDescription = null) },
                                    label = { Text(tr(t.bn, t.en)) }
                                )
                            }
                        }
                    }
                ) { padding ->
                    Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    NavHost(
                        navController = nav,
                        startDestination = "home",
                        modifier = Modifier.fillMaxHeight().widthIn(max = 900.dp)
                    ) {
                        composable("home") {
                            HomeScreen(
                                vm = vm,
                                onSearch = { nav.go("search") },
                                onCategory = { nav.go("list?cat=$it&union=") },
                                onUnion = { nav.go("list?cat=&union=$it") },
                                onAbout = { nav.go("about") }
                            )
                        }
                        composable(
                            route = "list?cat={cat}&union={union}",
                            arguments = listOf(
                                navArgument("cat") { type = NavType.StringType; defaultValue = "" },
                                navArgument("union") { type = NavType.StringType; defaultValue = "" }
                            )
                        ) { entry ->
                            ListScreen(
                                vm = vm,
                                cat = entry.arguments?.getString("cat") ?: "",
                                unionId = entry.arguments?.getString("union") ?: "",
                                onOpen = { nav.go("detail/$it") },
                                onBack = { nav.popBackStack() }
                            )
                        }
                        composable(
                            route = "detail/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.StringType })
                        ) { entry ->
                            DetailScreen(
                                vm = vm,
                                id = entry.arguments?.getString("id") ?: "",
                                onBack = { nav.popBackStack() }
                            )
                        }
                        composable("search") { SearchScreen(vm) { nav.go("detail/$it") } }
                        composable("favourites") { FavouritesScreen(vm) { nav.go("detail/$it") } }
                        composable("settings") { SettingsScreen(vm) { nav.go("about") } }
                        composable("about") { AboutScreen { nav.popBackStack() } }
                    }
                    }
                }
            }
        }
    }
}

/** Navigate without stacking duplicates when the user double-taps. */
private fun NavController.go(route: String) = navigate(route) { launchSingleTop = true }
