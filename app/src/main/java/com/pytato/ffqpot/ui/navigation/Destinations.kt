package com.pytato.ffqpot.ui.navigation

sealed class Destinations(val route: String) {
    object Home : Destinations("home")
    object Remuxer : Destinations("remuxer")
    object Settings : Destinations("settings")
}