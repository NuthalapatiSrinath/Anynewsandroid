package com.anyNews.anynews.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.permissionx.guolindev.PermissionX;

import java.util.List;

/**
 * Reporter media pick: ask only the permission needed for the chosen action, once.
 *
 * <ul>
 *   <li>Camera / record video → {@link Manifest.permission#CAMERA} only</li>
 *   <li>Gallery image/video via SAF / Photo Picker → no READ_MEDIA / storage</li>
 * </ul>
 *
 * Never request CAMERA + gallery storage together up front (that caused double OS
 * dialogs and first-upload failures when one grant completed mid-flow).
 */
public final class DeferredMediaPermissions {

    public interface GrantCallback {
        void onGranted();
    }

    private DeferredMediaPermissions() {
    }

    public static boolean hasCamera(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request CAMERA only (not storage). On grant, runs {@code onGranted} once.
     */
    public static void ensureCamera(
            @NonNull FragmentActivity activity,
            @Nullable GrantCallback onGranted) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || hasCamera(activity)) {
            if (onGranted != null) {
                onGranted.onGranted();
            }
            return;
        }
        PermissionX.init(activity)
                .permissions(Manifest.permission.CAMERA)
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted && onGranted != null) {
                        onGranted.onGranted();
                    } else if (!allGranted) {
                        Toast.makeText(activity,
                                "Camera permission is required to take a photo",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Gallery / SAF / Photo Picker do not need READ_MEDIA_* when using
     * ACTION_GET_CONTENT / OPEN_DOCUMENT / PickVisualMedia.
     * Prefer those so we never re-prompt for storage on first image pick.
     */
    public static void openImageGallery(
            @NonNull Activity activity,
            @Nullable ActivityResultLauncher<Intent> galleryLauncher,
            @Nullable ActivityResultLauncher<PickVisualMediaRequest> pickVisualMedia) {
        Intent getContent = new Intent(Intent.ACTION_GET_CONTENT);
        getContent.setType("image/*");
        getContent.addCategory(Intent.CATEGORY_OPENABLE);
        getContent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent openDocument = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        openDocument.addCategory(Intent.CATEGORY_OPENABLE);
        openDocument.setType("image/*");
        openDocument.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        // Avoid ACTION_PICK as primary: it often needs READ_MEDIA_IMAGES on API 33+.
        Intent chooser = Intent.createChooser(getContent, "Select Image");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{openDocument});

        if (galleryLauncher != null) {
            try {
                galleryLauncher.launch(chooser);
                return;
            } catch (ActivityNotFoundException e) {
                // fall through
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                activity.startActivityForResult(chooser, 1);
                return;
            } catch (ActivityNotFoundException e) {
                // fall through
            }
        }

        if (pickVisualMedia != null
                && ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(activity)) {
            try {
                pickVisualMedia.launch(
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build());
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Toast.makeText(activity, "No gallery app available on this device", Toast.LENGTH_LONG).show();
    }

    public static void openVideoGallery(
            @NonNull Activity activity,
            @Nullable ActivityResultLauncher<Intent> galleryLauncher) {
        Intent getContent = new Intent(Intent.ACTION_GET_CONTENT);
        getContent.setType("video/*");
        getContent.addCategory(Intent.CATEGORY_OPENABLE);
        getContent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent openDocument = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        openDocument.addCategory(Intent.CATEGORY_OPENABLE);
        openDocument.setType("video/*");
        openDocument.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        Intent chooser = Intent.createChooser(getContent, "Select Video");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{openDocument});

        if (galleryLauncher != null) {
            try {
                galleryLauncher.launch(chooser);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            activity.startActivityForResult(chooser, 1);
        } catch (Exception e) {
            Toast.makeText(activity, "No gallery app available on this device", Toast.LENGTH_LONG).show();
        }
    }

    /** Build camera capture intent and grant write/read flags to camera packages. */
    @Nullable
    public static Intent buildCameraIntent(@NonNull Activity activity, @NonNull Uri outputUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        List<ResolveInfo> resInfoList = activity.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            activity.grantUriPermission(
                    resolveInfo.activityInfo.packageName,
                    outputUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        return intent;
    }
}
