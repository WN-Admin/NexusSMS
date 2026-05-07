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
import java.util.Calendar

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
            val nowTime = System.currentTimeMillis()

            for (scheduledMessage in scheduledMessages) {
                if (scheduledMessage.scheduledTime <= nowTime) {
                    val message = Message(
                        conversationId = scheduledMessage.conversationId,
                        senderPhoneNumber = "self",
                        recipientPhoneNumber = scheduledMessage.recipientPhoneNumber,
                        content = scheduledMessage.content,
                        timestamp = System.currentTimeMillis(),
                        type = "TEXT",
                        status = "SENT",
                        isEncrypted = true,
                        encryptionAlgorithm = "AES256",
                        mediaUrls = scheduledMessage.mediaUrls
                    )
                    messageRepository.insertMessage(message)

                    val sentAt = System.currentTimeMillis()
                    val nextScheduledTime = computeNextScheduleTime(
                        repeatType = scheduledMessage.repeatType,
                        currentScheduledTime = scheduledMessage.scheduledTime
                    )

                    if (nextScheduledTime != null &&
                        (scheduledMessage.repeatUntil == null || nextScheduledTime <= scheduledMessage.repeatUntil)
                    ) {
                        // Keep repeating messages pending and move the next trigger time.
                        scheduledMessageRepository.updateScheduledMessage(
                            scheduledMessage.copy(
                                scheduledTime = nextScheduledTime,
                                status = "PENDING",
                                sentAt = sentAt
                            )
                        )
                    } else {
                        scheduledMessageRepository.updateScheduledMessage(
                            scheduledMessage.copy(status = "SENT", sentAt = sentAt)
                        )
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun computeNextScheduleTime(
        repeatType: String,
        currentScheduledTime: Long
    ): Long? {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentScheduledTime }
        return when (repeatType) {
            "DAILY" -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.timeInMillis
            }

            "WEEKLY" -> {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                calendar.timeInMillis
            }

            "MONTHLY" -> {
                calendar.add(Calendar.MONTH, 1)
                calendar.timeInMillis
            }

            else -> null
        }
    }
}
