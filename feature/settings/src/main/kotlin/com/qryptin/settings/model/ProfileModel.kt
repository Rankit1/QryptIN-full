package com.qryptin.settings.model

data class Profile(
    val displayName : String  = "",
    val username    : String  = "",
    val bio         : String  = "",
    val email       : String  = "",
    val avatarUri   : String? = null,
)

data class EditProfileUiState(
    val profile      : Profile  = Profile(),
    val isLoading    : Boolean  = true,
    val isSaving     : Boolean  = false,
    val saveError    : String?  = null,
    val saveSuccess  : Boolean  = false,
)
