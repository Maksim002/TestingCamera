package com.example.testingcamera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.media.FaceDetector
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.priyankvasa.android.cameraviewex.Image
import com.priyankvasa.android.cameraviewex.Modes
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.os.Handler
import android.view.View
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private val REQUEST_CAMERA_PERMISSION = 200
    private var glideManager: RequestManager? = null
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        glideManager = Glide.with(this)
        RadiusOverlayView(this)

        handler.postDelayed(Runnable { // Do something after 5s = 500ms
            camera.enableCameraMode(Modes.CameraMode.CONTINUOUS_FRAME)
            camera.setContinuousFrameListener(maxFrameRate = 1f /*optional*/) { image: Image ->
                CoroutineScope(Dispatchers.IO).launch {
                    showFramePreview(image)
                }
            }
        }, 2000)
    }

    private fun openCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
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

    override fun onStart() {
        super.onStart()
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
        val previewFrameScale = 1f
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), (100 * previewFrameScale).toInt(), jpegDataStream)
        jpegData = jpegDataStream.toByteArray()
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        val bm = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size, options) ?: return
        val mat = Matrix()
        mat.postRotate(270f)
        val bmpRotate = Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height , mat, true)

        crop(bmpRotate)
    }

    fun crop(source: Bitmap): Bitmap? {
        val squaredBitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height)
        if (squaredBitmap != source) {
            source.recycle()
        }
        val bitmap = Bitmap.createBitmap(source.width, source.height, source.config)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val shader = BitmapShader(squaredBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        paint.shader = shader
        paint.isAntiAlias = true
        val oval = RectF(50f, 220f, source.width.toFloat() -50f, source.height.toFloat() - 220f)
        canvas.drawOval(oval, paint)
        squaredBitmap.recycle()
        try {
            facedetection(bitmap)
        }catch (e:Exception){}
        return bitmap
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
