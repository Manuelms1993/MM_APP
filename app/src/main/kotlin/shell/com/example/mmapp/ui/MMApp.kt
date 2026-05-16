package com.example.mmapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.mmapp.app1.AppContainer as PlantsAppContainer
import com.example.mmapp.app1.ui.PlantReminderApp
import com.example.mmapp.app2.AppContainer as FoodAppContainer
import com.example.mmapp.app2.ui.MMFoodApp
import com.example.mmapp.app3.AppContainer as TravelAppContainer
import com.example.mmapp.app3.ui.TravelGuideApp

private enum class AppSection(
    val label: String,
) {
    Plants("Plantas"),
    Food("Comida"),
    Travel("China"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MMApp(
    plantsContainer: PlantsAppContainer,
    foodContainer: FoodAppContainer,
    travelContainer: TravelAppContainer,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedSection by rememberSaveable { mutableStateOf(AppSection.Plants) }
    var plantsReloadVersion by rememberSaveable { mutableIntStateOf(0) }
    var foodReloadVersion by rememberSaveable { mutableIntStateOf(0) }
    var travelReloadVersion by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedSection.label) },
                actions = {
                    Column {
                        TextButton(onClick = { expanded = true }) {
                            Text("Menú")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            AppSection.entries.forEach { section ->
                                DropdownMenuItem(
                                    text = { Text(section.label) },
                                    onClick = {
                                        if (selectedSection != section) {
                                            when (section) {
                                                AppSection.Plants -> plantsReloadVersion++
                                                AppSection.Food -> foodReloadVersion++
                                                AppSection.Travel -> travelReloadVersion++
                                            }
                                        }
                                        selectedSection = section
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (selectedSection) {
                AppSection.Plants -> key("plants-$plantsReloadVersion") {
                    PlantReminderApp(
                        container = plantsContainer,
                        viewModelKey = "plants-$plantsReloadVersion",
                    )
                }

                AppSection.Food -> key("food-$foodReloadVersion") {
                    MMFoodApp(
                        container = foodContainer,
                        viewModelKey = "food-$foodReloadVersion",
                    )
                }

                AppSection.Travel -> key("travel-$travelReloadVersion") {
                    TravelGuideApp(
                        container = travelContainer,
                        viewModelKey = "travel-$travelReloadVersion",
                    )
                }
            }
        }
    }
}
