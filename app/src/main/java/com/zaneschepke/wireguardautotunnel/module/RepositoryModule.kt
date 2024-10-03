package com.zaneschepke.wireguardautotunnel.module

import android.content.Context
import androidx.room.Room
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.data.AppDatabase
import com.zaneschepke.wireguardautotunnel.data.DatabaseCallback
import com.zaneschepke.wireguardautotunnel.data.SettingsDao
import com.zaneschepke.wireguardautotunnel.data.TunnelConfigDao
import com.zaneschepke.wireguardautotunnel.data.datastore.DataStoreManager
import com.zaneschepke.wireguardautotunnel.data.network.ApiService
import com.zaneschepke.wireguardautotunnel.data.repository.AppDataRepository
import com.zaneschepke.wireguardautotunnel.data.repository.AppDataRoomRepository
import com.zaneschepke.wireguardautotunnel.data.repository.AppStateRepository
import com.zaneschepke.wireguardautotunnel.data.repository.DataStoreAppStateRepository
import com.zaneschepke.wireguardautotunnel.data.repository.RoomSettingsRepository
import com.zaneschepke.wireguardautotunnel.data.repository.RoomTunnelConfigRepository
import com.zaneschepke.wireguardautotunnel.data.repository.SettingsRepository
import com.zaneschepke.wireguardautotunnel.data.repository.TunnelConfigRepository
import com.zaneschepke.wireguardautotunnel.data.repository.WireGuardConfigRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
	@Provides
	@Singleton
	fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
		return Room.databaseBuilder(
			context,
			AppDatabase::class.java,
			context.getString(R.string.db_name),
		)
			.fallbackToDestructiveMigration()
			.addCallback(DatabaseCallback())
			.build()
	}

	@Singleton
	@Provides
	fun provideSettingsDoa(appDatabase: AppDatabase): SettingsDao {
		return appDatabase.settingDao()
	}

	@Singleton
	@Provides
	fun provideTunnelConfigDoa(appDatabase: AppDatabase): TunnelConfigDao {
		return appDatabase.tunnelConfigDoa()
	}

	@Provides
	@Singleton
	fun getGson(): Gson {
		return GsonBuilder().serializeNulls().setLenient().create()
	}

	@Provides
	@Singleton
	fun getInterceptor(): Interceptor {
		return Interceptor {
			val request = it.request().newBuilder()
//			request.addHeader("Authorization", "<Your token here>")
			val actualRequest = request.build()
			it.proceed(actualRequest)
		}
	}
	@Provides
	@Singleton
	fun getOkHttpClient(
		interceptor: Interceptor
	): OkHttpClient {
		val httpBuilder = OkHttpClient.Builder()
			.addInterceptor(interceptor)
			.addNetworkInterceptor(StethoInterceptor())
			.connectTimeout(30, TimeUnit.SECONDS)
			.readTimeout(30, TimeUnit.SECONDS)
			.writeTimeout(50, TimeUnit.SECONDS)

		return httpBuilder
			.protocols(mutableListOf(Protocol.HTTP_1_1))
			.build()
	}

	@Provides
	@Singleton
	fun getRetrofit(
		client: OkHttpClient,
		gson: Gson
	): Retrofit {
		return Retrofit.Builder()
			.baseUrl("http://185.125.101.21:8080/")
			.addConverterFactory(GsonConverterFactory.create(gson))
			.client(client)
			.build()
	}
	@Provides
	@Singleton
	fun getApiClient(retrofit: Retrofit): ApiService {
		return retrofit.create(ApiService:: class.java )
	}
	@Singleton
	@Provides
	fun provideTunnelConfigRepository(tunnelConfigDao: TunnelConfigDao, @IoDispatcher ioDispatcher: CoroutineDispatcher): TunnelConfigRepository {
		return RoomTunnelConfigRepository(tunnelConfigDao, ioDispatcher)
	}

	@Singleton
	@Provides
	fun provideSettingsRepository(settingsDao: SettingsDao, @IoDispatcher ioDispatcher: CoroutineDispatcher): SettingsRepository {
		return RoomSettingsRepository(settingsDao, ioDispatcher)
	}
	@Provides
	@Singleton
	fun provideWireGuardConfigRepository(@ApplicationContext context: Context, apiService: ApiService): WireGuardConfigRepository {
		return WireGuardConfigRepository(apiService, context)
	}

	@Singleton
	@Provides
	fun providePreferencesDataStore(@ApplicationContext context: Context, @IoDispatcher ioDispatcher: CoroutineDispatcher): DataStoreManager {
		return DataStoreManager(context, ioDispatcher)
	}

	@Provides
	@Singleton
	fun provideGeneralStateRepository(dataStoreManager: DataStoreManager): AppStateRepository {
		return DataStoreAppStateRepository(dataStoreManager)
	}

	@Provides
	@Singleton
	fun provideAppDataRepository(
		wireGuardConfigRepository: WireGuardConfigRepository,
		settingsRepository: SettingsRepository,
		tunnelConfigRepository: TunnelConfigRepository,
		appStateRepository: AppStateRepository,
	): AppDataRepository {
		return AppDataRoomRepository(wireGuardConfigRepository, settingsRepository, tunnelConfigRepository, appStateRepository)
	}
}
