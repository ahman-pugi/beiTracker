package com.ahmanpg.beitracker.data.repository

import com.ahmanpg.beitracker.data.remote.model.UserSettings
import com.ahmanpg.beitracker.data.remote.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestoreRepository: FirestoreRepository
) {
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Login failed: User is null")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Google Sign-In failed: User is null")
            
            // Initialize Firestore if new user
            if (result.additionalUserInfo?.isNewUser == true) {
                val initialSettings = UserSettings(
                    userName = user.displayName ?: "User",
                    userEmail = user.email ?: "",
                    notificationsEnabled = true,
                    checkIntervalHours = 6
                )
                firestoreRepository.updateUserSettings(user.uid, initialSettings)
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, name: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Signup failed: User is null")
            
            // 1. Update Firebase Auth Profile
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profileUpdates).await()
            user.reload().await()
            
            // 2. Initialize Firestore User Data
            val initialSettings = UserSettings(
                userName = name,
                userEmail = email,
                notificationsEnabled = true,
                checkIntervalHours = 6
            )
            firestoreRepository.updateUserSettings(user.uid, initialSettings)
            
            Result.success(firebaseAuth.currentUser!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(name: String): Result<FirebaseUser> {
        return try {
            val user = firebaseAuth.currentUser ?: throw Exception("No user logged in")
            
            // Update Auth Profile
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profileUpdates).await()
            user.reload().await()
            
            // Update Firestore Profile
            firestoreRepository.updateUserSettings(user.uid, UserSettings(userName = name, userEmail = user.email ?: ""))

            Result.success(firebaseAuth.currentUser!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
