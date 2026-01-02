package com.huanchengfly.tieba.core.ui.compose.settings

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.AlertDialog
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.theme.compose.settingsIconTint
import com.huanchengfly.tieba.core.theme.compose.settingsSubtitleColor
import com.huanchengfly.tieba.core.theme.compose.settingsTitleColor
import com.huanchengfly.tieba.core.ui.preferences.LocalPreferencesDataStore
import com.huanchengfly.tieba.core.ui.compose.widgets.Switch
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ThemeScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: androidx.compose.material.FabPosition = androidx.compose.material.FabPosition.End,
    isFloatingActionButtonDocked: Boolean = false,
    containerColor: Color = ExtendedTheme.colors.background,
    contentColor: Color = ExtendedTheme.colors.text,
    contentWindowInsets: WindowInsets = WindowInsets.systemBars,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar ?: {},
        bottomBar = bottomBar ?: {},
        snackbarHost = { snackbarHost() },
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        isFloatingActionButtonDocked = isFloatingActionButtonDocked,
        backgroundColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets
    ) { innerPadding ->
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Box(modifier = Modifier.padding(innerPadding)) {
                content(innerPadding)
            }
        }
    }
}

@Composable
fun SettingsSectionContainer(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = ExtendedTheme.colors.card,
    contentColor: Color = settingsTitleColor(),
    contentPadding: PaddingValues = SettingsDefaults.sectionPadding,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .padding(horizontal = SettingsDefaults.sectionHorizontalPadding, vertical = 8.dp)
            .fillMaxWidth(),
        shape = shape,
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = SettingsDefaults.itemPadding,
    backgroundColor: Color = Color.Transparent,
    shape: Shape = RectangleShape,
    onClick: (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    contentSpacing: Dp = SettingsDefaults.iconTextSpacing,
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = LocalIndication.current
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = indication,
            enabled = enabled,
            role = Role.Button,
            onClick = onClick
        )
    } else {
        Modifier
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = backgroundColor,
        contentColor = settingsTitleColor()
    ) {
        CompositionLocalProvider(LocalContentColor provides settingsTitleColor()) {
            val titleAlpha = if (enabled) ContentAlpha.high else ContentAlpha.disabled
            val subtitleAlpha = if (enabled) ContentAlpha.medium else ContentAlpha.disabled
            val hasSubtitle = subtitle != null
            val startPadding = contentPadding.calculateLeftPadding(LayoutDirection.Ltr)
            val endPadding = contentPadding.calculateRightPadding(LayoutDirection.Ltr)
            Row(
                modifier = clickableModifier
                    .fillMaxWidth()
                    .heightIn(min = SettingsDefaults.minItemHeight)
                    .padding(
                        start = startPadding,
                        end = endPadding,
                        top = SettingsDefaults.topPadding(hasSubtitle),
                        bottom = SettingsDefaults.bottomPadding(hasSubtitle)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingContent?.let {
                    CompositionLocalProvider(LocalContentColor provides settingsIconTint()) {
                        Box(
                            modifier = Modifier.sizeIn(
                                minWidth = SettingsDefaults.iconSlotMinWidth,
                                minHeight = SettingsDefaults.iconSlotMinHeight
                            ),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            it()
                        }
                    }
                }
                CompositionLocalProvider(
                    LocalContentAlpha provides titleAlpha,
                    LocalTextStyle provides MaterialTheme.typography.subtitle1
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = contentSpacing),
                        verticalArrangement = Arrangement.Center
                    ) {
                        title()
                    subtitle?.let { sub ->
                        CompositionLocalProvider(
                            LocalContentAlpha provides 1f,
                            LocalTextStyle provides MaterialTheme.typography.body2,
                            LocalContentColor provides settingsSubtitleColor()
                        ) {
                            sub()
                        }
                    }
                    }
                }
                trailingContent?.let {
                    Spacer(modifier = Modifier.width(SettingsDefaults.trailingSpacing))
                    CompositionLocalProvider(LocalContentColor provides settingsSubtitleColor()) {
                        it()
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDivider(
    modifier: Modifier = Modifier,
    color: Color = ExtendedTheme.colors.divider
) {
    Divider(
        modifier = modifier
            .padding(horizontal = SettingsDefaults.sectionHorizontalPadding),
        color = color,
        thickness = 0.5.dp
    )
}

object SettingsDefaults {
    val sectionPadding: PaddingValues = PaddingValues(vertical = 4.dp)
    val sectionHorizontalPadding: Dp = 16.dp
    val itemPadding: PaddingValues = PaddingValues(horizontal = 16.dp)
    val trailingSpacing: Dp = 24.dp
    val minItemHeight: Dp = 48.dp
    val iconSlotMinWidth: Dp = 40.dp
    val iconSlotMinHeight: Dp = 40.dp
    val iconTextSpacing: Dp = 16.dp
    private val singleLineVerticalPadding: Dp = 16.dp
    private val multiLineVerticalPadding: Dp = 16.dp

    fun topPadding(hasSubtitle: Boolean): Dp =
        if (hasSubtitle) multiLineVerticalPadding else singleLineVerticalPadding

    fun bottomPadding(hasSubtitle: Boolean): Dp =
        if (hasSubtitle) multiLineVerticalPadding else singleLineVerticalPadding
}

@Composable
fun SettingsSwitch(
    key: String,
    title: String,
    modifier: Modifier = Modifier,
    summary: (@Composable (Boolean) -> String?)? = null,
    defaultChecked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    enabled: Boolean = true,
    leadingContent: @Composable (() -> Unit)? = null
) {
    val datastore = LocalPreferencesDataStore.current
    val prefsFlow = remember { datastore.data }
    val prefs by prefsFlow.collectAsState(initial = null)
    val selectionKey = remember(key) { booleanPreferencesKey(key) }
    val scope = rememberCoroutineScope()
    val checked = prefs?.get(selectionKey) ?: defaultChecked

    fun persist(newValue: Boolean) {
        onCheckedChange?.invoke(newValue)
        scope.launch {
            datastore.edit { storage ->
                storage[selectionKey] = newValue
            }
        }
    }

    val subtitleComposable = summary?.let { provider ->
        @Composable {
            val summaryText = provider(checked)
            if (summaryText != null) {
                Text(text = summaryText)
            }
        }
    }
    SettingsItem(
        modifier = modifier,
        onClick = { persist(!checked) },
        enabled = enabled,
        leadingContent = leadingContent,
        title = { Text(text = title) },
        subtitle = subtitleComposable,
        trailingContent = {
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = { persist(it) }
            )
        }
    )
}

@Composable
fun SettingsListPicker(
    key: String,
    title: String,
    modifier: Modifier = Modifier,
    summary: (@Composable (String?) -> String?)? = null,
    defaultValue: String? = null,
    onValueChange: ((String) -> Unit)? = null,
    useSelectedAsSummary: Boolean = false,
    enabled: Boolean = true,
    leadingContent: @Composable (() -> Unit)? = null,
    entries: Map<String, String>,
    itemIcons: Map<String, @Composable () -> Unit> = emptyMap()
) {
    val datastore = LocalPreferencesDataStore.current
    val prefsFlow = remember { datastore.data }
    val prefs by prefsFlow.collectAsState(initial = null)
    val selectionKey = remember(key) { stringPreferencesKey(key) }
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    val selectedValue = prefs?.get(selectionKey) ?: defaultValue

    fun persist(newValue: String) {
        onValueChange?.invoke(newValue)
        scope.launch {
            datastore.edit { storage ->
                storage[selectionKey] = newValue
            }
        }
    }

    val subtitleContent = summary?.let { provider ->
        @Composable {
            val summaryText = provider(selectedValue)
            if (summaryText != null) {
                Text(text = summaryText)
            }
        }
    } ?: run {
        if (useSelectedAsSummary) {
            {
                val text = selectedValue?.let { entries[it] } ?: "Not Set"
                Text(text = text)
            }
        } else {
            null
        }
    }

    Column {
        SettingsItem(
            modifier = modifier,
            onClick = {
                if (enabled) {
                    expanded = true
                }
            },
            enabled = enabled,
            leadingContent = leadingContent,
            title = { Text(text = title) },
            subtitle = subtitleContent
        )
        Box(
            modifier = Modifier
                .padding(horizontal = SettingsDefaults.sectionHorizontalPadding)
        ) {
            DropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false }
            ) {
                entries.forEach { (value, label) ->
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            persist(value)
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            itemIcons[value]?.let {
                                Box(modifier = Modifier.padding(end = 8.dp)) {
                                    it()
                                }
                            }
                            Text(text = label)
                        }
                    }
                }
            }
        }
    }
}

data class SettingsTime(
    val hour: Int,
    val minute: Int,
) {
    fun format(): String = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
}

private fun parseSettingsTime(raw: String, fallback: SettingsTime): SettingsTime {
    val parts = raw.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull()
    val minute = parts.getOrNull(1)?.toIntOrNull()
    return if (hour != null && minute != null && hour in 0..23 && minute in 0..59) {
        SettingsTime(hour, minute)
    } else {
        fallback
    }
}

@Composable
fun SettingsTimePicker(
    key: String,
    title: String,
    modifier: Modifier = Modifier,
    defaultValue: String = "09:00",
    summary: ((SettingsTime) -> String?)? = null,
    onValueChange: ((SettingsTime) -> Unit)? = null,
    enabled: Boolean = true,
    leadingContent: @Composable (() -> Unit)? = null
) {
    val context = LocalContext.current
    val datastore = LocalPreferencesDataStore.current
    val prefsFlow = remember { datastore.data }
    val prefs by prefsFlow.collectAsState(initial = null)
    val selectionKey = remember(key) { stringPreferencesKey(key) }
    val scope = rememberCoroutineScope()

    val fallbackTime = remember(defaultValue) {
        parseSettingsTime(defaultValue, SettingsTime(9, 0))
    }
    val initialTime = remember(prefs, defaultValue) {
        val raw = prefs?.get(selectionKey) ?: defaultValue
        parseSettingsTime(raw, fallbackTime)
    }
    var selectedTime by remember { mutableStateOf(initialTime) }
    LaunchedEffect(initialTime) {
        selectedTime = initialTime
    }

    fun persist(newValue: SettingsTime) {
        selectedTime = newValue
        onValueChange?.invoke(newValue)
        scope.launch {
            datastore.edit { storage ->
                storage[selectionKey] = newValue.format()
            }
        }
    }

    val summaryContent: @Composable (() -> Unit)? = {
        val text = summary?.invoke(selectedTime) ?: selectedTime.format()
        Text(text = text)
    }

    SettingsItem(
        modifier = modifier,
        onClick = {
            if (!enabled) return@SettingsItem
            val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
            val dialog = android.app.TimePickerDialog(
                context,
                { _, hour, minute ->
                    persist(SettingsTime(hour, minute))
                },
                selectedTime.hour,
                selectedTime.minute,
                is24Hour
            )
            dialog.show()
        },
        enabled = enabled,
        leadingContent = leadingContent,
        title = { Text(text = title) },
        subtitle = summaryContent
    )
}

@Composable
fun SettingsTextField(
    key: String,
    title: String,
    modifier: Modifier = Modifier,
    dialogTitle: String? = null,
    defaultValue: String = "",
    summary: ((String) -> String?)? = null,
    onValueSaved: ((String) -> Unit)? = null,
    enabled: Boolean = true,
    leadingContent: @Composable (() -> Unit)? = null
) {
    val datastore = LocalPreferencesDataStore.current
    val prefsFlow = remember { datastore.data }
    val prefs by prefsFlow.collectAsState(initial = null)
    val selectionKey = remember(key) { stringPreferencesKey(key) }
    val scope = rememberCoroutineScope()
    var value by remember { mutableStateOf(defaultValue) }
    var textVal by remember { mutableStateOf(defaultValue) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(prefs) {
        prefs?.get(selectionKey)?.let {
            value = it
        }
    }

    fun persist(newValue: String) {
        onValueSaved?.invoke(newValue)
        scope.launch {
            datastore.edit { storage ->
                storage[selectionKey] = newValue
            }
        }
    }

    val subtitleContent: (@Composable (() -> Unit))? = summary?.let { provider ->
        @Composable {
            provider(value)?.let { summaryText ->
                Text(text = summaryText)
            }
        }
    } ?: run {
        if (value.isNotEmpty()) {
            {
                Text(text = value)
            }
        } else {
            null
        }
    }

    SettingsItem(
        modifier = modifier,
        onClick = {
            if (enabled) {
                textVal = value
                showDialog = true
            }
        },
        enabled = enabled,
        leadingContent = leadingContent,
        title = { Text(text = title) },
        subtitle = subtitleContent
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(text = dialogTitle ?: title)
            },
            text = {
                OutlinedTextField(
                    value = textVal,
                    onValueChange = { textVal = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    value = textVal
                    persist(textVal)
                    showDialog = false
                }) {
                    Text(text = "确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = "取消")
                }
            }
        )
    }
}
