package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.search.SearchSecurityValidator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `verify app name resource is Amanix`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Amanix", appName)
    }

    @Test
    fun `verify search security validator blocks SSRF and private IPs`() {
        assertFalse(SearchSecurityValidator.isSafeUrl("http://localhost:8080/secret"))
        assertFalse(SearchSecurityValidator.isSafeUrl("http://127.0.0.1/admin"))
        assertFalse(SearchSecurityValidator.isSafeUrl("http://169.254.169.254/latest/meta-data/"))
        assertFalse(SearchSecurityValidator.isSafeUrl("http://192.168.1.1/router"))
        assertTrue(SearchSecurityValidator.isSafeUrl("https://en.wikipedia.org/wiki/Artificial_intelligence"))
    }

    @Test
    fun `verify conversation and message creation in Room database`() = runBlocking {
        val convId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()
        val conv = ConversationEntity(
            id = convId,
            userId = userId,
            title = "First Amanix Chat",
            model = "gemini-3.5-flash"
        )
        db.conversationDao().insertConversation(conv)

        val retrievedConvs = db.conversationDao().getConversationsForUser(userId).first()
        assertEquals(1, retrievedConvs.size)
        assertEquals("First Amanix Chat", retrievedConvs[0].title)

        val msg = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = convId,
            role = "user",
            content = "Explain quantum computing",
            model = "gemini-3.5-flash"
        )
        db.messageDao().insertMessage(msg)

        val retrievedMsgs = db.messageDao().getMessagesForConversation(convId).first()
        assertEquals(1, retrievedMsgs.size)
        assertEquals("Explain quantum computing", retrievedMsgs[0].content)
    }
}
