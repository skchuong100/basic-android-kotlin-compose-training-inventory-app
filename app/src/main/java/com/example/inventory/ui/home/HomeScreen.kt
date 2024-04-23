package com.example.inventory.ui.home

// Importing necessary libraries and components
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventory.InventoryTopAppBar
import com.example.inventory.R
import com.example.inventory.data.Item
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.item.formatedPrice
import com.example.inventory.ui.navigation.NavigationDestination
import com.example.inventory.ui.theme.InventoryTheme

// Object representing navigation destination specifics for the home screen
object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

// Entry point for the Home screen, utilizing Compose's experimental material API
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController, // NavController to handle navigation
    navigateToItemEntry: () -> Unit, // Lambda function to handle navigation to item entry screen
    navigateToItemUpdate: (Int) -> Unit, // Lambda function to handle navigation to item update screen
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory), // ViewModel that handles the logic for the Home screen
    modifier: Modifier = Modifier // Modifier for styling
) {
    val homeUiState by viewModel.homeUiState.collectAsState() // Collecting UI state as a state object
    val searchTextState = remember { mutableStateOf("") } // Remembering the search text across recompositions
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior() // Defines the scroll behavior for the top app bar

    // Main layout structure with a top bar and floating action button
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), // Applying nested scroll behavior
        topBar = {
            InventoryTopAppBar(
                title = stringResource(id = R.string.app_name), // Title for the top app bar
                canNavigateBack = navController.previousBackStackEntry != null, // Enables back navigation if possible
                navigateUp = { navController.navigateUp() }, // Action on back navigation
                scrollBehavior = scrollBehavior // Applies the defined scroll behavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToItemEntry, // Navigation action for the floating action button
                shape = MaterialTheme.shapes.medium, // Shape theming for the button
                modifier = Modifier
                    .padding(end = WindowInsets.safeDrawing.asPaddingValues().calculateEndPadding(LocalLayoutDirection.current))
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.item_entry_title)) // Icon for the button
            }
        },
    ) { innerPadding ->
        HomeBody(
            itemList = homeUiState.itemList, // List of items to display
            onItemClick = navigateToItemUpdate, // Action on item click
            searchTextState = searchTextState, // Search text state
            onSearchTriggered = { searchText -> viewModel.searchProduct(searchText) }, // Action on search trigger
            modifier = Modifier.padding(innerPadding).fillMaxSize(), // Applying padding and filling size
            contentPadding = PaddingValues(top = 8.dp) // Additional padding for content
        )
    }
}

// Component displaying the body of the home screen including a search bar and item list
@Composable
private fun HomeBody(
    itemList: List<Item>, // List of items to display
    onItemClick: (Int) -> Unit, // Action on item click
    searchTextState: MutableState<String>, // Mutable state for the search text
    onSearchTriggered: (String) -> Unit, // Action on search trigger
    modifier: Modifier = Modifier, // Modifier for styling
    contentPadding: PaddingValues // Padding for the content
) {
    Column(modifier = modifier.padding(contentPadding)) { // Vertical arrangement
        OutlinedTextField(
            value = searchTextState.value, // Current value of the search field
            onValueChange = {
                searchTextState.value = it
                onSearchTriggered(it)
            },
            label = { Text("Search by ID or Name") }, // Label for the search field
            singleLine = true, // Limits to a single line input
            trailingIcon = {
                IconButton(onClick = { onSearchTriggered(searchTextState.value) }) { // Icon button for search action
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search), // Keyboard options for the search action
            keyboardActions = KeyboardActions(onSearch = {
                onSearchTriggered(searchTextState.value) // Defines action on keyboard search action
            }),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp) // Modifiers for width and padding
        )
        if (itemList.isEmpty()) { // Conditionally display text if the list is empty
            Text(
                text = stringResource(R.string.no_item_description), // Text for no items
                textAlign = TextAlign.Center, // Center alignment
                style = MaterialTheme.typography.titleLarge, // Styling
                modifier = Modifier.padding(16.dp) // Padding
            )
        } else { // Displaying list of items using LazyColumn for efficient rendering
            LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                items(items = itemList, key = { it.id }) { item -> // Defining items and keys
                    InventoryItem(item = item, modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { onItemClick(item.id) }) // Item component with click handling
                }
            }
        }
    }
}

// Component for displaying a single inventory item
@Composable
private fun InventoryItem(
    item: Item, modifier: Modifier = Modifier // Item data and modifier for styling
) {
    Card(
        modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Card styling with elevation
    ) {
        Column(
            modifier = Modifier.padding(16.dp), // Padding within the card
            verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing between elements
        ) {
            Row(
                modifier = Modifier.fillMaxWidth() // Filling the width
            ) {
                Text(
                    text = item.name, // Display item name
                    style = MaterialTheme.typography.titleLarge, // Styling for the text
                )
                Spacer(Modifier.weight(1f)) // Spacer to push elements to opposite ends
                Text(
                    text = item.formatedPrice(), // Display formatted price
                    style = MaterialTheme.typography.titleMedium // Styling for the price text
                )
            }
            Text(
                text = stringResource(R.string.in_stock, item.quantity), // Display stock quantity
                style = MaterialTheme.typography.titleMedium // Styling for the text
            )
        }
    }
}

// Preview annotations for visualizing the components in the Android Studio design editor
@Preview(showBackground = true)
@Composable
fun HomeBodyPreview() {
    val mockItems = listOf(
        Item(1, "Game", 100.0, 20),
        Item(2, "Pen", 200.0, 30),
        Item(3, "TV", 300.0, 50)
    )
    val searchTextState = remember { mutableStateOf("") }
    InventoryTheme {
        HomeBody(
            itemList = mockItems,
            onItemClick = {},  // No navigation handling necessary for preview
            searchTextState = searchTextState,
            onSearchTriggered = {},
            contentPadding = PaddingValues(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeBodyEmptyListPreview() {
    val searchTextState = remember { mutableStateOf("") }
    InventoryTheme {
        HomeBody(
            itemList = listOf(),
            onItemClick = {},  // No navigation handling necessary for preview
            searchTextState = searchTextState,
            onSearchTriggered = {},
            contentPadding = PaddingValues(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryItemPreview() {
    InventoryTheme {
        InventoryItem(
            Item(1, "Game", 100.0, 20),
        )
    }
}
