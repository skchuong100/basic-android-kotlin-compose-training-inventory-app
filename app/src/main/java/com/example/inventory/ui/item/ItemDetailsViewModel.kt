package com.example.inventory.ui.item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.ItemsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel to retrieve, update and delete an item from the [ItemsRepository]'s data source.
 * This class is responsible for handling the business logic associated with an individual item's details
 * and updating the UI state based on changes in the item's data.
 */
class ItemDetailsViewModel(
    savedStateHandle: SavedStateHandle,  // Handle for retrieving arguments passed through the navigation
    private val itemsRepository: ItemsRepository,  // Repository for accessing item data
) : ViewModel() {

    private val itemId: Int = checkNotNull(savedStateHandle[ItemDetailsDestination.itemIdArg])  // Retrieve the item ID from the saved state

    /**
     * Holds the item details UI state. The data is retrieved from [ItemsRepository] and mapped to
     * the UI state.
     */
    val uiState: StateFlow<ItemDetailsUiState> =
        itemsRepository.getItemStream(itemId)  // Stream the item details from the repository
            .filterNotNull()  // Ensure that only non-null item data is processed
            .map { item ->
                ItemDetailsUiState(outOfStock = item.quantity <= 0, itemDetails = item.toItemDetails())
            }.stateIn(
                scope = viewModelScope,  // Define the Coroutine scope tied to the ViewModel lifecycle
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),  // Only active while there are subscribers, with a timeout
                initialValue = ItemDetailsUiState()  // Initial value before any data is loaded
            )

    /**
     * Reduces the item quantity by one and updates the [ItemsRepository]'s data source.
     */
    fun reduceQuantityByOne() {
        viewModelScope.launch {  // Launch a coroutine for asynchronous execution
            val currentItem = uiState.value.itemDetails.toItem()  // Convert UI state to an item model
            if (currentItem.quantity > 0) {
                itemsRepository.updateItem(currentItem.copy(quantity = currentItem.quantity - 1))  // Update the repository with the new quantity
            }
        }
    }

    /**
     * Submits an order for a specified quantity, reducing the stock quantity accordingly.
     */
    fun submitOrder(quantity: Int) {
        viewModelScope.launch {
            val currentItem = uiState.value.itemDetails.toItem()
            if (currentItem.quantity > 0) {
                itemsRepository.updateItem(currentItem.copy(quantity = currentItem.quantity - quantity))
            }
        }
    }

    /**
     * Purchase a specific quantity of the item, updating the inventory if sufficient stock is available.
     */
    fun purchaseItem(purchaseQuantity: Int) {
        viewModelScope.launch {
            val currentItem = uiState.value.itemDetails.toItem()
            if (currentItem.quantity >= purchaseQuantity) {
                itemsRepository.updateItem(currentItem.copy(quantity = currentItem.quantity - purchaseQuantity))
            } else {
                // Ideally, handle insufficient stock scenario here (e.g., error notification)
            }
        }
    }

    /**
     * Deletes the item from the [ItemsRepository]'s data source.
     */
    suspend fun deleteItem() {
        itemsRepository.deleteItem(uiState.value.itemDetails.toItem())
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L  // Define a timeout for keeping the flow subscription active
    }
}

/**
 * UI state for ItemDetailsScreen.
 * This data class holds the current state of the item details to be displayed on the UI,
 * including stock availability and item detail.
 */
data class ItemDetailsUiState(
    val outOfStock: Boolean = true,  // Indicates if the item is out of stock
    val itemDetails: ItemDetails = ItemDetails()  // The details of the item
)
