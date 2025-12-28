package com.shatrix.pixalize

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DetailActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var buttonDownload: Button
    private lateinit var buttonShare: Button
    private lateinit var buttonSetWallpaper: Button
    private lateinit var imageUrl: String
    private lateinit var imageName: String
    private var progressDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // AdMob ads are disabled in the public version
        // To enable ads, see README.md for setup instructions

        imageView = findViewById(R.id.image_view_wallpaper)
        buttonDownload = findViewById(R.id.button_download)
        buttonShare = findViewById(R.id.button_share)
        buttonSetWallpaper = findViewById(R.id.button_setwallpaper)

        // Set as wallpaper functionality
        buttonSetWallpaper.setOnClickListener {
            setAsWallpaper()
        }

        imageUrl = intent.getStringExtra("imageUrl") ?: ""
        imageName = intent.getStringExtra("imageName") ?: ""

        // Load image with placeholder
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .into(imageView)

        // Create modern progress dialog
        progressDialog = AlertDialog.Builder(this)
            .setMessage("Downloading image...")
            .setCancelable(false)
            .create()

        buttonDownload.setOnClickListener {
            downloadImage { filePath ->
                // Optional: You can show a message or update UI here after downloading
            }
        }
        buttonShare.setOnClickListener { shareImage() }
    }

    /**
     * Download the original image from URL (not from ImageView which may be scaled)
     */
    private fun downloadImage(onDownloadComplete: (String) -> Unit) {
        val filePath = "${externalCacheDir?.absolutePath}/$imageName.jpg"
        val file = File(filePath)
        
        // Check if file already exists (duplicate check)
        if (file.exists()) {
            onDownloadComplete(filePath)
            Toast.makeText(this, "Image already saved!", Toast.LENGTH_SHORT).show()
            return
        }
        
        progressDialog?.show()

        // Download original image from URL using Glide
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // Save to cache
                            FileOutputStream(filePath).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                                out.flush()
                            }
                            
                            // Save to gallery
                            saveImageToGallery(bitmap, imageName)

                            withContext(Dispatchers.Main) {
                                progressDialog?.dismiss()
                                onDownloadComplete(filePath)
                                Toast.makeText(this@DetailActivity, "Download complete!", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                progressDialog?.dismiss()
                                Snackbar.make(imageView, "Failed to download image: ${e.localizedMessage}", Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                
                override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                    progressDialog?.dismiss()
                    Snackbar.make(imageView, "Failed to load image", Snackbar.LENGTH_LONG).show()
                }
            })
    }
    
    private fun saveImageToGallery(bitmap: Bitmap, displayName: String): Boolean {
        // Check if image already exists in gallery
        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf("$displayName.jpg")
        
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.count > 0) {
                // Image already exists, skip saving
                return false
            }
        }
        
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Pixalize")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(uri, contentValues, null, null)
            }
            return true
        }
        return false
    }

    private fun shareImage() {
        downloadImage { filePath ->
            val file = File(filePath)
            Log.d("ShareImage", "File Path: $filePath")

            if (file.exists()) {
                try {
                    val uri: Uri = FileProvider.getUriForFile(
                        this,
                        "${packageName}.fileprovider",
                        file
                    )

                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT,
                            "Get more Wallpapers from the Pixalize app on the Play Store: https://play.google.com/store/apps/details?id=com.shatrix.pixalize")
                        putExtra(Intent.EXTRA_STREAM, uri)
                        type = "image/jpeg"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    startActivity(Intent.createChooser(shareIntent, "Share Image"))

                } catch (e: Exception) {
                    Log.e("ShareImage", "Error sharing image", e)
                    Snackbar.make(imageView, "Unable to share image", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Snackbar.make(imageView, "Image file not found", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setAsWallpaper() {
        downloadImage { filePath ->
            val file = File(filePath)
            Log.d("SetAsWallpaper", "File Path: $filePath")

            if (file.exists()) {
                try {
                    val uri: Uri = FileProvider.getUriForFile(
                        this,
                        "${packageName}.fileprovider",
                        file
                    )

                    val wallpaperIntent = Intent(Intent.ACTION_ATTACH_DATA).apply {
                        setDataAndType(uri, "image/jpeg")
                        putExtra("mimeType", "image/jpeg")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    startActivity(Intent.createChooser(wallpaperIntent, "Set as Wallpaper"))

                } catch (e: Exception) {
                    Log.e("SetAsWallpaper", "Error setting wallpaper", e)
                    Snackbar.make(imageView, "Unable to set wallpaper", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Snackbar.make(imageView, "Image file not found", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
