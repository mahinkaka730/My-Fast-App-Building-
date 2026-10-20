package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.firebase.ChatRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertFailsWith

class ChatRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun saveMessage_authenticatedOwner_createsDocumentAndReturnsId() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = ChatRepository(firestore)

    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.saveMessage(uid, "user", "Hello Kaka AI")
    }
    assertTrue(result.isSuccess)
    val messageId = result.getOrThrow()
    assertNotNull(messageId)
    assertTrue(messageId.isNotEmpty())
  }

  @Test
  fun observeMessages_authenticatedOwner_emitsMessages() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = ChatRepository(firestore)
    val messageId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.saveMessage(uid, "user", "Observing message").getOrThrow()
    }

    val emitted = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeMessages(uid).first { list -> list.any { it.id == messageId } }
    }
    assertTrue(emitted.any { it.id == messageId })
  }

  @Test
  fun observeMessages_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repository = ChatRepository(firestore)

    val exception = assertFailsWith<FirebaseFirestoreException> {
      withTimeout(FLOW_TIMEOUT_MS) {
        repository.observeMessages("unauthenticated_uid").first()
      }
    }
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exception.code)
  }

  @Test
  fun createProfile_authenticatedOwner_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = ChatRepository(firestore)

    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createOrUpdateProfile(uid, "Alice", ALICE_EMAIL)
    }
    assertTrue(result.isSuccess)
  }

  @Test
  fun clearAllMessages_authenticatedOwner_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = ChatRepository(firestore)
    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.saveMessage(uid, "user", "To be cleared").getOrThrow()
    }

    val clearResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.clearAllMessages(uid)
    }
    assertTrue(clearResult.isSuccess)

    val remaining = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeMessages(uid).first()
    }
    assertTrue(remaining.isEmpty())
  }

  private companion object {
    const val ALICE_EMAIL = "alice@test.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
