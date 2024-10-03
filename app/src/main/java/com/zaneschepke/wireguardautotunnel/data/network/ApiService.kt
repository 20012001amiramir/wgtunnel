package com.zaneschepke.wireguardautotunnel.data.network

import android.content.Context
import androidx.core.net.toUri
import com.zaneschepke.wireguardautotunnel.ui.screens.main.MainViewModel
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.File

@JvmSuppressWildcards
interface ApiService {

	@GET("{id}")
	suspend fun getWireGuardConfig(@Path("id") id: String): Response<ResponseBody> // Ensure this is String
}
