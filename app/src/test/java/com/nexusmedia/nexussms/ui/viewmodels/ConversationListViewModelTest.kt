package com.nexusmedia.nexussms.ui.viewmodels

import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.repository.ConversationRepository
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

class ConversationListViewModelTest {
    private val conversationRepository = mockk<ConversationRepository>()
    private val smsImporter = mockk<com.nexusmedia.nexussms.data.repository.SmsImporter>(relaxed = true)
    private val contactAvatarRepository = mockk<com.nexusmedia.nexussms.data.repository.ContactAvatarRepository>(relaxed = true)
    private val context = mockk<android.content.Context>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: ConversationListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val conversations = listOf(
            Conversation(
                id = "conv1",
                participantPhoneNumbers = "[\"+11111\"]",
                displayName = "Alice",
                lastMessageTime = 2000L
            ),
            Conversation(
                id = "conv2",
                participantPhoneNumbers = "[\"+22222\"]",
                displayName = "Bob",
                lastMessageTime = 1000L
            )
        )
        val pinned = listOf(
            Conversation(
                id = "conv3",
                participantPhoneNumbers = "[\"+33333\"]",
                displayName = "Pinned",
                isPinned = true,
                lastMessageTime = 3000L
            )
        )
        every { conversationRepository.getAllConversations() } returns flowOf(conversations)
        every { conversationRepository.getPinnedConversations() } returns flowOf(pinned)
        every { conversationRepository.getActivePlatforms() } returns flowOf(listOf("SMS"))
        every { context.getSharedPreferences(any(), any()) } returns mockk(relaxed = true)
        every { contactAvatarRepository.getAll() } returns flowOf(emptyList())

        viewModel = ConversationListViewModel(conversationRepository, smsImporter, contactAvatarRepository, context)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testInitialStateLoadsConversations() = runTest {
        val conversationList = viewModel.conversationList.value
        val pinnedList = viewModel.pinnedConversations.value

        assertEquals(2, conversationList.size)
        assertEquals(1, pinnedList.size)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun testSelectConversationUpdatesSelected() {
        val conversation = Conversation(
            id = "conv1",
            participantPhoneNumbers = "[\"+11111\"]",
            displayName = "Alice"
        )

        viewModel.selectConversation(conversation)

        assertNotNull(viewModel.selectedConversation.value)
        assertEquals("conv1", viewModel.selectedConversation.value?.id)
    }

    @Test
    fun testPinConversationCallsRepoWithIsPinnedTrue() = runTest {
        val conversation = Conversation(
            id = "conv1",
            participantPhoneNumbers = "[\"+11111\"]",
            displayName = "Alice",
            isPinned = false
        )
        coEvery { conversationRepository.getConversationById("conv1") } returns conversation
        coEvery { conversationRepository.updateConversation(any()) } returns Unit

        viewModel.pinConversation("conv1")

        coVerify {
            conversationRepository.getConversationById("conv1")
            conversationRepository.updateConversation(match {
                it.id == "conv1" && it.isPinned
            })
        }
    }

    @Test
    fun testDeleteConversationCallsRepo() = runTest {
        coEvery { conversationRepository.deleteConversationById("conv1") } returns Unit

        viewModel.deleteConversation("conv1")

        coVerify { conversationRepository.deleteConversationById("conv1") }
    }
}
