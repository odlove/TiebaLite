package com.huanchengfly.tieba.post.ui.common.prefs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.huanchengfly.tieba.core.ui.compose.Container
import com.huanchengfly.tieba.core.ui.preferences.LocalPreferencesDataStore

/**
 * Main preference screen which holds [PrefsListItem]s
 *
 * @param dataStore DataStore which will be used to save all the preferences
 * @param modifier Modifier applied to the [LazyColumn] holding the list of Prefs
 *
 */
@Composable
fun PrefsScreen(
    dataStore: DataStore<Preferences>,
    modifier: Modifier = Modifier,
    dividerThickness: Dp = 1.dp, // 0 for no divider
    dividerIndent: Dp = 0.dp, // indents on both sides
    content: PrefsScope.() -> Unit
) {
    val prefsScope = PrefsScopeImpl().apply(content)

    // Now any子 Pref 都可以通过 LocalPreferencesDataStore.current 访问 dataStore
    CompositionLocalProvider(LocalPreferencesDataStore provides dataStore) {
        Container {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(modifier = modifier.fillMaxSize()) {

                    items(prefsScope.prefsItems.size) { index ->
                        prefsScope.getPrefsItem(index)()

                        if (dividerThickness != 0.dp
                            && index != prefsScope.prefsItems.size - 1
                            && !prefsScope.headerIndexes.contains(index)
                            && !prefsScope.headerIndexes.contains(index + 1)
                            && !prefsScope.footerIndexes.contains(index)
                        ) {
                            Divider(
                                thickness = dividerThickness,
                                indent = dividerIndent
                            )
                        }
                    }
                }
            }
        }
    }
}
