package com.anyNews.anynews.Utils

import android.graphics.Bitmap
import android.net.Uri
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView

/**
 * Small Kotlin bridge so the (Java) reporter submission screen can build the
 * image-cropper options without dealing with Kotlin default-argument interop.
 */
object CropHelper {

    @JvmStatic
    fun buildCropOptions(uri: Uri?): CropImageContractOptions {
        return CropImageContractOptions(
            uri = uri,
            cropImageOptions = CropImageOptions(
                guidelines = CropImageView.Guidelines.ON,
                outputCompressFormat = Bitmap.CompressFormat.JPEG,
                outputCompressQuality = 90,
            ),
        )
    }
}
