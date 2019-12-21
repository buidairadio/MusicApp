package com.uet.teamduan.musicplayer.visual_effects

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.utils.Utils

private const val TAG="WAVEFORM_VIEW"

class WaveformView(
    context: Context,
    attributes: AttributeSet
): View(context,attributes) {

    private var mBytes: ByteArray? = null
    private var mPoints: FloatArray? = null
    private var mRect = Rect()
    private var mForePaint = Paint()
    private var radiusDiff = 0.0f
    private var captureSizeRange:Int = 0
    val destRect = Rect()
    var strokeWidth = 10.0f
    var image:Bitmap
    init{
        mBytes = null
        mForePaint.setStrokeWidth(strokeWidth)
        mForePaint.setAntiAlias(true)
        mForePaint.setColor(Color.rgb(240, 241, 241))
        image = BitmapFactory.decodeResource(resources, R.drawable.ic_songs);
    }

    override fun onDraw(canvas: Canvas){

        super.onDraw(canvas)
        if (mBytes == null) {
            return
        }

        mRect.set(0, 0, width, height)

        var n = mBytes?.size!!/2-1
        n/=3
        var sum = 0.0f
        val innerRadius = width.toFloat()/4+radiusDiff


        if (mPoints == null || mPoints?.size!!.compareTo(n* 4)<0) {
            mPoints = FloatArray(n* 4)
        }
        for (i in 0 until n step 2) {

//            val rfk = mBytes!![2 * i]
//            val ifk = mBytes!![2 * i + 1]
//            val magnitude = (rfk * rfk + ifk * ifk).toFloat()
//            val dbValue = (magnitude/(255*255/5))
            val dbValue = Math.abs(mBytes!![i].toDouble())/255.0f
            sum+=dbValue.toFloat()
            displayInCircle(mPoints!!,i,dbValue.toFloat(),n,width.toFloat()/2,height.toFloat()/2,
                innerRadius,innerRadius*2)


//            mPoints!![i * 4] = (mRect.width() * i*10).toFloat() / (mBytes?.size!!-1).toFloat()
//
//            mPoints!![i * 4 + 1] = 0.0f
//
//            mPoints!![i * 4 + 2] = (mRect.width()*i*10).toFloat()/ (mBytes?.size!!-1).toFloat()
//
//            mPoints!![i * 4 + 3] = dbValue*mRect.height()

        }
        radiusDiff = Utils.map(sum/n.toFloat(),0.0f,1.0f,0.0f,1000.0f)
//        for (i in 0 until mBytes?.size!!-1) {


//            mPoints!![i * 4] = (mRect.width() * i).toFloat() / (mBytes?.size!!.minus(1)).toFloat()
//
//            mPoints!![i * 4 + 1] = mRect.height().toFloat() / 2 + (mBytes!![i] + 128).toByte() * (mRect.height() / 4) / 128
//
//            mPoints!![i * 4 + 2] = mRect.width().toFloat() * (i + 1) / (mBytes?.size!!.minus(1)).toFloat()
//
//            mPoints!![i * 4 + 3] = mRect.height().toFloat() / 2 + (mBytes!![i + 1] + 128).toByte() * (mRect.height() / 4) / 128

//        }
        canvas.drawLines(mPoints, mForePaint)
        destRect.set(width/2-innerRadius.toInt(),height/2-innerRadius.toInt(),width/2+innerRadius.toInt(),height/2+innerRadius.toInt())
        canvas.drawBitmap(image,null,destRect,null)

    }
    fun updateWaveform(waveform: ByteArray, captureSizeRange:Int){
        mBytes = waveform
        this.captureSizeRange = captureSizeRange
        invalidate()
    }

    fun displayInCircle(points:FloatArray,index:Int,value:Float, n:Int, posX:Float, posY:Float, radius:Float, radius2:Float){

        val angle = (index.toFloat()/n.toFloat())*360.0f
        val sinX = Math.cos(Math.toRadians(angle.toDouble()))
        val cosY = Math.sin(Math.toRadians(angle.toDouble()))

        val x1 = posX+sinX*radius
        val y1 = posY+cosY*radius
        val x2 = posX+sinX*(radius+radius2*value)
        val y2 = posY+cosY*(radius+radius2*value)

        points[index * 4] = x1.toFloat()
        points[index* 4 + 1] = y1.toFloat()
        points[index * 4 + 2] = x2.toFloat()
        points[index * 4 + 3] = y2.toFloat()
    }
}