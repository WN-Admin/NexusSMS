package com.nexussms.services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nexussms.data.models.Message
import com.nexussms.data.repository.MessageRepository
import com.nexussms.data.repository.ScheduledMessageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Date

@HiltWorker
class ScheduledMessageWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val scheduledMessageRepository: ScheduledMessageRepository,
    private val messageRepository: MessageRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val scheduledMessages = scheduledMessageRepository.getPendingScheduledMessages().first()
            val now = Date()

            for (scheduledMessage in scheduledMessages) {
                if (scheduledMessage.scheduledTime.time <= now.time) {
                    val message = Message(
                        conversationId = scheduledMessage.conversationId,
                        senderId = "self",
                        recipientId = scheduledMessage.recipientPhone,
                        content = scheduledMessage.content,
                        timestamp = Date(),
                        isIncoming = false,
                        isSent = true,
                        messageType = if (scheduledMessage.isRCS) "RCS" else "SMS",
                        attachmentUrls = scheduledMessage.attachmentUrls,
                        encryptionType = "AES256"
                    )
                    messageRepository.insertMessage(message)

                    scheduledMessageRepository.updateScheduledMessage(
                        scheduledMessage.copy(status = "SENT")
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
