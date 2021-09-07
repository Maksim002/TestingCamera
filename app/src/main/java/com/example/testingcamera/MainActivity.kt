package com.example.testingcamera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.priyankvasa.android.cameraviewex.Image
import com.priyankvasa.android.cameraviewex.Modes
import kotlinx.android.synthetic.main.activity_main.*

import android.graphics.*
import android.media.FaceDetector
import android.os.Handler
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlin.Exception
import android.graphics.Bitmap
import android.view.SurfaceView
import android.view.animation.TranslateAnimation

import android.widget.Toast

import android.util.DisplayMetrics

import android.widget.RelativeLayout
import android.graphics.PorterDuff

import android.graphics.PorterDuffXfermode
import android.util.AttributeSet

class MainActivity : AppCompatActivity() {
    private val REQUEST_CAMERA_PERMISSION = 200
    private var glideManager: RequestManager? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        glideManager = Glide.with(this)
        RadiusOverlayView(this)

        camera.setCameraMode(Modes.CameraMode.CONTINUOUS_FRAME)
        camera.setContinuousFrameListener(maxFrameRate = 1f /*optional*/) { image: Image ->
            CoroutineScope(Dispatchers.IO).launch {
                showFramePreview(image)
            }
        }
    }

    private fun openCamera() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION)
        } else {
            camera.start()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                ) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            } else {
                openCamera()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        openCamera()
    }

    override fun onPause() {
        super.onPause()
        camera.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        camera.destroy()
    }

    private fun showFramePreview(image: Image) {
        val jpegData: ByteArray
        val yuvImage = YuvImage(
            image.data,
            ImageFormat.NV21,
            image.width,
            image.height,
            null
        )
        val jpegDataStream = ByteArrayOutputStream()
        val previewFrameScale = 0.4f
        yuvImage.compressToJpeg(
            Rect(0, 0, image.width, image.height),
            (100 * previewFrameScale).toInt(),
            jpegDataStream
        )
        jpegData = jpegDataStream.toByteArray()
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        val bm = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size, options) ?: return

        val mat = Matrix()
        mat.postRotate(90f)
        val bmpRotate = Bitmap.createBitmap(bm, 50, 300, bm.width -50, bm.height - 300, mat, true)
        try {
            facedetection(bmpRotate)
        }catch (e:Exception){}
    }

    fun facedetection(bitmap: Bitmap) {
        var numOfFaces = 1
        val mFaceDetector = FaceDetector(bitmap.width, bitmap.height, numOfFaces)
        val mFace = arrayOfNulls<FaceDetector.Face>(numOfFaces)
        numOfFaces = mFaceDetector.findFaces(bitmap, mFace)
        Log.v("------------->", "" + numOfFaces)

        if (numOfFaces != 0){
            grtgr.OvC(Color.GREEN)
        }else {
            grtgr.OvC(Color.BLACK)
        }
    }
}
