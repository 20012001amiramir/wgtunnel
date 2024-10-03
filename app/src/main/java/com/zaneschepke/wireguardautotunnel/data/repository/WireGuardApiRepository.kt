package com.zaneschepke.wireguardautotunnel.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.zaneschepke.wireguardautotunnel.data.network.ApiService
import com.zaneschepke.wireguardautotunnel.ui.screens.main.MainViewModel
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

class WireGuardConfigRepository @Inject constructor(
	private val apiService: ApiService,
	private val context: Context
) {

	suspend fun fetchAndSaveConfigToFile(id: String): Uri? {
		// Получаем ResponseBody из API
		val responseBody = apiService.getWireGuardConfig("$id.conf").body()

		// Проверяем, что ResponseBody не пустой
		responseBody?.let {
			// Создаём временный файл в кэше приложения
			val tempFilePath = File(context.cacheDir, "$id.conf").absolutePath

			// Сохраняем файл и получаем путь сохранения
			val savedFilePath = saveFile(it, tempFilePath)

			// Прочитаем содержимое файла и выведем его в лог
			val fileContent = File(savedFilePath).readText()
			Log.d("WireGuardConfigRepository", "Содержимое файла: $fileContent")

			// Возвращаем Uri файла
			return File(savedFilePath).toUri()
		}

		// Если responseBody пустой, возвращаем null
		return null
	}


	fun saveFile(body: ResponseBody?, pathWhereYouWantToSaveFile: String):String{
		if (body==null)
			return ""
		var input: InputStream? = null
		try {
			input = body.byteStream()
			//val file = File(getCacheDir(), "cacheFileAppeal.srl")
			val fos = FileOutputStream(pathWhereYouWantToSaveFile)
			fos.use { output ->
				val buffer = ByteArray(4 * 1024) // or other buffer size
				var read: Int
				while (input.read(buffer).also { read = it } != -1) {
					output.write(buffer, 0, read)
				}
				output.flush()
			}
			return pathWhereYouWantToSaveFile
		}catch (e:Exception){
			Log.e("saveFile",e.toString())
		}
		finally {
			input?.close()
		}
		return ""
	}
}
