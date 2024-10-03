package com.zaneschepke.wireguardautotunnel.data.repository

import com.zaneschepke.wireguardautotunnel.data.domain.TunnelConfig
import timber.log.Timber
import javax.inject.Inject

class AppDataRoomRepository
@Inject
constructor(
	override val wireguardApi: WireGuardConfigRepository,
	override val settings: SettingsRepository,
	override val tunnels: TunnelConfigRepository,
	override val appState: AppStateRepository,
) : AppDataRepository {
	override suspend fun getPrimaryOrFirstTunnel(): TunnelConfig? {
		return tunnels.findPrimary().firstOrNull() ?: tunnels.getAll().firstOrNull()
	}

	override suspend fun getVpnConfig(): TunnelConfig? {
		// Replace this with your actual fetching logic
		return try {
			// Simulate network/database call

			// Replace with your actual fetching mechanism
			TunnelConfig(name = "ExampleTunnel", wgQuick = "wgConfigExample", amQuick = "amConfigExample")
		} catch (e: Exception) {
			Timber.e(e, "Error fetching VPN config")
			null
		}
	}

	override suspend fun getStartTunnelConfig(): TunnelConfig? {
		tunnels.getActive().let {
			if (it.isNotEmpty()) return it.first()
			return getPrimaryOrFirstTunnel()
		}
	}
}
