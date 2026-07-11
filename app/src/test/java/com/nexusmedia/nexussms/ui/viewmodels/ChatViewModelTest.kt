package com.nexusmedia.nexussms.ui.viewmodels

import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.MessageRepository
import com.nexusmedia.nexussms.data.repository.ScheduledMessageRepository
import com.nexusmedia.nexussms.features.rcs.RcsService
import com.nexusmedia.nexussms.features.shortcodes.ShortcodeExpansionService
import com.nexusmedia.nexussms.security.EncryptionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class ChatViewModelTest {
    private val messageRepository = mockk<MessageRepository>()
    private val conversationRepository = mockk<ConversationRepository>()
    private val scheduledMessageRepository = mockk<ScheduledMessageRepository>()
    private val shortcodeExpansionService = mockk<ShortcodeExpansionService>()
    private val rcsService = mockk<RcsService>()
    private val encryptionManager = mockk<EncryptionManager>()
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ChatViewModel(
            messageRepository,
            conversationRepository,
            scheduledMessageRepository,
            shortcodeExpansionService,
            rcsService,
            encryptionManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadConversationTriggersMessageLoading() = runTest {
        val conversation = Conversation(
            id = "conv1",
            participantPhoneNumbers = "[\"+11111\"]",
            displayName = "Test"
        )
        val messages = listOf(
            Message(
                id = "m1",
                conversationId = "conv1",
                content = "Hello",
                senderPhoneNumber = "p1",
                recipientPhoneNumber = "p2",
                type = "TEXT",
                status = "SENT"
            )
        )

        every { conversationRepository.getAllConversations() } returns flowOf(listOf(conversation))
        every { messageRepository.getConversationMessages("conv1") } returns flowOf(messages)

        viewModel.loadConversation("conv1")

        assertEquals("conv1", viewModel.currentConversation.value?.id)
        assertTrue(viewModel.messages.value.isNotEmpty())
        assertEquals("Hello", viewModel.messages.value.first().content)
    }

    @Test
    fun testSendMessageInsertsMessage() = runTest {
        val conversationId = "conv1"
        val recipientPhone = "+1234567890"
        val messageText = "Hello there"
        val expandedText = "Hello there"
        val signedText = "Hello there\n\n-- Sent from NexusSMS"

        viewModel.updateMessageText(messageText)
        assertEquals(messageText, viewModel.messageText.value)

        coEvery { shortcodeExpansionService.expandMessage(messageText) } returns expandedText
        every { encryptionManager.generateMessageSignature(expandedText) } returns signedText
        coEvery { messageRepository.insertMessage(any()) } returns 1L

        viewModel.sendMessage(conversationId, recipientPhone)

        coVerify { messageRepository.insertMessage(any()) }
        assertEquals("", viewModel.messageText.value)
        assertFalse(viewModel.isSending.value)
    }

    @Test
    fun testUpdateMessageTextUpdatesState() {
        viewModel.updateMessageText("New text")

        assertEquals("New text", viewModel.messageText.value)
    }

    @Test
    fun testDeleteMessageCallsRepo() = runTest {
        val message = Message(
            id = "m1",
            conversationId = "conv1",
            content = "Delete me",
            senderPhoneNumber = "p1",
            recipientPhoneNumber = "p2",
            type = "TEXT",
            status = "SENT"
        )
        coEvery { messageRepository.deleteMessage(message) } returns Unit

        viewModel.deleteMessage(message)

        coVerify { messageRepository.deleteMessage(message) }
    }
}
