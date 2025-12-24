package com.notex.sd.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.hilt.navigation.compose.hiltViewModel
import com.notex.sd.ui.screens.archive.ArchiveScreen
import com.notex.sd.ui.screens.editor.EditorScreen
import com.notex.sd.ui.screens.folder.FolderScreen
import com.notex.sd.ui.screens.home.HomeScreen
import com.notex.sd.ui.screens.onboarding.OnboardingScreen
import com.notex.sd.ui.screens.onboarding.OnboardingViewModel
import com.notex.sd.ui.screens.search.SearchScreen
import com.notex.sd.ui.screens.settings.SettingsScreen
import com.notex.sd.ui.screens.trash.TrashScreen

private const val ANIMATION_DURATION = 300

@Composable
fun NoteXNavGraph(
    navController: NavHostController,
    startDestination: Route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)) +
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing),
                    initialOffset = { it / 4 }
                )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)) +
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing),
                    targetOffset = { it / 4 }
                )
        }
    ) {
        composable<Route.Onboarding>(
            enterTransition = { fadeIn(animationSpec = tween(ANIMATION_DURATION)) },
            exitTransition = { fadeOut(animationSpec = tween(ANIMATION_DURATION)) }
        ) {
            val viewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                appPreferences = viewModel.appPreferences,
                onOnboardingComplete = {
                    navController.navigate(Route.Home) {
                        popUpTo(Route.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.Home> {
            HomeScreen(
                onNavigateToEditor = { noteId ->
                    navController.navigate(Route.Editor(noteId = noteId))
                },
                onNavigateToSearch = {
                    navController.navigate(Route.Search)
                },
                onNavigateToArchive = {
                    navController.navigate(Route.Archive)
                },
                onNavigateToTrash = {
                    navController.navigate(Route.Trash)
                },
                onNavigateToSettings = {
                    navController.navigate(Route.Settings)
                },
                onNavigateToFolder = { folderId ->
                    navController.navigate(Route.Folder(folderId))
                }
            )
        }

        composable<Route.Editor> {
            EditorScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.Search> {
            SearchScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEditor = { noteId ->
                    navController.navigate(Route.Editor(noteId = noteId))
                }
            )
        }

        composable<Route.Archive> {
            ArchiveScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEditor = { noteId ->
                    navController.navigate(Route.Editor(noteId = noteId))
                }
            )
        }

        composable<Route.Trash> {
            TrashScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEditor = { noteId ->
                    navController.navigate(Route.Editor(noteId = noteId))
                }
            )
        }

        composable<Route.Settings> {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.Folder> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Folder>()
            FolderScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSearchClick = {
                    navController.navigate(Route.Search)
                },
                onNoteClick = { noteId ->
                    navController.navigate(Route.Editor(noteId = noteId, folderId = route.folderId))
                },
                onCreateNoteClick = { folderId ->
                    navController.navigate(Route.Editor(folderId = folderId))
                }
            )
        }
    }
}
