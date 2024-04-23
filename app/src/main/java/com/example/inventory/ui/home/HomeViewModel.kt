/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory.ui.home

// Import statements for Android lifecycle components, coroutine management, and data flow handling
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.ItemsRepository
import kotlinx.coroutines.flow.*

/**
 * ViewModel to retrieve and manage items in the inventory, including search functionality.
 * It uses Kotlin's Flow API to manage asynchronous data streams for UI updates.
 */
class HomeViewModel(private val itemsRepository: ItemsRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("") // Holds the current state of the search query

    /**
     * Holds home UI state. The list of items are retrieved from [ItemsRepository] and filtered based on
     * the current search query. This is achieved through a series of transformations on the flow of data.
     */
    val homeUiState: StateFlow<HomeUiState> = _searchQuery
        .debounce(300)  // Debounces the input to limit updates to the flow and reduce load
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                itemsRepository.getAllItemsStream() // Fetch all items if query is empty
            } else {
                itemsRepository.getAllItemsStream().map { items ->
                    // Filter items based on the query, checking for name inclusion or exact ID match
                    items.filter { it.name.contains(query, ignoreCase = true) || it.id.toString() == query }
                }
            }
        }
        .map { HomeUiState(it) } // Convert the resulting list of items into a UI state object
        .stateIn(
            scope = viewModelScope, // Use ViewModel's own coroutine scope to tie the flow's lifecycle
            started = SharingStarted.WhileSubscribed(5000L), // Restart flow if there are active subscribers within timeout
            initialValue = HomeUiState() // Initial empty state before any data is loaded
        )

    /**
     * Updates the current search query. This method updates the MutableStateFlow, triggering the flow pipeline.
     */
    fun searchProduct(query: String) {
        _searchQuery.value = query
    }

    // Static definition for a timeout constant used in flow configurations
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * Ui State for the HomeScreen. This data class represents the state of the UI at any point,
 * encapsulating the list of items currently being displayed.
 */
data class HomeUiState(val itemList: List<Item> = listOf()) // Initial state with an empty list of items
