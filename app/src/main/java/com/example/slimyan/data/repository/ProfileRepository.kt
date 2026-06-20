package com.example.slimyan.data.repository

import com.example.slimyan.data.dao.UserProfileDao
import com.example.slimyan.data.entity.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val dao: UserProfileDao,
) {
    fun get(): Flow<UserProfile?> = dao.get()

    suspend fun save(profile: UserProfile) = dao.upsert(profile)
}
