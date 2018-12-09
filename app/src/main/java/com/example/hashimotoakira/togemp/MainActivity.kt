package com.example.hashimotoakira.togemp

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hashimotoakira.togemp.util.*
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.android.synthetic.main.activity_main.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnShowRationale
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class MainActivity : AppCompatActivity() {

    lateinit var token: String
    lateinit var name: String

    val endpointIds = mutableListOf<String>()

    private val discoveredEndpoints = HashMap<String, DiscoveredEndpointInfo>()

    // 検出の応答が返って来た際に呼ばれるコールバック
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String,
                                     discoveredEndpointInfo: DiscoveredEndpointInfo) {
            // 端末を検出した
            endpointIds.add(endpointId)
            logD("onEndpointFound  endpointID = $endpointId")
            discoveredEndpoints[endpointId] = discoveredEndpointInfo
            requestConnectionButton.isEnabled = true
        }

        override fun onEndpointLost(endpointId: String) {
            // 検出済みの端末を見失った
            // TODO endpointIdsで対応したものを消去する
            logD("onEndpointLost  endpointID = $endpointId")
            discoveredEndpoints.remove(endpointId)
        }
    }

    // 接続準備が出来た際に呼ばれるコールバック
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        // requestConnectionの後に呼ばれる
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            endpointIds.add(endpointId)

            name = connectionInfo.endpointName
            token = connectionInfo.authenticationToken
            logD("connectionLifecycleCallback onConnectionInitiated endpointId = $endpointId connectionInfo = $connectionInfo name = $name token = $token")
            acceptButton.isEnabled = true
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            logD("connectionLifecycleCallback onConnectionResult ${result.status.statusCode}")
        }

        override fun onDisconnected(endpointId: String) {
            logD("connectionLifecycleCallback onDisconnected endpointId = $endpointId")
        }
    }

    //
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadTransferUpdate(endpointId: String, payload: PayloadTransferUpdate) {
            logD(endpointId)
        }

        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payloadText.text = String(payload.asBytes()!!)
        }

    }

    var idCount = 0
    var idCount2 = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        advertisingButton.setOnClickListener {
            startAdvertisingWithPermissionCheck(connectionLifecycleCallback)
        }
        discoveringButton.setOnClickListener {
            startDiscoveryWithPermissionCheck(endpointDiscoveryCallback)
        }
        requestConnectionButton.setOnClickListener {
            logD("onEndpointFound  endpointID = ${endpointIds[idCount]}")

            requestConnection(
                    this,
                    packageName,
                    endpointIds[idCount],
                    connectionLifecycleCallback)
            idCount++
        }
        acceptButton.setOnClickListener {
            acceptConnections(this, endpointIds[idCount2], payloadCallback)
            idCount2++
        }
        sendPayload0Button.setOnClickListener {
            sendPayload(this, endpointIds[0], "good morning")
        }
        sendPayload1Button.setOnClickListener {
            sendPayload(this, endpointIds[1], "good night")
        }
    }

    // RuntimePermission用
    // PermissionDispatcherの関係でActivityに入れてある

    @OnShowRationale(Manifest.permission.BLUETOOTH)
    fun showRationaleForBluetooth(request: PermissionRequest) {
    }

    @OnShowRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
    fun showRationaleForAccess(request: PermissionRequest) {
    }

    // ここもNearbyConnectクラスに入れておきたい

    /**
     * 検出可能な状態にする
     *
     * ①　子が呼ぶ
     *
     * @param connectionLifecycleCallback 接続準備の通知が来た際の処理
     */

    @NeedsPermission(Manifest.permission.BLUETOOTH)
    fun startAdvertising(connectionLifecycleCallback: ConnectionLifecycleCallback) {
        Nearby.getConnectionsClient(this).startAdvertising(
                "Device A",
                packageName,
                connectionLifecycleCallback,
                advertisingOptions)
                .addOnSuccessListener {
                    logD("startAdvertising Succeeded")
                }
                .addOnFailureListener {
                    logD("startAdvertising Failed $it")
                }
    }


    /**
     * 検出可能な端末を検出する
     *
     * ②　親が呼ぶ
     *
     * @param endpointDiscoveryCallback 検出結果の通知が来た際の処理
     */
    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    fun startDiscovery(endpointDiscoveryCallback: EndpointDiscoveryCallback) {
        Nearby.getConnectionsClient(this).startDiscovery(
                packageName,
                endpointDiscoveryCallback,
                discoveryOptions
        )
                .addOnSuccessListener {
                    logD("startDiscovery Succeeded")
                }
                .addOnFailureListener {
                    logD("startDiscovery Failed")
                }
    }
}
