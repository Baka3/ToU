package com.example.tou

suspend fun ensureTopicExists(name: String) {
    if (name.isNotBlank() && App.db.topicDao().exists(name) == 0) {
        val maxOrder = App.db.topicDao().getMaxOrder() ?: 0
        App.db.topicDao().insert(CustomTopicEntity(name = name, order = maxOrder + 1))
    }
}