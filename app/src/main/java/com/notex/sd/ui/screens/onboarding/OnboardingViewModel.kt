package com.notex.sd.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import com.notex.sd.core.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    val appPreferences: AppPreferences
) : ViewModel()
