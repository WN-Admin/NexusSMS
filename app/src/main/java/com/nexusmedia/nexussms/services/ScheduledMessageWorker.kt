package com.nexusmedia.nexussms.services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.ScheduledMessageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.Calendar

@HiltWorker
class ScheduledMessageWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val scheduledMessageRepository: ScheduledMessageRepository,
    private val conversationRepository: ConversationRepository,
    private val smsSender: SmsSender
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val scheduledMessages = scheduledMessageRepository.getPendingScheduledMessages().first()
            val nowTime = System.currentTimeMillis()

            for (scheduledMessage in scheduledMessages) {
                if (scheduledMessage.scheduledTime > nowTime) continue

                val sendResult = smsSender.sendTextMessage(
                    conversationId = scheduledMessage.conversationId,
                    recipientPhone = scheduledMessage.recipientPhoneNumber,
                    content = scheduledMessage.content,
                    persistToDb = true
                )

                if (sendResult.isFailure) {
                    scheduledMessageRepository.updateScheduledMessage(
                        scheduledMessage.copy(
                            status = "FAILED",
                            failureReason = sendResult.exceptionOrNull()?.message
                        )
                    )
                    continue
                }

                val sentAt = System.currentTimeMillis()
                conversationRepository.getConversationById(scheduledMessage.conversationId)?.let { conv ->
                    conversationRepository.updateConversation(
                        conv.copy(
                            lastMessage = scheduledMessage.content,
                            lastMessageTime = sentAt
                        )
                    )
                }

                val nextScheduledTime = computeNextScheduleTime(
                    repeatType = scheduledMessage.repeatType,
                    currentScheduledTime = scheduledMessage.scheduledTime
                )

                if (nextScheduledTime != null &&
                    (scheduledMessage.repeatUntil == null || nextScheduledTime <= scheduledMessage.repeatUntil)
                ) {
                    scheduledMessageRepository.updateScheduledMessage(
                        scheduledMessage.copy(
                            scheduledTime = nextScheduledTime,
                            status = "PENDING",
                            sentAt = sentAt,
                            failureReason = null
                        )
                    )
                } else {
                    scheduledMessageRepository.updateScheduledMessage(
                        scheduledMessage.copy(status = "SENT", sentAt = sentAt, failureReason = null)
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "ScheduledMessageWorker failed")
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
