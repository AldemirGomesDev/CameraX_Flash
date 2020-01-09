package com.android.camerax

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.TextureView
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)


class MainActivity : AppCompatActivity() {

    private var lensFacing = CameraX.LensFacing.BACK
    private var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Takes an images and saves it in the local storage
        fab_camera.setOnClickListener {
            val filename = "image" + System.currentTimeMillis() + ".png"
            val sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val dest = File(sd, filename)
            imageCapture?.takePicture(dest,
                object : ImageCapture.OnImageSavedListener {
                    override fun onError(error: ImageCapture.UseCaseError,
                                         message: String, exc: Throwable?) {
                        Log.e("Image", error.toString())
                    }
                    override fun onImageSaved(file: File) {
                        Log.v("Image", "Successfully saved image")
                    }
                })
        }

        // Changes the flash mode when the button is clicked
        fab_flash.setOnClickListener {
            val flashMode = imageCapture?.flashMode
            if(flashMode == FlashMode.ON) imageCapture?.flashMode = FlashMode.OFF
            else imageCapture?.flashMode = FlashMode.ON
        }

        // Changes the lens direction if the button is clicked
        fab_switch_camera.setOnClickListener {
            lensFacing = if (CameraX.LensFacing.FRONT == lensFacing) {
                CameraX.LensFacing.BACK
            } else {
                CameraX.LensFacing.FRONT
            }
            bindCamera()
        }
    }

    /**
     * Check if the app has all permissions
     */
    private fun hasNoPermissions(): Boolean{
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request all permissions
     */
    private fun requestPermission(){
        ActivityCompat.requestPermissions(this, permissions,0)
    }

    /**
     * Bind the Camera to the lifecycle
     */
    private fun bindCamera(){
        CameraX.unbindAll()

        // Preview config for the camera
        val previewConfig = PreviewConfig.Builder()
            .setLensFacing(lensFacing)
            .build()

        val preview = Preview(previewConfig)

        // Image capture config which controls the Flash and Lens
        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .setTargetRotation(windowManager.defaultDisplay.rotation)
            .setLensFacing(lensFacing)
            .setFlashMode(FlashMode.ON)
            .build()

        imageCapture = ImageCapture(imageCaptureConfig)

        // The view that displays the preview
        val textureView: TextureView = findViewById(R.id.view_finder)

        // Handles the output data of the camera
        preview.setOnPreviewOutputUpdateListener { previewOutput ->
            // Displays the camera image in our preview view
            textureView.surfaceTexture = previewOutput.surfaceTexture
        }

        // Bind the camera to the lifecycle
        CameraX.bindToLifecycle(this as LifecycleOwner, imageCapture, preview)
    }

    override fun onResume() {
        super.onResume()
        // Check and request permissions
        if (hasNoPermissions()) {
            requestPermission()
        }else {
            bindCamera()
        }
    }
    override fun onStart() {
        super.onStart()


    }

}
