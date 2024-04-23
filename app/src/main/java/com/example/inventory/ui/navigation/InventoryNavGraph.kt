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

package com.example.inventory.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.inventory.data.PurchaseDetails
import com.example.inventory.ui.ConfirmationPage.PurchaseConfirmationScreen
import com.example.inventory.ui.home.HomeDestination
import com.example.inventory.ui.home.HomeScreen
import com.example.inventory.ui.item.ItemDetailsDestination
import com.example.inventory.ui.item.ItemDetailsScreen
import com.example.inventory.ui.item.ItemEditDestination
import com.example.inventory.ui.item.ItemEditScreen
import com.example.inventory.ui.item.ItemEntryDestination
import com.example.inventory.ui.item.ItemEntryScreen

/**
 * Provides the navigation graph for the application using Jetpack Compose Navigation component.
 * This function defines all the navigation routes and the corresponding screens for the app.
 */
@Composable
fun InventoryNavHost(
    navController: NavHostController, // Controller for managing app navigation
    modifier: Modifier = Modifier // Modifier for customizing the layout further
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route, // Starting point in the navigation graph
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) { // Home screen route
            HomeScreen(
                navController = navController, // Pass NavController here
                navigateToItemEntry = { navController.navigate(ItemEntryDestination.route) }, // Navigate to item entry screen
                navigateToItemUpdate = { itemId -> // Navigate to item details screen
                    navController.navigate("${ItemDetailsDestination.route}/$itemId")
                }
            )
        }
        composable(route = ItemEntryDestination.route) { // Item entry screen route
            ItemEntryScreen(
                navigateBack = { navController.popBackStack() }, // Handle back navigation
                onNavigateUp = { navController.navigateUp() } // Handle up navigation
            )
        }
        composable(
            route = ItemDetailsDestination.routeWithArgs, // Item details screen with arguments
            arguments = listOf(navArgument(ItemDetailsDestination.itemIdArg) { // Argument for item ID
                type = NavType.IntType
            })
        ) {
            ItemDetailsScreen(
                navigateToEditItem = { itemId -> // Navigate to item edit screen
                    navController.navigate("${ItemEditDestination.route}/$itemId")
                },
                navigateBack = { navController.navigateUp() }, // Handle back navigation
                navController = navController // Pass the NavController
            )
        }
        composable(
            route = "purchaseConfirmation/{productName}/{pricePerItem}/{quantityOrdered}/{totalCost}/{itemsLeftInInventory}", // Purchase confirmation screen with arguments
            arguments = listOf(
                navArgument("productName") { type = NavType.StringType },
                navArgument("pricePerItem") { type = NavType.StringType },
                navArgument("quantityOrdered") { type = NavType.IntType },
                navArgument("totalCost") { type = NavType.StringType },
                navArgument("itemsLeftInInventory") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            PurchaseConfirmationScreen(
                navController = navController,
                purchaseDetails = PurchaseDetails(
                    productName = backStackEntry.arguments?.getString("productName") ?: "", // Retrieve the product name from arguments
                    pricePerItem = backStackEntry.arguments?.getString("pricePerItem") ?: "", // Retrieve the price per item from arguments
                    quantityOrdered = backStackEntry.arguments?.getInt("quantityOrdered") ?: 0, // Retrieve the quantity ordered from arguments
                    totalCost = backStackEntry.arguments?.getString("totalCost") ?: "", // Retrieve the total cost from arguments
                    itemsLeftInInventory = backStackEntry.arguments?.getInt("itemsLeftInInventory") ?: 0 // Retrieve the items left in inventory from arguments
                )
            )
        }
    }
}
