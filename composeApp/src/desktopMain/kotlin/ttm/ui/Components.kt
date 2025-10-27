package ttm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppScaffold(
    title: String,
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null,
    topActions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = if (showBack && onBack != null) {
                    { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
                } else null,
                actions = topActions,
                elevation = 2.dp
            )
        },
        backgroundColor = MaterialTheme.colors.background,
        content = content
    )
}

@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier,
        elevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colors.surface
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content()
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(onClick = onClick, enabled = enabled) { Text(text) }
}

@Composable
fun DangerButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)) {
        Text(text, color = MaterialTheme.colors.onPrimary)
    }
}

@Composable
fun DropdownMenuBox(
    items: List<Pair<Long, String>>,
    selected: Long?,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expanded = true }) {
        Text(items.find { it.first == selected }?.second ?: "Select")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        items.forEach { (id, name) ->
            DropdownMenuItem(onClick = { onSelect(id); expanded = false }) { Text(name) }
        }
    }
}

@Composable
fun AdminAction(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin")
    }
}
