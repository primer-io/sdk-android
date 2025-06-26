# PrimerSelectCountryScope Usage Guide

## Overview

`PrimerSelectCountryScope` provides a complete country selection interface for the Primer SDK. This scope handles country data loading, search functionality, and user selection with full customization capabilities.

### Key Capabilities

- **State Management**: Reactive state handling for country loading, search, and selection
- **Search Functionality**: Real-time country filtering with case-insensitive search
- **UI Customization**: Full screen, search bar, and country item component customization
- **Navigation Integration**: Seamless integration with checkout flow navigation
- **Data Management**: Automatic country data loading with caching
- **Accessibility**: Built-in accessibility support for screen readers

## Customization Levels

### 1. Screen Level Customization
Replace the entire country selection screen with your custom implementation while maintaining the underlying business logic.

### 2. Component Level Customization
Customize individual UI components like the search bar or country list items while keeping the default screen structure.

### 3. Hybrid Customization
Mix and match default components with custom ones for targeted customization where needed.

## How Customization Works

The `PrimerSelectCountryScope` uses **property reassignment** for customization:

```kotlin
// ✅ Correct - Property reassignment
selectCountryScope.searchBar = { query, onQueryChange, placeholder ->
    CustomSearchBar(query, onQueryChange, placeholder)
}

// ❌ Incorrect - Method override not supported
override fun searchBar() = CustomSearchBar()
```

## Basic Usage

### Default Implementation

The simplest way to use `PrimerSelectCountryScope` is with the default implementation:

```kotlin
@Composable
fun MyCheckoutScreen() {
    val checkoutScope = Primer.checkout()
    
    // Country selection screen with default UI
    LaunchedEffect(Unit) {
        checkoutScope.cardForm.navigateToCountrySelection()
    }
    
    // The scope handles everything automatically:
    // - Loading countries from data source
    // - Search functionality
    // - Country selection and navigation back
    // - Error handling and loading states
}
```

## Component-Level Customization

### Custom Search Bar

```kotlin
@Composable
fun MyCheckoutScreen() {
    val checkoutScope = Primer.checkout()
    
    // Customize the search bar
    checkoutScope.cardForm.selectCountry.searchBar = { query, onQueryChange, placeholder ->
        CustomCountrySearchBar(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = placeholder
        )
    }
}

@Composable
fun CustomCountrySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
```

### Custom Country Item

```kotlin
@Composable
fun MyCheckoutScreen() {
    val checkoutScope = Primer.checkout()
    
    // Customize individual country items
    checkoutScope.cardForm.selectCountry.countryItem = { country, onSelect ->
        CustomCountryItem(
            country = country,
            onSelect = onSelect
        )
    }
}

@Composable
fun CustomCountryItem(
    country: PrimerCountry,
    onSelect: (String, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onSelect(country.code.name, country.name) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Country flag emoji or icon
            Text(
                text = getFlagEmoji(country.code),
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = country.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = country.code.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Select ${country.name}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

## Complete Custom Implementation

### Full Screen Replacement

```kotlin
@Composable
fun MyCheckoutScreen() {
    val checkoutScope = Primer.checkout()
    
    // Replace the entire country selection screen
    checkoutScope.cardForm.selectCountry.screen = {
        CustomCountrySelectionScreen(checkoutScope.cardForm.selectCountry)
    }
}

@Composable
fun CustomCountrySelectionScreen(scope: PrimerSelectCountryScope) {
    val state by scope.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Custom header
        TopAppBar(
            title = { Text("Select Your Country") },
            navigationIcon = {
                IconButton(onClick = scope::onCancel) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        
        when (state) {
            is PrimerSelectCountryScope.State -> {
                if (state.isLoading) {
                    // Custom loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading countries...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Custom search section
                    CustomSearchSection(
                        query = state.searchQuery,
                        onQueryChange = scope::onSearch
                    )
                    
                    // Custom country list
                    CustomCountryList(
                        countries = state.filteredCountries,
                        onCountrySelect = scope::onCountrySelected
                    )
                }
            }
        }
    }
}

@Composable
fun CustomSearchSection(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search countries...") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun CustomCountryList(
    countries: List<PrimerCountry>,
    onCountrySelect: (String, String) -> Unit
) {
    if (countries.isEmpty()) {
        // Empty state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No countries found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Try a different search term",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(countries) { country ->
                CountryListItem(
                    country = country,
                    onSelect = { onCountrySelect(country.code.name, country.name) }
                )
            }
        }
    }
}

@Composable
fun CountryListItem(
    country: PrimerCountry,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Country flag
            Text(
                text = getFlagEmoji(country.code),
                fontSize = 28.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            // Country details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = country.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Code: ${country.code.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Selection indicator
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Select",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
```

## Advanced Usage Patterns

### State Observation and Custom Logic

```kotlin
@Composable
fun MyCheckoutScreen() {
    val checkoutScope = Primer.checkout()
    val selectCountryScope = checkoutScope.cardForm.selectCountry
    val state by selectCountryScope.state.collectAsState()
    
    // Custom logic based on state
    LaunchedEffect(state) {
        when (state) {
            is PrimerSelectCountryScope.State -> {
                if (state.filteredCountries.isEmpty() && state.searchQuery.isNotEmpty()) {
                    // Custom behavior for empty search results
                    // e.g., Analytics tracking, suggestions, etc.
                }
            }
        }
    }
    
    // Custom screen with state-dependent behavior
    selectCountryScope.screen = {
        CustomCountryScreenWithAnalytics(selectCountryScope, state)
    }
}

@Composable
fun CustomCountryScreenWithAnalytics(
    scope: PrimerSelectCountryScope,
    state: PrimerSelectCountryScope.State
) {
    // Track screen view
    LaunchedEffect(Unit) {
        Analytics.track("country_selection_screen_viewed")
    }
    
    // Track search usage
    LaunchedEffect(state.searchQuery) {
        if (state.searchQuery.isNotEmpty()) {
            Analytics.track("country_search_used", mapOf("query_length" to state.searchQuery.length))
        }
    }
    
    // Custom screen implementation with analytics
    CustomCountrySelectionScreen(scope)
}
```

### Integration with Parent Form

```kotlin
@Composable
fun MyCardFormScreen() {
    val checkoutScope = Primer.checkout()
    val cardFormScope = checkoutScope.cardForm
    val cardFormState by cardFormScope.state.collectAsState()
    
    // Observe selected country from the form state
    val selectedCountry = cardFormState.data[PrimerInputElementType.COUNTRY_CODE]
    
    // Custom country selection trigger
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { cardFormScope.navigateToCountrySelection() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.LocationOn, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = selectedCountry?.let { "Selected: $it" } ?: "Select Country",
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
    }
    
    // Customize the country selection experience
    cardFormScope.selectCountry.countryItem = { country, onSelect ->
        EnhancedCountryItem(
            country = country,
            onSelect = onSelect,
            isSelected = selectedCountry == country.code.name
        )
    }
}

@Composable
fun EnhancedCountryItem(
    country: PrimerCountry,
    onSelect: (String, String) -> Unit,
    isSelected: Boolean
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(country.code.name, country.name) },
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getFlagEmoji(country.code),
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            Text(
                text = country.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
```

## Best Practices

### Error Handling and Loading States

```kotlin
@Composable
fun RobustCountrySelection() {
    val checkoutScope = Primer.checkout()
    val selectCountryScope = checkoutScope.cardForm.selectCountry
    
    // Custom screen with comprehensive error handling
    selectCountryScope.screen = {
        val state by selectCountryScope.state.collectAsState()
        
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Select Country") },
                navigationIcon = {
                    IconButton(onClick = selectCountryScope::onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
            
            when (state) {
                is PrimerSelectCountryScope.State -> {
                    if (state.isLoading) {
                        LoadingState()
                    } else if (state.countries.isEmpty()) {
                        ErrorState(onRetry = { /* Retry logic */ })
                    } else {
                        CountryContent(state, selectCountryScope)
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading countries...")
        }
    }
}

@Composable
fun ErrorState(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Failed to load countries",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please check your connection and try again",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
```

### Accessibility Support

```kotlin
@Composable
fun AccessibleCountryItem(
    country: PrimerCountry,
    onSelect: (String, String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = "Select ${country.name}",
                role = Role.Button
            ) { onSelect(country.code.name, country.name) }
            .semantics {
                contentDescription = "Country: ${country.name}, Code: ${country.code.name}"
                stateDescription = "Selectable"
            },
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getFlagEmoji(country.code),
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .semantics { contentDescription = "Flag of ${country.name}" }
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = country.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = country.code.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

### Performance Optimization

```kotlin
@Composable
fun OptimizedCountryList(
    countries: List<PrimerCountry>,
    onCountrySelect: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = countries,
            key = { country -> country.code.name } // Stable key for efficient recomposition
        ) { country ->
            // Stable lambda for performance
            val onSelect = remember(country) {
                { onCountrySelect(country.code.name, country.name) }
            }
            
            CountryListItem(
                country = country,
                onSelect = onSelect
            )
        }
    }
}
```

## Summary

`PrimerSelectCountryScope` provides flexible country selection capabilities with multiple levels of customization:

### Customization Levels:
- **Default**: Zero configuration, fully functional country selection
- **Component-Level**: Customize search bar, country items, or other specific components
- **Screen-Level**: Complete control over the entire country selection experience
- **Hybrid**: Mix default components with custom ones for targeted customization

### Key Benefits:
- **Comprehensive**: Handles data loading, search, selection, and navigation
- **Flexible**: Multiple customization options without breaking functionality
- **Performant**: Efficient search and list rendering with proper state management
- **Accessible**: Built-in accessibility support with customization options
- **Integrated**: Seamless integration with the checkout flow and navigation system

The scope architecture allows you to start with the default implementation and progressively enhance it with custom UI components while maintaining all the business logic and state management provided by the SDK.
