package com.example.inventory.ui.item

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.inventory.InventoryTopAppBar
import com.example.inventory.R
import com.example.inventory.data.Item
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.navigation.NavigationDestination
import com.example.inventory.ui.theme.InventoryTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object ItemDetailsDestination : NavigationDestination {
    override val route = "item_details"
    override val titleRes = R.string.item_detail_title
    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    navigateToEditItem: (Int) -> Unit, // Function to navigate to item editing
    navigateBack: () -> Unit, // Function to handle back navigation
    navController: NavController, // Navigation controller for app navigation
    modifier: Modifier = Modifier, // Modifier for styling and layout adjustments
    viewModel: ItemDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory) // View model for managing item details
) {
    val uiState by viewModel.uiState.collectAsState() // Collecting the UI state from the view model

    val coroutineScope = rememberCoroutineScope() // Remembering a coroutine scope tied to this composable's lifecycle

    Scaffold(
        topBar = {
            InventoryTopAppBar(
                title = stringResource(ItemDetailsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigateToEditItem(uiState.itemDetails.id) },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(
                        end = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateEndPadding(LocalLayoutDirection.current)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_item_title),
                )
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        ItemDetailsBody(
            itemDetailsUiState = uiState, // UI state containing the item details
            onSellItem = { viewModel.reduceQuantityByOne() }, // Function to handle selling an item
            onDelete = {
                coroutineScope.launch {
                    viewModel.deleteItem() // Delete the item
                    navigateBack() // Navigate back after deletion
                }
            },
            onBuyItem = { quantity ->
                viewModel.submitOrder(quantity) // Function to handle buying an item
            },
            navController = navController, // Pass the navController
            coroutineScope = coroutineScope, // Pass the coroutineScope
            itemDetails = uiState.itemDetails, // Pass the item details
            viewModel = viewModel, // Pass the viewModel
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                )
                .verticalScroll(rememberScrollState()) // Make the body scrollable
        )
    }
}

// Composable function that defines the layout for displaying and interacting with the item details
@Composable
private fun ItemDetailsBody(
    itemDetailsUiState: ItemDetailsUiState, // UI state for the item details
    onSellItem: () -> Unit, // Function to sell the item
    onDelete: () -> Unit, // Function to delete the item
    onBuyItem: (Int) -> Unit, // Function to buy the item
    navController: NavController, // Navigation controller
    modifier: Modifier = Modifier, // Modifier for styling
    coroutineScope: CoroutineScope, // Coroutine scope for launching asynchronous tasks
    viewModel: ItemDetailsViewModel, // ViewModel for the item details logic
    itemDetails: ItemDetails, // Item details data class
) {
    var quantity by rememberSaveable { mutableStateOf(1) } // Remember the quantity state across recompositions

    val item = itemDetailsUiState.itemDetails.toItem() // Convert the itemDetailsUiState to Item data class
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)), // Apply padding
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)) // Arrange elements with space in between
    ) {
        var deleteConfirmationRequired by rememberSaveable { mutableStateOf(false) } // State to trigger delete confirmation dialog
        ItemDetails(
            item = itemDetailsUiState.itemDetails.toItem(), // Display the item details
            modifier = Modifier.fillMaxWidth(), // Fill the width
        )
        Row(
            modifier = Modifier.fillMaxWidth(), // Fill the width for the row
            verticalAlignment = Alignment.CenterVertically // Center items vertically
        ) {
            var quantity by rememberSaveable { mutableStateOf(1) } // Remember the quantity state
            OutlinedTextField(
                value = quantity.toString(), // Display the quantity
                onValueChange = { newQuantity -> quantity = newQuantity.toIntOrNull() ?: 1 }, // Update the quantity on change
                label = { Text(stringResource(R.string.quantity)) }, // Label for the quantity field
                modifier = Modifier.weight(1f), // Fill the available space
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number // Set the keyboard type to number
                ),
                singleLine = true // Single line input
            )
            Button(
                onClick = {
                    coroutineScope.launch {
                        // Launch asynchronous tasks
                        val quantityInt = quantity
                        viewModel.submitOrder(quantityInt) // Submit the order

                        val totalCost = item.price * quantityInt // Calculate the total cost
                        navController.navigate(
                            "purchaseConfirmation/${item.name}/${item.price}/$quantityInt/$totalCost/${item.quantity - quantityInt}"
                        ) // Navigate to purchase confirmation
                    }
                },
                modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_medium)), // Apply padding
                shape = MaterialTheme.shapes.small, // Apply shape
            ) {
                Text(stringResource(R.string.buy)) // Display the buy button
            }
        }
        Button(
            onClick = onSellItem, // Handle sell item
            modifier = Modifier.fillMaxWidth(), // Fill the width
            shape = MaterialTheme.shapes.small, // Apply shape
            enabled = !itemDetailsUiState.outOfStock // Enable button based on stock availability
        ) {
            Text(stringResource(R.string.sell)) // Display the sell button
        }
        OutlinedButton(
            onClick = { deleteConfirmationRequired = true }, // Trigger delete confirmation
            shape = MaterialTheme.shapes.small, // Apply shape
            modifier = Modifier.fillMaxWidth() // Fill the width
        ) {
            Text(stringResource(R.string.delete)) // Display the delete button
        }
        if (deleteConfirmationRequired) {
            DeleteConfirmationDialog(
                onDeleteConfirm = {
                    deleteConfirmationRequired = false
                    onDelete() // Confirm deletion
                },
                onDeleteCancel = { deleteConfirmationRequired = false }, // Cancel deletion
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)) // Apply padding
            )
        }
    }
}


@Composable
fun ItemDetails(
    item: Item, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier, colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
        ) {
            ItemDetailsRow(
                labelResID = R.string.item,
                itemDetail = item.name,
                modifier = Modifier.padding(
                    horizontal = dimensionResource(
                        id = R.dimen
                            .padding_medium
                    )
                )
            )
            ItemDetailsRow(
                labelResID = R.string.quantity_in_stock,
                itemDetail = item.quantity.toString(),
                modifier = Modifier.padding(
                    horizontal = dimensionResource(
                        id = R.dimen
                            .padding_medium
                    )
                )
            )
            ItemDetailsRow(
                labelResID = R.string.price,
                itemDetail = item.formatedPrice(),
                modifier = Modifier.padding(
                    horizontal = dimensionResource(
                        id = R.dimen
                            .padding_medium
                    )
                )
            )
        }

    }
}

// Composable function that displays a single row of item details
@Composable
private fun ItemDetailsRow(
    @StringRes labelResID: Int, // Resource ID for the label
    itemDetail: String, // Detail to display
    modifier: Modifier = Modifier // Modifier for styling
) {
    Row(modifier = modifier) {
        Text(text = stringResource(labelResID)) // Display the label
        Spacer(modifier = Modifier.weight(1f)) // Spacer to push content to the ends
        Text(text = itemDetail, fontWeight = FontWeight.Bold) // Display the item detail with bold weight
    }
}

// Composable function for displaying a confirmation dialog for deletion
@Composable
private fun DeleteConfirmationDialog(
    onDeleteConfirm: () -> Unit, // Function to confirm deletion
    onDeleteCancel: () -> Unit, // Function to cancel deletion
    modifier: Modifier = Modifier // Modifier for styling
) {
    AlertDialog(
        onDismissRequest = { /* Do nothing on dismiss */ },
        title = { Text(stringResource(R.string.attention)) }, // Title for the dialog
        text = { Text(stringResource(R.string.delete_question)) }, // Text content for the dialog
        modifier = modifier, // Apply the modifier
        dismissButton = {
            TextButton(onClick = onDeleteCancel) {
                Text(text = stringResource(R.string.no)) // No button text
            }
        },
        confirmButton = {
            TextButton(onClick = onDeleteConfirm) {
                Text(text = stringResource(R.string.yes)) // Yes button text
            }
        })
}
