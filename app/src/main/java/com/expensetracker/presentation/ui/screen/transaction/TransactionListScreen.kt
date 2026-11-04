package com.expensetracker.presentation.ui.screen.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Chip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.presentation.theme.AppTheme
import com.expensetracker.presentation.theme.Green600
import com.expensetracker.presentation.ui.component.CategoryFilterChips
import com.expensetracker.presentation.ui.component.EmptyTransactionsState
import com.expensetracker.presentation.ui.component.LoadingState
import com.expensetracker.presentation.ui.component.TransactionCard
import com.expensetracker.presentation.viewmodel.TransactionListViewModel

@Composable
fun TransactionListScreen(
    viewModel: TransactionListViewModel,
    transactionType: TransactionType,
    onNavigateToAdd: (TransactionType) -> Unit,
    onNavigateToEdit: (Transaction) -> Unit,
    onNavigateBack: () -> Unit
) {
    val pagingItems = viewModel.pager.collectAsLazyPagingItems()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val transactionTypeState by viewModel.transactionType.collectAsState()

    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    AppTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            TransactionListTopAppBar(
                title = transactionType.name,
                onNavigateBack = onNavigateBack,
                onFilterClick = { showFilterMenu = true },
                onSortClick = { showSortMenu = true },
                onSearchClick = { }
            )

            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::setSearchQuery
            )

            // Category Filter Chips
            CategoryFilterChips(
                categories = emptyList(), // Would be passed from parent
                selectedCategoryId = selectedCategoryId,
                onCategoryClick = viewModel::setCategoryFilter
            )

            // Transaction List
            androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    pagingItems.loadState.refresh is androidx.paging.LoadState.Loading -> LoadingState()
                    pagingItems.loadState.refresh is androidx.paging.LoadState.Error -> {
                        ErrorState(
                            message = (pagingItems.loadState.refresh as androidx.paging.LoadState.Error).error.message ?: "Failed to load transactions",
                            onRetry = { pagingItems.retry() }
                        )
                    }
                    else -> {
                        if (pagingItems.itemCount == 0) {
                            EmptyTransactionsState(onAddClick = { onNavigateToAdd(transactionType) })
                        } else {
                            TransactionList(
                                pagingItems = pagingItems,
                                onItemClick = onNavigateToEdit,
                                onItemLongClick = { }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionListTopAppBar(
    title: String,
    onNavigateBack: () -> Unit,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    androidx.compose.material3.TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        navigationIcon = {
            androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1C1C1C))
            }
        },
        title = { Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C)) },
        actions = {
            androidx.compose.material3.IconButton(onClick = onFilterClick) {
                Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filter", tint = Color(0xFF1C1C1C))
            }
            androidx.compose.material3.IconButton(onClick = onSortClick) {
                Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort", tint = Color(0xFF1C1C1C))
            }
        },
        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color(0xFF1C1C1C)
        )
    )
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search transactions...", color = Color(0xFF9E9E9E)) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF9E9E9E)) },
            trailingIcon = {
                if (query.isNotBlank()) {
                    androidx.compose.material3.IconButton(onClick = { onQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF9E9E9E))
                    }
                }
            },
            singleLine = true,
            colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
                containerColor = Color(0xFFF5F5F5),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFF5F5F5)
            )
        )
    }
}

@Composable
fun TransactionList(
    pagingItems: androidx.paging.compose.LazyPagingItems<Transaction>,
    onItemClick: (Transaction) -> Unit,
    onItemLongClick: (Transaction) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(pagingItems) { transaction ->
            transaction?.let {
                TransactionCard(
                    transaction = it,
                    onClick = { onItemClick(it) },
                    onEdit = { },
                    onDelete = { }
                )
            }
        }
        items(pagingItems.loadState.append) { loadState ->
            if (loadState is androidx.paging.LoadState.Loading) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = Green600
                )
            }
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(64.dp))
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Error", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C))
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, fontSize = 16.sp, color = Color(0xFF757575), textAlign = androidx.compose.ui.text.TextAlign.Center)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
        androidx.compose.material3.Button(onClick = onRetry) { Text("Retry") }
    }
}