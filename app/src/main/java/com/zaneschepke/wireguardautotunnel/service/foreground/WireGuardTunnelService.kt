package com.zaneschepke.wireguardautotunnel.service.foreground

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.receiver.NotificationActionReceiver
import com.zaneschepke.wireguardautotunnel.service.notification.NotificationService
import com.zaneschepke.wireguardautotunnel.service.tunnel.HandshakeStatus
import com.zaneschepke.wireguardautotunnel.service.tunnel.VpnService
import com.zaneschepke.wireguardautotunnel.service.tunnel.model.TunnelConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WireGuardTunnelService : ForegroundService() {

    private val foregroundId = 123;

    @Inject
    lateinit var vpnService : VpnService

    @Inject
    lateinit var notificationService : NotificationService

    private lateinit var job : Job

    private var tunnelName : String = ""

    override fun startService(extras : Bundle?) {
        super.startService(extras)
        val tunnelConfigString = extras?.getString(getString(R.string.tunnel_extras_key))
        cancelJob()
        job = CoroutineScope(SupervisorJob()).launch {
            if(tunnelConfigString != null) {
                try {
                    val tunnelConfig = TunnelConfig.from(tunnelConfigString)
                    tunnelName = tunnelConfig.name
                    vpnService.startTunnel(tunnelConfig)
                } catch (e : Exception) {
                    Timber.e("Problem starting tunnel: ${e.message}")
                    stopService(extras)
                }
            } else {
                Timber.e("Tunnel config null")
            }
        }
        CoroutineScope(job).launch {
            var didShowConnected = false
            var didShowFailedHandshakeNotification = false
            vpnService.handshakeStatus.collect {
                when(it) {
                    HandshakeStatus.NOT_STARTED -> {
                    }
                    HandshakeStatus.NEVER_CONNECTED -> {
                        if(!didShowFailedHandshakeNotification) {
                            launchVpnConnectionFailedNotification(getString(R.string.initial_connection_failure_message))
                            didShowFailedHandshakeNotification = true
                        }
                    }
                    HandshakeStatus.HEALTHY -> {
                        if(!didShowConnected) {
                            launchVpnConnectedNotification()
                            didShowConnected = true
                        }
                    }
                    HandshakeStatus.UNHEALTHY -> {
                        if(!didShowFailedHandshakeNotification) {
                            launchVpnConnectionFailedNotification(getString(R.string.lost_connection_failure_message))
                            didShowFailedHandshakeNotification = true
                        }
                    }
                }
            }
        }
    }

    override fun stopService(extras : Bundle?) {
        super.stopService(extras)
        CoroutineScope(Dispatchers.IO).launch() {
            vpnService.stopTunnel()
        }
        cancelJob()
        stopSelf()
    }

    private fun launchVpnConnectedNotification() {
        val notification = notificationService.createNotification(
            channelId = getString(R.string.vpn_channel_id),
            channelName = getString(R.string.vpn_channel_name),
            title = getString(R.string.tunnel_start_title),
            onGoing = false,
            showTimestamp = true,
            description = "${getString(R.string.tunnel_start_text)} $tunnelName"
        )
        super.startForeground(foregroundId, notification)
    }

    private fun launchVpnConnectionFailedNotification(message : String) {
        val notification = notificationService.createNotification(
            channelId = getString(R.string.vpn_channel_id),
            channelName = getString(R.string.vpn_channel_name),
            action = PendingIntent.getBroadcast(this,0,Intent(this, NotificationActionReceiver::class.java),PendingIntent.FLAG_IMMUTABLE),
            actionText = getString(R.string.restart),
            title = getString(R.string.vpn_connection_failed),
            onGoing = false,
            showTimestamp = true,
            description = message
        )
        super.startForeground(foregroundId, notification)
    }


    private fun cancelJob() {
        if(this::job.isInitialized) {
            job.cancel()
        }
    }
}