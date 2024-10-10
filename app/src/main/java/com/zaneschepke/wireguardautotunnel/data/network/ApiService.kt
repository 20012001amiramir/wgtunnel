package com.zaneschepke.wireguardautotunnel.data.network

import android.content.Context
import androidx.core.net.toUri
import com.zaneschepke.wireguardautotunnel.data.entity.HealthStatus
import com.zaneschepke.wireguardautotunnel.data.entity.Server
import com.zaneschepke.wireguardautotunnel.data.requests.ConfigRequest
import com.zaneschepke.wireguardautotunnel.data.requests.LoginRequest
import com.zaneschepke.wireguardautotunnel.data.responses.LoginResponse
import com.zaneschepke.wireguardautotunnel.ui.screens.main.MainViewModel
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import java.io.File

@JvmSuppressWildcards
interface ApiService {

	@GET("{id}")
	suspend fun getWireGuardConfig(@Path("id") id: String): Response<ResponseBody> // Ensure this is String

	// Получить список всех конфигов
	@GET("/client")
	suspend fun getAllConfigs(): Response<List<WireGuardConfig>>

	// Создать новый конфиг
	@POST("/client")
	@Headers("Content-Type: application/json")
	suspend fun createConfig(@Body config: ConfigRequest): Response<Void>

	// Удалить конфиг по UUID
	@DELETE("/client/{uuid}")
	suspend fun deleteConfig(@Path("uuid") uuid: String): Response<Void>

	// Авторизация пользователя
	@POST("/auth/login")
	@Headers("Content-Type: application/json")
	suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

	// Получить список серверов
	@GET("/list")
	suspend fun getServerList(): Response<List<Server>>

	// Проверить работоспособность сервера
	@GET("/health")
	suspend fun checkHealth(): Response<HealthStatus>

}
