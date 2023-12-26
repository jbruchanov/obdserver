package com.jibru.obdserver

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import androidx.compose.runtime.mutableStateListOf
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*


data class ReceivedMessage(
    val bluetoothDevice: BluetoothDevice,
    val message: ByteArray
)

class ObdServerService : Service() {

    var responseDelay: Long = 0L

    private val responseCommands = MutableSharedFlow<ReceivedMessage>(extraBufferCapacity = 32)
    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder {
        return ObdServerBinder(this)
    }

    private val manager: BluetoothManager by lazy {
        applicationContext.getSystemService()!!
    }
    private val advertiser: BluetoothLeAdvertiser
        get() = manager.adapter.bluetoothLeAdvertiser

    val notifyCharacteristics = BluetoothGattCharacteristic(
        NTF_UUID,
        BluetoothGattCharacteristic.PROPERTY_BROADCAST or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
        BluetoothGattDescriptor.PERMISSION_READ,
    ).also { ch ->
        ch.addDescriptor(
            BluetoothGattDescriptor(
                NTF_UUID,
                BluetoothGattDescriptor.PERMISSION_WRITE or BluetoothGattDescriptor.PERMISSION_READ
            )
        )
    }

    private val service =
        BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY).also {
            it.addCharacteristic(
                BluetoothGattCharacteristic(
                    RW_UUID,
                    BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE or BluetoothGattCharacteristic.PERMISSION_READ,
                ),
            )
            it.addCharacteristic(notifyCharacteristics)
        }

    private lateinit var server: BluetoothGattServer

    override fun onCreate() {
        super.onCreate()
        // If we are missing permission stop the service
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            PackageManager.PERMISSION_GRANTED
        }

        if (permission == PackageManager.PERMISSION_GRANTED) {
            startInForeground()

            serverLogs.add("Opening GATT server...")
            server = manager.openGattServer(applicationContext, SampleServerCallback())
            server.addService(service)
        } else {
            serverLogs.add("Missing connect permission")
            stopSelf()
        }

        job = GlobalScope.launch {
            responseCommands
                .onEach { delay(responseDelay) }
                .collect { (device, message) ->
                    val responses = Defaults.getResponse(message)
                    responses.forEachIndexed { index, bytes ->
                        server.notifyCharacteristicChanged(
                            device,
                            notifyCharacteristics,
                            false,
                            bytes
                        )
                        if (responses.size - 1 > index) {
                            delay(25L)
                        }
                    }

                }
        }
    }

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }

    @SuppressLint("MissingPermission")
    fun startAdvertising() {
        serverLogs.add("Start advertising")
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .setTimeout(0)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()

        advertiser.startAdvertising(settings, data, SampleAdvertiseCallback)
        isServerRunning.value = true
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        serverLogs.add("Stop advertising")
        advertiser.stopAdvertising(SampleAdvertiseCallback)
        isServerRunning.value = false
        stopSelf()
    }

    private fun startInForeground() {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(applicationInfo.icon)
            .setContentTitle("GATT Server")
            .setContentText("Running...")
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                100,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE,
            )
        } else {
            startForeground(100, notification)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannelCompat.Builder(
            CHANNEL,
            NotificationManagerCompat.IMPORTANCE_HIGH,
        )
            .setName("GATT Server channel")
            .setDescription("Channel for the GATT server sample")
            .build()
        NotificationManagerCompat.from(this).createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    inner class SampleServerCallback : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(
            device: BluetoothDevice,
            status: Int,
            newState: Int,
        ) {
            serverLogs.add(
                "Connection state change: " +
                        "${newState.toConnectionStateString()}.\n" +
                        "> New device: ${device.name} ${device.address}"
            )
            // You should keep a list of connected device to manage them
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray,
        ) {
            serverLogs.add("Characteristic Write request: $requestId")
            serverLogs.add(" Data: ${String(value)} (offset $offset)")
            // Here you should apply the write of the characteristic and notify connected
            // devices that it changed

            if (characteristic.uuid == RW_UUID) {
                responseCommands.tryEmit(
                    ReceivedMessage(device, value.copyOf())
                )
            }
            // If response is needed reply to the device that the write was successful
            if (responseNeeded) {
                server.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    null,
                )
            }
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {
            serverLogs.add("Descriptor Write request: $requestId")
            serverLogs.add(" Data: ${String(value)} (offset $offset)")
            // Here you should apply the write of the characteristic and notify connected
            // devices that it changed

            if (descriptor.uuid == RW_UUID) {
                responseCommands.tryEmit(
                    ReceivedMessage(device, value.copyOf())
                )
            }
            // If response is needed reply to the device that the write was successful
            if (responseNeeded) {
                server.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    null,
                )
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?,
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            serverLogs.add("Characteristic Read request: $requestId (offset $offset)")
            val data = serverLogs.last().toByteArray()
            val response = data.copyOfRange(offset, data.size)
            server.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                response,
            )
        }

        override fun onDescriptorReadRequest(device: BluetoothDevice, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor)
            serverLogs.add("Descriptor Read request: $requestId (offset $offset)")
            val data = serverLogs.last().toByteArray()
            val response = data.copyOfRange(offset, data.size)
            server.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                response,
            )
        }

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
            serverLogs.add("MTU change request: $mtu")
        }
    }

    object SampleAdvertiseCallback : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            serverLogs.add("Started advertising")
        }

        override fun onStartFailure(errorCode: Int) {
            serverLogs.add("Failed to start advertising: $errorCode")
        }
    }

    companion object {

        // Random UUID for our service known between the client and server to allow communication
        val SERVICE_UUID: UUID = UUID.fromString("00001110-0000-1000-8000-00805f9b34fb")

        // Same as the service but for the characteristic
        val RW_UUID: UUID = UUID.fromString("00001111-0000-1000-8000-00805f9b34fb")
        val NTF_UUID: UUID = UUID.fromString("00001112-0000-1000-8000-00805f9b34fb")

        // Important: this is just for simplicity, there are better ways to communicate between
        // a service and an activity/view
        val serverLogs = mutableStateListOf<String>()
        val isServerRunning = MutableStateFlow(false)

        private const val CHANNEL = "gatt_server_channel"
    }
}

class ObdServerBinder(val service: ObdServerService) : Binder()

internal fun Int.toConnectionStateString() = when (this) {
    BluetoothProfile.STATE_CONNECTED -> "Connected"
    BluetoothProfile.STATE_CONNECTING -> "Connecting"
    BluetoothProfile.STATE_DISCONNECTED -> "Disconnected"
    BluetoothProfile.STATE_DISCONNECTING -> "Disconnecting"
    else -> "N/A"
}
