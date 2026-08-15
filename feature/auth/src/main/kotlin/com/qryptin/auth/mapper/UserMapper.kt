package com.qryptin.auth.mapper

import com.qryptin.auth.local.UserEntity
import com.qryptin.auth.model.UserProfile

fun UserEntity.toDomain(): UserProfile = UserProfile(
    userId          = userId,
    qryptinId        = qryptinId,
    fullName          = fullName,
    phoneNumber        = phoneNumber,
    email                = email,
    bio                    = bio,
    profilePhotoUri         = profilePhotoUri,
    isRegistered              = isRegistered,
    isVerified                  = isVerified,
)
