package com.example.testingcamera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.graphics.RectF

class RadiusOverlayView : View{
    var bm: Bitmap? = null
    var cv: Canvas? = null
    var eraser: Paint? = null
    var eraserOutline: Paint? = null
    var colors: Int = 0

    constructor(context: Context?) : super(context) {
        Init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        Init()
    }

    fun OvC(color: Int){
        colors = color
        onOvalColor(cv!!)
    }

    constructor(
        context: Context?, attrs: AttributeSet?,
        defStyleAttr: Int,
    ) : super(context, attrs, defStyleAttr) {
        Init()
    }

    private fun Init() {
        eraser = Paint()
        eraserOutline = Paint()
        eraser!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        eraser!!.isAntiAlias = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw || h != oldh) {
            bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            cv = Canvas(bm!!)
        }
        super.onSizeChanged(w, h, oldw, oldh)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        eraser!!.style = Paint.Style.FILL;
        eraserOutline!!.style = Paint.Style.STROKE
        eraser!!.color = Color.GREEN
        eraser!!.isAntiAlias = true
        bm!!.eraseColor(Color.TRANSPARENT)
        eraserOutline!!.strokeWidth = 3F
        eraserOutline!!.color = Color.BLACK
        cv!!.drawColor(resources.getColor(R.color.material_on_surface_stroke))
        val oval = RectF(50f, 300f, width.toFloat() -50f, height.toFloat() - 300f)
        cv!!.drawOval(oval, eraser!!)
        canvas.drawOval(oval, eraserOutline!!)
        canvas.drawBitmap(bm!!, 0f, 0f, null)
        super.onDraw(canvas)
    }

    private fun onOvalColor(canvas: Canvas){
        eraserOutline!!.strokeWidth = 3F
        if (colors != 0){
            eraserOutline!!.color = colors
        }
        eraserOutline!!.style = Paint.Style.STROKE
        val oval = RectF(50f, 300f, width.toFloat() -50f, height.toFloat() - 300f)
        canvas.drawOval(oval, eraserOutline!!)
    }
}