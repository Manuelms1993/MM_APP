package com.example.mmapp.app1.ui.usecases

import com.example.mmapp.app1.data.repositories.ConversationDataSource
import com.example.mmapp.app1.domain.models.ConversationMessage
import kotlinx.coroutines.flow.Flow

class ObserveConversationUseCase(
    private val conversationDataSource: ConversationDataSource,
) {
    operator fun invoke(): Flow<List<ConversationMessage>> = conversationDataSource.getConversationFlow()
}
