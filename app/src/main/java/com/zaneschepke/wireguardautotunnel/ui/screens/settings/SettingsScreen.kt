package com.zaneschepke.wireguardautotunnel.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.wireguard.android.backend.Tunnel
import com.wireguard.android.backend.WgQuickBackend
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.WireGuardAutoTunnel
import com.zaneschepke.wireguardautotunnel.ui.common.ClickableIconButton
import com.zaneschepke.wireguardautotunnel.ui.common.config.ConfigurationToggle
import com.zaneschepke.wireguardautotunnel.ui.common.prompt.AuthorizationPrompt
import com.zaneschepke.wireguardautotunnel.ui.common.text.SectionTitle
import com.zaneschepke.wireguardautotunnel.util.FileUtils
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalPermissionsApi::class,
    ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    padding: PaddingValues,
    showSnackbarMessage: (String) -> Unit,
    focusRequester: FocusRequester
) {
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val fineLocationState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var currentText by remember { mutableStateOf("") }
    var isBackgroundLocationGranted by remember { mutableStateOf(true) }
    var didExportFiles by remember { mutableStateOf(false) }
    var showAuthPrompt by remember { mutableStateOf(false) }

    val screenPadding = 5.dp
    val fillMaxWidth = .85f


    //TODO add error collecting and displaying for WGTunnelErrors

    fun exportAllConfigs() {
        try {
            val files = uiState.tunnels.map { File(context.cacheDir, "${it.name}.conf") }
            files.forEachIndexed { index, file ->
                file.outputStream().use {
                    it.write(uiState.tunnels[index].wgQuick.toByteArray())
                }
            }
            FileUtils.saveFilesToZip(context, files)
            didExportFiles = true
            showSnackbarMessage(context.getString(R.string.exported_configs_message))
        } catch (e: Exception) {
            showSnackbarMessage(e.message!!)
        }
    }

    fun saveTrustedSSID() {
        if (currentText.isNotEmpty()) {
            scope.launch {
                try {
                    viewModel.onSaveTrustedSSID(currentText)
                    currentText = ""
                } catch (e: Exception) {
                    showSnackbarMessage(e.message ?: context.getString(R.string.unknown_error))
                }
            }
        }
    }

    fun isAllAutoTunnelPermissionsEnabled(): Boolean {
        return (
                isBackgroundLocationGranted &&
                        fineLocationState.status.isGranted &&
                        !viewModel.isLocationServicesNeeded()
                )
    }

    fun openSettings() {
        scope.launch {
            val intentSettings =
                Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
            intentSettings.data =
                Uri.fromParts("package", context.packageName, null)
            context.startActivity(intentSettings)
        }
    }

    fun checkFineLocationGranted() {
        isBackgroundLocationGranted = if (!fineLocationState.status.isGranted) {
            false
        } else {
            scope.launch {
                viewModel.setLocationDisclosureShown()
            }
            true
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val backgroundLocationState =
            rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        isBackgroundLocationGranted = if (!backgroundLocationState.status.isGranted) {
            false
        } else {
            SideEffect {
                viewModel.setLocationDisclosureShown()
            }
            true
        }
    }

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
        isBackgroundLocationGranted = if (!fineLocationState.status.isGranted) {
            false
        } else {
            SideEffect {
                viewModel.setLocationDisclosureShown()
            }
            true
        }
    }

        AnimatedVisibility(!uiState.isLocationDisclosureShown) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(padding)
            ) {
                Icon(
                    Icons.Rounded.LocationOff,
                    contentDescription = stringResource(id = R.string.map),
                    modifier =
                    Modifier
                        .padding(30.dp)
                        .size(128.dp)
                )
                Text(
                    stringResource(R.string.prominent_background_location_title),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(30.dp),
                    fontSize = 20.sp
                )
                Text(
                    stringResource(R.string.prominent_background_location_message),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(30.dp),
                    fontSize = 15.sp
                )
                Row(
                    modifier =
                    if (WireGuardAutoTunnel.isRunningOnAndroidTv(context)) {
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .padding(30.dp)
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = {
                        viewModel.setLocationDisclosureShown()
                    }) {
                        Text(stringResource(id = R.string.no_thanks))
                    }
                    TextButton(modifier = Modifier.focusRequester(focusRequester), onClick = {
                        openSettings()
                        viewModel.setLocationDisclosureShown()
                    }) {
                        Text(stringResource(id = R.string.turn_on))
                    }
                }
            }
        }

        AnimatedVisibility(showAuthPrompt) {
            AuthorizationPrompt(
                onSuccess = {
                    showAuthPrompt = false
                    exportAllConfigs()
                },
                onError = { error ->
                    showSnackbarMessage(error)
                    showAuthPrompt = false
                },
                onFailure = {
                    showAuthPrompt = false
                    showSnackbarMessage(context.getString(R.string.authentication_failed))
                }
            )
        }

        if (uiState.tunnels.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Text(
                    stringResource(R.string.one_tunnel_required),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(15.dp),
                    fontStyle = FontStyle.Italic
                )
            }
            return
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .clickable(indication = null, interactionSource = interactionSource) {
                    focusManager.clearFocus()
                }
        ) {
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier =
                (
                        if (WireGuardAutoTunnel.isRunningOnAndroidTv(context)) {
                            Modifier
                                .height(IntrinsicSize.Min)
                                .fillMaxWidth(fillMaxWidth)
                                .padding(top = 10.dp)
                        } else {
                            Modifier
                                .fillMaxWidth(fillMaxWidth)
                                .padding(top = 60.dp)
                        }
                        ).padding(bottom = 10.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.padding(15.dp)
                ) {
                    SectionTitle(
                        title = stringResource(id = R.string.auto_tunneling),
                        padding = screenPadding
                    )
                    ConfigurationToggle(
                        stringResource(id = R.string.tunnel_on_wifi),
                        enabled = !(uiState.settings.isAutoTunnelEnabled || uiState.settings.isAlwaysOnVpnEnabled),
                        checked = uiState.settings.isTunnelOnWifiEnabled,
                        padding = screenPadding,
                        onCheckChanged = {
                            viewModel.onToggleTunnelOnWifi()
                        },
                        modifier = Modifier.focusRequester(focusRequester)
                    )
                    AnimatedVisibility(visible = uiState.settings.isTunnelOnWifiEnabled) {
                        Column {
                            FlowRow(
                                modifier = Modifier
                                    .padding(screenPadding)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                uiState.settings.trustedNetworkSSIDs.forEach { ssid ->
                                    ClickableIconButton(
                                        onIconClick = {
                                            viewModel.onDeleteTrustedSSID(ssid)
                                        },
                                        text = ssid,
                                        icon = Icons.Filled.Close,
                                        enabled = !(uiState.settings.isAutoTunnelEnabled || uiState.settings.isAlwaysOnVpnEnabled)
                                    )
                                }
                                if (uiState.settings.trustedNetworkSSIDs.isEmpty()) {
                                    Text(
                                        stringResource(R.string.none),
                                        fontStyle = FontStyle.Italic,
                                        color = Color.Gray
                                    )
                                }
                            }
                            OutlinedTextField(
                                enabled = !(uiState.settings.isAutoTunnelEnabled || uiState.settings.isAlwaysOnVpnEnabled),
                                value = currentText,
                                onValueChange = { currentText = it },
                                label = { Text(stringResource(R.string.add_trusted_ssid)) },
                                modifier =
                                Modifier
                                    .padding(start = screenPadding, top = 5.dp, bottom = 10.dp)
                                    .onFocusChanged {
                                        if (WireGuardAutoTunnel.isRunningOnAndroidTv(context)) {
                                            keyboardController?.hide()
                                        }
                                    },
                                maxLines = 1,
                                keyboardOptions =
                                KeyboardOptions(
                                    capitalization = KeyboardCapitalization.None,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions =
                                KeyboardActions(
                                    onDone = {
                                        saveTrustedSSID()
                                    }
                                ),
                                trailingIcon = {
                                    if (currentText != "") {
                                        IconButton(onClick = { saveTrustedSSID() }) {
                                            Icon(
                                                imageVector = Icons.Outlined.Add,
                                                contentDescription =
                                                if (currentText == "") {
                                                    stringResource(
                                                        id = R.string.trusted_ssid_empty_description
                                                    )
                                                } else {
                                                    stringResource(
                                                        id = R.string.trusted_ssid_value_description
                                                    )
                                                },
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                    ConfigurationToggle(
                        stringResource(R.string.tunnel_mobile_data),
                        enabled = !(uiState.settings.isAutoTunnelEnabled || uiState.settings.isAlwaysOnVpnEnabled),
                        checked = uiState.settings.isTunnelOnMobileDataEnabled,
                        padding = screenPadding,
                        onCheckChanged = {
                            viewModel.onToggleTunnelOnMobileData()
                        }
                    )
                    ConfigurationToggle(
                        stringResource(id = R.string.tunnel_on_ethernet),
                        enabled = !(uiState.settings.isAutoTunnelEnabled || uiState.settings.isAlwaysOnVpnEnabled),
                        checked = uiState.settings.isTunnelOnEthernetEnabled,
                        padding = screenPadding,
                        onCheckChanged = {
                            viewModel.onToggleTunnelOnEthernet()
                        }
                    )
                    ConfigurationToggle(
                        stringResource(R.string.battery_saver),
                        enabled = !(uiState.settings.isAutoTunnelEnabled || uiState.settings.isAlwaysOnVpnEnabled),
                        checked = uiState.settings.isBatterySaverEnabled,
                        padding = screenPadding,
                        onCheckChanged = {
                            viewModel.onToggleBatterySaver()
                        }
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(top = 5.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            enabled = !uiState.settings.isAlwaysOnVpnEnabled,
                            onClick = {
                                if (!isAllAutoTunnelPermissionsEnabled() && uiState.settings.isTunnelOnWifiEnabled) {
                                    val message =
                                        if (!isBackgroundLocationGranted) {
                                            context.getString(R.string.background_location_required)
                                        } else if (viewModel.isLocationServicesNeeded()) {
                                            context.getString(R.string.location_services_required)
                                        } else {
                                            context.getString(R.string.precise_location_required)
                                        }
                                    showSnackbarMessage(message)
                                } else {
                                    viewModel.toggleAutoTunnel()
                                }
                            }
                        ) {
                            val autoTunnelButtonText =
                                if (uiState.settings.isAutoTunnelEnabled) {
                                    stringResource(R.string.disable_auto_tunnel)
                                } else {
                                    stringResource(id = R.string.enable_auto_tunnel)
                                }
                            Text(autoTunnelButtonText)
                        }
                    }
                }
            }
            if (WgQuickBackend.hasKernelSupport()) {
                Surface(
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth(fillMaxWidth)
                        .padding(vertical = 10.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier.padding(15.dp)
                    ) {
                        SectionTitle(
                            title = stringResource(id = R.string.kernel),
                            padding = screenPadding
                        )
                        ConfigurationToggle(
                            stringResource(R.string.use_kernel),
                            enabled = !(
                                    uiState.settings.isAutoTunnelEnabled || uiState.settings.isAlwaysOnVpnEnabled ||
                                            (uiState.tunnelState == Tunnel.State.UP)
                                    ),
                            checked = uiState.settings.isKernelEnabled,
                            padding = screenPadding,
                            onCheckChanged = {
                                viewModel.onToggleKernelMode()
                            }
                        )
                    }
                }
            }
            if (!WireGuardAutoTunnel.isRunningOnAndroidTv(context)) {
                Surface(
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth(fillMaxWidth)
                        .padding(vertical = 10.dp)
                        .padding(bottom = 140.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier.padding(15.dp)
                    ) {
                        SectionTitle(
                            title = stringResource(id = R.string.other),
                            padding = screenPadding
                        )
                        ConfigurationToggle(
                            stringResource(R.string.always_on_vpn_support),
                            enabled = !uiState.settings.isAutoTunnelEnabled,
                            checked = uiState.settings.isAlwaysOnVpnEnabled,
                            padding = screenPadding,
                            onCheckChanged = {
                                scope.launch {
                                    viewModel.onToggleAlwaysOnVPN()
                                }
                            }
                        )
                        ConfigurationToggle(
                            stringResource(R.string.enabled_app_shortcuts),
                            enabled = true,
                            checked = uiState.settings.isShortcutsEnabled,
                            padding = screenPadding,
                            onCheckChanged = {
                                scope.launch {
                                    viewModel.onToggleShortcutsEnabled()
                                }
                            }
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(top = 5.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                enabled = !didExportFiles,
                                onClick = {
                                    showAuthPrompt = true
                                }
                            ) {
                                Text(stringResource(R.string.export_configs))
                            }
                        }
                    }
                }
            }
            if (WireGuardAutoTunnel.isRunningOnAndroidTv(context)) {
                Spacer(modifier = Modifier.weight(.17f))
            }
        }
    }
