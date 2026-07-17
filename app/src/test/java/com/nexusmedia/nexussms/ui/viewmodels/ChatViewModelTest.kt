package com.nexusmedia.nexussms.ui.viewmodels

import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.MessageRepository
import com.nexusmedia.nexussms.data.repository.ScheduledMessageRepository
import com.nexusmedia.nexussms.features.rcs.RcsService
import com.nexusmedia.nexussms.features.shortcodes.ShortcodeExpansionService
import com.nexusmedia.nexussms.features.matrix.MatrixMessageService
import com.nexusmedia.nexussms.features.matrix.MatrixSyncService
import com.nexusmedia.nexussms.features.telegram.TelegramService
import com.nexusmedia.nexussms.features.discord.DiscordService
import com.nexusmedia.nexussms.features.messenger.MessengerService
import com.nexusmedia.nexussms.features.messaging.SimSelector
import com.nexusmedia.nexussms.features.messaging.ChannelRoutingManager
import com.nexusmedia.nexussms.features.smartreply.SmartReplyService
import com.nexusmedia.nexussms.data.repository.TemplateRepository
import com.nexusmedia.nexussms.security.EncryptionManager
import com.nexusmedia.nexussms.services.SmsSender
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
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
    private val contactAvatarRepository = mockk<com.nexusmedia.nexussms.data.repository.ContactAvatarRepository>(relaxed = true)
    private val themeRepository = mockk<com.nexusmedia.nexussms.data.repository.ThemeRepository>(relaxed = true)
    private val matrixMessageService = mockk<MatrixMessageService>(relaxed = true)
    private val matrixSyncService = mockk<MatrixSyncService>(relaxed = true)
    private val telegramService = mockk<TelegramService>(relaxed = true)
    private val discordService = mockk<DiscordService>(relaxed = true)
    private val messengerService = mockk<MessengerService>(relaxed = true)
    private val simSelector = mockk<SimSelector>(relaxed = true)
    private val smartReplyService = mockk<SmartReplyService>(relaxed = true)
    private val templateRepository = mockk<TemplateRepository>(relaxed = true)
    private val channelRoutingManager = mockk<ChannelRoutingManager>(relaxed = true)
    private val smsSender = mockk<SmsSender>(relaxed = true)
    private val context = mockk<android.content.Context>(relaxed = true)

    private fun setupMocks() {
        every { conversationRepository.getAllConversations() } returns flowOf(emptyList())
    }

    private fun createViewModel(): ChatViewModel {
        return ChatViewModel(
            context,
            messageRepository,
            conversationRepository,
            scheduledMessageRepository,
            shortcodeExpansionService,
            rcsService,
            encryptionManager,
            contactAvatarRepository,
            themeRepository,
            matrixMessageService,
            matrixSyncService,
            telegramService,
            discordService,
            messengerService,
            simSelector,
            smartReplyService,
            templateRepository,
            channelRoutingManager,
            smsSender
        )
    }

    @Before
    fun setup() {
        setupMocks()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadConversationTriggersMessageLoading() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        val viewModel = createViewModel()

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
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        val viewModel = createViewModel()

        val conversationId = "conv1"
        val recipientPhone = "+1234567890"
        val messageText = "Hello there"
        val expandedText = "Hello there"
        val signedText = "Hello there\n\n-- Sent from NexusSMS"

        viewModel.updateMessageText(messageText)
        assertEquals(messageText, viewModel.messageText.value)

        coEvery { shortcodeExpansionService.expandMessage(messageText) } returns expandedText
        every { encryptionManager.generateMessageSignature(expandedText) } returns signedText
        coEvery { smsSender.sendTextMessage(any(), any(), any(), any(), any(), any()) } returns Result.success("msg-id")
        coEvery { messageRepository.insertMessage(any()) } returns 1L

        viewModel.sendMessage(conversationId, recipientPhone)
        testScheduler.advanceUntilIdle()

        coVerify { smsSender.sendTextMessage(
            conversationId = conversationId,
            recipientPhone = recipientPhone,
            content = signedText,
            existingMessageId = any(),
            persistToDb = true,
            subscriptionId = any()
        ) }
    }

    @Test
    fun testUpdateMessageTextUpdatesState() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        val viewModel = createViewModel()

        viewModel.updateMessageText("New text")

        assertEquals("New text", viewModel.messageText.value)
    }

    @Test
    fun testDeleteMessageCallsRepo() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        val viewModel = createViewModel()

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
