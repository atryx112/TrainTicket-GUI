package ui

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun DropdownMenuBox(
    items: List<Pair<Long, String>>,
    selected: Long?,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Button(onClick = { expanded = true }) {
        Text(items.find { it.first == selected }?.second ?: "Select")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        items.forEach { (id, name) ->
            DropdownMenuItem(onClick = { onSelect(id); expanded = false }) { Text(name) }
        }
    }
}
