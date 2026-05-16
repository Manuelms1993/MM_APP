package com.example.mmapp.app2.ui.usecases

import com.example.mmapp.app2.data.repositories.ConversationDataSource
import com.example.mmapp.app2.domain.models.ConversationMessage
import kotlinx.coroutines.flow.Flow

class ObserveConversationUseCase(
    private val conversationDataSource: ConversationDataSource,
) {
    operator fun invoke(): Flow<List<ConversationMessage>> = conversationDataSource.getConversationFlow()
}
