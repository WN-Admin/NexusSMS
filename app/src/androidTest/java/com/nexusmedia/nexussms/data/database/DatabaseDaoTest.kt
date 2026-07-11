package com.nexusmedia.nexussms.data.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.models.Shortcut
import com.nexusmedia.nexussms.data.models.Signature
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseDaoTest {

    private lateinit var database: NexusSMSDatabase
    private lateinit var messageDao: MessageDao
    private lateinit var conversationDao: ConversationDao
    private lateinit var shortcutDao: ShortcutDao
    private lateinit var signatureDao: SignatureDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, NexusSMSDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        messageDao = database.messageDao()
        conversationDao = database.conversationDao()
        shortcutDao = database.shortcutDao()
        signatureDao = database.signatureDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun messageDao_insertAndRetrieve() = runTest {
        val message = Message(
            id = "test_msg_1",
            conversationId = "conv1",
            content = "Hello World",
            senderPhoneNumber = "self",
            recipientPhoneNumber = "+1234567890",
            type = "TEXT",
            status = "SENT",
            timestamp = System.currentTimeMillis()
        )

        messageDao.insertMessage(message)
        val retrieved = messageDao.getMessageById("test_msg_1")

        assertNotNull(retrieved)
        assertEquals("Hello World", retrieved?.content)
        assertEquals("conv1", retrieved?.conversationId)
    }

    @Test
    fun messageDao_updateMessage() = runTest {
        val message = Message(
            id = "test_msg_2",
            conversationId = "conv1",
            content = "Original",
            senderPhoneNumber = "self",
            recipientPhoneNumber = "+1234567890",
            type = "TEXT",
            status = "SENDING"
        )
        messageDao.insertMessage(message)

        messageDao.updateMessage(message.copy(status = "DELIVERED"))
        val updated = messageDao.getMessageById("test_msg_2")

        assertEquals("DELIVERED", updated?.status)
    }

    @Test
    fun messageDao_getMessagesByConversation() = runTest {
        val msg1 = Message(
            id = "m1", conversationId = "conv1", content = "First",
            senderPhoneNumber = "self", recipientPhoneNumber = "+1",
            type = "TEXT", status = "SENT", timestamp = 1000L
        )
        val msg2 = Message(
            id = "m2", conversationId = "conv1", content = "Second",
            senderPhoneNumber = "self", recipientPhoneNumber = "+1",
            type = "TEXT", status = "SENT", timestamp = 2000L
        )
        val msg3 = Message(
            id = "m3", conversationId = "conv2", content = "Other conv",
            senderPhoneNumber = "self", recipientPhoneNumber = "+2",
            type = "TEXT", status = "SENT", timestamp = 3000L
        )

        messageDao.insertMessages(listOf(msg1, msg2, msg3))
        val conv1Messages = messageDao.getMessagesByConversation("conv1", 10, 0).first()

        assertEquals(2, conv1Messages.size)
        assertTrue(conv1Messages.all { it.conversationId == "conv1" })
    }

    @Test
    fun conversationDao_insertAndRetrieve() = runTest {
        val conversation = Conversation(
            id = "test_conv_1",
            participantPhoneNumbers = "[\"+1234567890\"]",
            displayName = "Test Contact",
            lastMessageTime = System.currentTimeMillis()
        )

        conversationDao.insertConversation(conversation)
        val conversations = conversationDao.getAllConversations().first()

        assertEquals(1, conversations.size)
        assertEquals("Test Contact", conversations[0].displayName)
    }

    @Test
    fun conversationDao_pinConversation() = runTest {
        val conversation = Conversation(
            id = "test_conv_2",
            participantPhoneNumbers = "[\"+9876543210\"]",
            displayName = "Pin Test",
            isPinned = false
        )

        conversationDao.insertConversation(conversation)
        conversationDao.updateConversation(conversation.copy(isPinned = true))

        val pinned = conversationDao.getPinnedConversations().first()
        assertEquals(1, pinned.size)
        assertTrue(pinned[0].isPinned)
    }

    @Test
    fun shortcutDao_insertAndRetrieve() = runTest {
        val shortcut = Shortcut(
            trigger = "!hello",
            expansion = "Hello, how are you?",
            description = "Greeting shortcut",
            category = "Greetings"
        )

        shortcutDao.insertShortcut(shortcut)
        val shortcuts = shortcutDao.getGlobalShortcuts().first()

        assertEquals(1, shortcuts.size)
        assertEquals("!hello", shortcuts[0].trigger)
        assertEquals("Hello, how are you?", shortcuts[0].expansion)
    }

    @Test
    fun shortcutDao_updateShortcut() = runTest {
        val shortcut = Shortcut(
            trigger = "!test",
            expansion = "Original",
            category = "General"
        )
        shortcutDao.insertShortcut(shortcut)

        val inserted = shortcutDao.getGlobalShortcuts().first().first()
        shortcutDao.updateShortcut(inserted.copy(expansion = "Updated"))

        val updated = shortcutDao.getGlobalShortcuts().first().first()
        assertEquals("Updated", updated.expansion)
    }

    @Test
    fun signatureDao_insertAndSetDefault() = runTest {
        val sig1 = Signature(
            name = "Work",
            content = "Best regards",
            isDefault = false
        )
        val sig2 = Signature(
            name = "Personal",
            content = "Cheers",
            isDefault = false
        )

        signatureDao.insertSignature(sig1)
        signatureDao.insertSignature(sig2)

        val signatures = signatureDao.getAllSignatures().first()
        assertEquals(2, signatures.size)

        val inserted1 = signatures.find { it.name == "Work" }!!
        signatureDao.clearDefaultSignature()
        signatureDao.markAsDefault(inserted1.id)

        val defaultSig = signatureDao.getDefaultSignature().first()
        assertNotNull(defaultSig)
        assertEquals("Work", defaultSig?.name)
    }
}
