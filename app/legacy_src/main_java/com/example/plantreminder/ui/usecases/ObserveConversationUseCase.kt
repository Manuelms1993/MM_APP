package com.example.plantreminder.ui.usecases

import com.example.plantreminder.data.repositories.ConversationDataSource
import com.example.plantreminder.domain.models.ConversationMessage
import kotlinx.coroutines.flow.Flow

class ObserveConversationUseCase(
    private val conversationDataSource: ConversationDataSource,
) {
    operator fun invoke(): Flow<List<ConversationMessage>> = conversationDataSource.getConversationFlow()
}
