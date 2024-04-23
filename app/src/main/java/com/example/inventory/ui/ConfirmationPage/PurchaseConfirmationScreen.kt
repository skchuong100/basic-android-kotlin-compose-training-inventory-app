package com.example.inventory.ui.ConfirmationPage

// Import statements bring in necessary libraries and components for the UI elements and functionality
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.inventory.InventoryTopAppBar
import com.example.inventory.R
import com.example.inventory.data.PurchaseDetails

// This annotation flags that the following Composable function uses experimental API features.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseConfirmationScreen(
    navController: NavController, // NavController for managing navigation within the app
    purchaseDetails: PurchaseDetails // Data class containing details about the purchase
) {
    // Scaffold is a layout structure that provides a consistent layout structure to UIs
    Scaffold(
        topBar = {
            // Custom top bar component for the app
            InventoryTopAppBar(
                title = stringResource(R.string.purchase_confirmation_title), // Set the title from string resources
                canNavigateBack = true, // Allows navigating back in the navigation stack
                navigateUp = { navController.navigateUp() } // Handler when the back action is triggered
            )
        }
    ) { innerPadding ->
        // Content of the screen, displaying item details with appropriate padding
        ItemDetails(
            purchaseDetails = purchaseDetails,
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
        )
    }
}

@Composable
fun ItemDetails(purchaseDetails: PurchaseDetails, modifier: Modifier = Modifier) {
    // Card component that wraps item details
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        // Vertical layout for item details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
        ) {
            // Repeated component for each detail row, showing various attributes of the purchase
            ItemDetailsRow(
                labelResID = R.string.item,
                itemDetail = purchaseDetails.productName,
                modifier = Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.padding_medium)
                )
            )
            // Repeated usage of ItemDetailsRow for different attributes
            ItemDetailsRow(
                labelResID = R.string.quantity_ordered,
                itemDetail = purchaseDetails.quantityOrdered.toString(),
                modifier = Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.padding_medium)
                )
            )
            ItemDetailsRow(
                labelResID = R.string.price,
                itemDetail = purchaseDetails.pricePerItem,
                modifier = Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.padding_medium)
                )
            )
            ItemDetailsRow(
                labelResID = R.string.total_cost,
                itemDetail = purchaseDetails.totalCost,
                modifier = Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.padding_medium)
                )
            )
            ItemDetailsRow(
                labelResID = R.string.quantity_in_stock,
                itemDetail = purchaseDetails.itemsLeftInInventory.toString(),
                modifier = Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.padding_medium)
                )
            )
        }
    }
}

// Helper Composable function to display a row of item details
@Composable
private fun ItemDetailsRow(
    @StringRes labelResID: Int, // Resource ID for label text
    itemDetail: String, // Detail text to display
    modifier: Modifier = Modifier // Modifier for styling and layout adjustments
) {
    Row(modifier = modifier) {
        // Displays the label from resources
        Text(text = stringResource(labelResID))
        // Spacer to create space between label and detail
        Spacer(modifier = Modifier.weight(1f))
        // Displays item detail, with emphasis using bold font
        Text(text = itemDetail, fontWeight = FontWeight.Bold)
    }
}
