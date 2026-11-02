package tech.nikku.learningapp.core.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import tech.nikku.learningapp.core.domain.repository.MediaRepository
import tech.nikku.learningapp.core.domain.repository.MediaResolution
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
  @ApplicationContext private val context: Context
) : MediaRepository {

  override suspend fun resolve(ref: String): MediaResolution {
    val cleanRef = ref.removePrefix("/")

    // 1. Check downloaded media in filesDir/media/...
    val downloadedFile = File(context.filesDir, "media/$cleanRef")
    if (downloadedFile.exists() && downloadedFile.length() > 0) {
      return MediaResolution.FilePath(downloadedFile.absolutePath)
    }

    // 2. Check bundled asset
    try {
      context.assets.open(cleanRef).use {
        return MediaResolution.AssetPath(cleanRef)
      }
    } catch (_: IOException) {
      // Not in assets under cleanRef
    }

    // 3. Alternative prefix check
    val altRef = if (cleanRef.startsWith("media/")) cleanRef.removePrefix("media/") else "media/$cleanRef"
    try {
      context.assets.open(altRef).use {
        return MediaResolution.AssetPath(altRef)
      }
    } catch (_: IOException) {
      // Not in assets under altRef
    }

    return MediaResolution.NotFound
  }
}
