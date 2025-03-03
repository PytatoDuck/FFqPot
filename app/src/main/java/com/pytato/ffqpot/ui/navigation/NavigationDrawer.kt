package com.pytato.ffqpot.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationDrawer(
    items: List<NavigationItem>,
    currentRoute: String,
    onItemClick: (NavigationItem) -> Unit,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        modifier = Modifier.clip(MaterialTheme.shapes.large),
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(240.dp)) {
                Text(
                    "FFqPot",
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 48.sp),
                    modifier = Modifier.padding(24.dp)
                )

                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, Modifier.background(Color.Transparent)) },
                        selected = isSelected,
                        onClick = {
                            onItemClick(item)
                            scope.launch { drawerState.close() }
                        },
                        shape = (MaterialTheme.shapes.small),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    )
                }
            }
        }
    ) {
        CompositionLocalProvider(
            LocalDrawerOpener provides object : DrawerOpener {
                override fun openDrawer() {
                    scope.launch { drawerState.open() }
                }
            }
        ) {
            content()
        }
    }
}


// DrawerOpener utility interface
interface DrawerOpener {
    fun openDrawer()
}

val LocalDrawerOpener = staticCompositionLocalOf<DrawerOpener> {
    error("No DrawerOpener provided")
}