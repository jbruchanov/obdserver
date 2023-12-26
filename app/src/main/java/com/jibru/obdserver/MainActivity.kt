@file:OptIn(ExperimentalMaterial3Api::class)

package com.jibru.obdserver

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jibru.obdserver.ui.theme.ObdServerTheme
import org.apache.commons.lang.StringEscapeUtils
import org.apache.commons.lang.StringUtils

class UiState {
    var service by mutableStateOf<ObdServerService?>(null)
    var hasPermissions by mutableStateOf(false)
    var selectedTab by mutableStateOf(0)

    var responseDelay by mutableStateOf(0L)
}

class MainActivity : ComponentActivity(), ObdServerEventHandler {

    private val uiState = UiState()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            uiState.service = (service as ObdServerBinder).service
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            uiState.service = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindService(Intent(this, ObdServerService::class.java), connection, Context.BIND_AUTO_CREATE)
        setContent {
            ObdServerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ObdServer(uiState, this@MainActivity)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        uiState.hasPermissions = permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    override fun onPermissionsClicked() {
        requestPermissions(permissions.toTypedArray(), 123)
    }

    override fun onStartStopClicked() {
        val service = uiState.service ?: return
        if (ObdServerService.isServerRunning.value) {
            service.stopAdvertising()
        } else {
            service.startAdvertising()
        }
    }

    override fun updateSettings() {
        val service = uiState.service ?: return
        service.responseDelay = uiState.responseDelay
    }

    companion object {
        val permissions = buildSet {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
            }
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }
}

interface ObdServerEventHandler {
    fun onPermissionsClicked()
    fun onStartStopClicked()
    fun updateSettings()
}

@Composable
fun ObdServer(uiState: UiState, eventHandler: ObdServerEventHandler) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {

        val service = uiState.service
        if (!uiState.hasPermissions) {
            Button(onClick = eventHandler::onPermissionsClicked) {
                Text("Permissions")
            }
        }

        if (service == null) {
            Text(text = "Waiting for service...")
        } else {
            TabRow(
                selectedTabIndex = uiState.selectedTab, modifier = Modifier.fillMaxWidth()
            ) {
                val tabs = remember { listOf("Server", "Responses") }
                tabs.forEachIndexed { index, s ->
                    Tab(selected = uiState.selectedTab == index, onClick = { uiState.selectedTab = index }, modifier = Modifier.height(48.dp)) {
                        Text(text = s)
                    }
                }
            }

            when (uiState.selectedTab) {
                0 -> ServerTab(uiState, eventHandler)
                1 -> Responses(uiState, eventHandler)
            }
        }
    }
}

@Composable
private fun ServerTab(uiState: UiState, eventHandler: ObdServerEventHandler) {
    val running by ObdServerService.isServerRunning.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Log:")
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(ObdServerService.serverLogs.size) {
                val item = ObdServerService.serverLogs.getOrNull(it)
                if (item != null) {
                    Text(
                        text = item,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
        Row {
            Button(onClick = eventHandler::onStartStopClicked, enabled = uiState.hasPermissions) {
                Text(if (running) "Stop" else "Start")
            }

            Button(onClick = { ObdServerService.serverLogs.clear() }, enabled = uiState.hasPermissions) {
                Text("Clear Log")
            }
        }
    }
}

@Composable
private fun Responses(uiState: UiState, eventHandler: ObdServerEventHandler) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeGesturesPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Delay:${uiState.responseDelay}ms")
        Slider(
            value = uiState.responseDelay.toFloat(),
            valueRange = 0f.rangeTo(5000f),
            onValueChange = { uiState.responseDelay = it.toLong() },
            onValueChangeFinished = eventHandler::updateSettings
        )

        Defaults.defaults
            .keys
            .sorted()
            .forEach { key ->
                val items = Defaults.defaults[key]
                items?.forEachIndexed { index, value ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val label = if (items.size > 1) "$key#$index" else key
                        Text(label, modifier = Modifier.width(64.dp))
                        TextField(
                            value = StringEscapeUtils.escapeJava(value),
                            onValueChange = { t -> Defaults.defaults[key]?.set(index, StringEscapeUtils.unescapeJava(t)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

            }
    }
}
