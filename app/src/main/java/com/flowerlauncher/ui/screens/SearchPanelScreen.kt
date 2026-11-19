package com.flowerlauncher.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowerlauncher.ui.FlowerLauncherViewModel
import com.flowerlauncher.ui.components.AppIconItem

@Composable
fun SearchPanelScreen(
    viewModel: FlowerLauncherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // In-line Math Expression Evaluation
    val mathResult = remember(searchQuery) {
        evaluateMathExpression(searchQuery)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search apps, web, calculate...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedContainerColor = Color.White.copy(alpha = 0.08f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Math Calculation Card
            if (mathResult != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Calculator Result", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text(mathResult, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }

            // Web Search Options (DuckDuckGo, Google)
            if (searchQuery.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            val uri = Uri.parse("https://duckduckgo.com/?q=${Uri.encode(searchQuery)}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                        },
                        label = { Text("DuckDuckGo") },
                        icon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    SuggestionChip(
                        onClick = {
                            val uri = Uri.parse("https://www.google.com/search?q=${Uri.encode(searchQuery)}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                        },
                        label = { Text("Google") },
                        icon = { Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            // Results List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.launchApp(app) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIconItem(
                            app = app,
                            iconDrawable = viewModel.repository.getAppIcon(app.packageName, app.className),
                            iconSize = 44.dp,
                            showLabel = false,
                            onAppClick = { viewModel.launchApp(app) }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = app.label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text(text = app.packageName, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// Lightweight offline math evaluator
private fun evaluateMathExpression(expr: String): String? {
    if (expr.isBlank()) return null
    val sanitized = expr.replace(" ", "")
    val regex = Regex("^(\\d+(?:\\.\\d+)?)([+\\-*/])(\\d+(?:\\.\\d+)?)$")
    val match = regex.find(sanitized) ?: return null
    val a = match.groupValues[1].toDoubleOrNull() ?: return null
    val op = match.groupValues[2]
    val b = match.groupValues[3].toDoubleOrNull() ?: return null

    val res = when (op) {
        "+" -> a + b
        "-" -> a - b
        "*" -> a * b
        "/" -> if (b != 0.0) a / b else return "Cannot divide by 0"
        else -> return null
    }
    return if (res % 1.0 == 0.0) res.toLong().toString() else "%.4f".format(res)
}
