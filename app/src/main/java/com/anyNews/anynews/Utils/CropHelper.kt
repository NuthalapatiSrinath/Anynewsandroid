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
 * CanHub crop options for reporter PostArticles.
 * Caller must pass an app-owned FileProvider URI (copy-before-crop) so CropImageActivity
 * does not depend on temporary Photo Picker grants.
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
                // Fit the whole image inside the view and zoom to it so the crop
                // frame starts ON the selected image, not spanning the full screen.
                scaleType = CropImageView.ScaleType.FIT_CENTER,
                autoZoomEnabled = true,
                cropShape = CropImageView.CropShape.RECTANGLE,
                // Free-form rectangular crop that initially hugs the image bounds.
                fixAspectRatio = false,
                // 0 padding => initial crop window matches the fitted image exactly.
                initialCropWindowPaddingRatio = 0f,
            ),
        )
    }

    /** Prefer [buildCropOptions] with Context so output URI is set. */
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
                scaleType = CropImageView.ScaleType.FIT_CENTER,
                autoZoomEnabled = true,
                cropShape = CropImageView.CropShape.RECTANGLE,
                fixAspectRatio = false,
                initialCropWindowPaddingRatio = 0f,
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
