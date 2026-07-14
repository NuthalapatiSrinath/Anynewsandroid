package com.anyNews.anynews.Utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import java.io.File

/**
 * Small Kotlin bridge so the (Java) reporter submission screen can build the
 * image-cropper options without dealing with Kotlin default-argument interop.
 *
 * Always uses an app-owned FileProvider [customOutputUri] so crop output never
 * depends on temporary Photo Picker / MediaStore grants (critical on Vivo).
 */
object CropHelper {

    private const val TAG = "REPORTER_IMAGE"

    @JvmStatic
    fun buildCropOptions(context: Context, uri: Uri?): CropImageContractOptions {
        val outputUri = createCropOutputUri(context)
        Log.e(TAG, "buildCropOptions source=$uri output=$outputUri")
        return CropImageContractOptions(
            uri = uri,
            cropImageOptions = CropImageOptions(
                guidelines = CropImageView.Guidelines.ON,
                outputCompressFormat = Bitmap.CompressFormat.JPEG,
                outputCompressQuality = 90,
                customOutputUri = outputUri,
                // Source was already picked in PostArticles — never re-open picker/camera.
                imageSourceIncludeGallery = false,
                imageSourceIncludeCamera = false,
                showIntentChooser = false,
            ),
        )
    }

    /** @deprecated Prefer [buildCropOptions] with Context so output URI is set. */
    @JvmStatic
    fun buildCropOptions(uri: Uri?): CropImageContractOptions {
        return CropImageContractOptions(
            uri = uri,
            cropImageOptions = CropImageOptions(
                guidelines = CropImageView.Guidelines.ON,
                outputCompressFormat = Bitmap.CompressFormat.JPEG,
                outputCompressQuality = 90,
                imageSourceIncludeGallery = false,
                imageSourceIncludeCamera = false,
                showIntentChooser = false,
            ),
        )
    }

    private fun createCropOutputUri(context: Context): Uri {
        val dir = File(context.cacheDir, "images")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "crop_" + System.currentTimeMillis() + ".jpg")
        return FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file,
        )
    }
}
