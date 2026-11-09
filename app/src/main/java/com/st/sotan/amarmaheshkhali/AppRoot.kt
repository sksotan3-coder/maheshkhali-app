package com.st.sotan.amarmaheshkhali

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
                    NavHost(
                        navController = nav,
                        startDestination = "home",
                        modifier = Modifier.padding(padding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                vm = vm,
                                onSearch = { nav.navigate("search") },
                                onCategory = { nav.navigate("list?cat=$it&union=") },
                                onUnion = { nav.navigate("list?cat=&union=$it") },
                                onAbout = { nav.navigate("about") }
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
                                onOpen = { nav.navigate("detail/$it") },
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
                        composable("search") { SearchScreen(vm) { nav.navigate("detail/$it") } }
                        composable("favourites") { FavouritesScreen(vm) { nav.navigate("detail/$it") } }
                        composable("settings") { SettingsScreen(vm) { nav.navigate("about") } }
                        composable("about") { AboutScreen { nav.popBackStack() } }
                    }
                }
            }
        }
    }
}
