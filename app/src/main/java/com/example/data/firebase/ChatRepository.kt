package com.example.data.firebase

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ChatRepository(private val db: FirebaseFirestore) {

    // Convenience constructor: resolves named database ID from string resources
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // Real-time observation of user chat messages
    fun observeMessages(userId: String): Flow<List<FirestoreChatMessage>> {
        val path = "users/$userId/messages"
        return db.collection("users").document(userId).collection("messages")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FirestoreChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.createdAt?.toDate()?.time ?: 0L }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                throw error
            }
    }

    // Save a new message into user's messages subcollection
    suspend fun saveMessage(
        userId: String,
        sender: String,
        text: String,
        hasSources: Boolean = false,
        sourcesJson: String? = null
    ): Result<String> {
        val path = "users/$userId/messages"
        val messagesRef = db.collection("users").document(userId).collection("messages")
        return try {
            val payload = mutableMapOf<String, Any>(
                "userId" to userId,
                "sender" to sender,
                "text" to text,
                "hasSources" to hasSources,
                "createdAt" to FieldValue.serverTimestamp()
            )
            if (sourcesJson != null) {
                payload["sourcesJson"] = sourcesJson
            }

            val docRef = messagesRef.add(payload).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // Clear all messages in user's subcollection
    suspend fun clearAllMessages(userId: String): Result<Unit> {
        val path = "users/$userId/messages"
        return try {
            val snapshot = db.collection("users").document(userId).collection("messages").get().await()
            val batch = db.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // Create or update user profile
    suspend fun createOrUpdateProfile(userId: String, displayName: String, email: String): Result<Unit> {
        val docRef = db.collection("users").document(userId)
        return try {
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val data = mapOf(
                    "userId" to userId,
                    "displayName" to displayName,
                    "email" to email,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                docRef.set(data).await()
            } else {
                val updates = mapOf(
                    "displayName" to displayName,
                    "email" to email,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }
}
