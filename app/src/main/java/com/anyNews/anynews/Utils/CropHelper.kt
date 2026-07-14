package com.anyNews.anynews.Utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import java.io.File

/**
 * CanHub crop options helper.
 *
 * Reporter PostArticles matches production: gallery/camera → preview + upload with no crop.
 * Kept for optional callers; production parity does not launch CropImageActivity.
 */
object CropHelper {

    @JvmStatic
    fun buildCropOptions(context: Context, uri: Uri?): CropImageContractOptions {
        val outputUri = createCropOutputUri(context)
        return CropImageContractOptions(
            uri = uri,
            cropImageOptions = CropImageOptions(
                guidelines = CropImageView.Guidelines.ON,
                outputCompressFormat = Bitmap.CompressFormat.JPEG,
                outputCompressQuality = 90,
                customOutputUri = outputUri,
                imageSourceIncludeGallery = false,
                imageSourceIncludeCamera = false,
                showIntentChooser = false,
                scaleType = CropImageView.ScaleType.CENTER_INSIDE,
                autoZoomEnabled = true,
                cropShape = CropImageView.CropShape.RECTANGLE,
                fixAspectRatio = false,
                initialCropWindowPaddingRatio = 0.1f,
            ),
        )
    }

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
                scaleType = CropImageView.ScaleType.CENTER_INSIDE,
                autoZoomEnabled = true,
                cropShape = CropImageView.CropShape.RECTANGLE,
                fixAspectRatio = false,
                initialCropWindowPaddingRatio = 0.1f,
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
