package com.zaneschepke.wireguardautotunnel.data.network

data class WireGuardConfig(
	val id: String,
	val name: String,
	val enabled: Boolean,
	val address: String,
	val publicKey: String,
	val createdAt: String,
	val updatedAt: String,
	val downloadableConfig: Boolean,
	val persistentKeepalive: String,
	val latestHandshakeAt: String?,
	val transferRx: Long,
	val transferTx: Long
)
