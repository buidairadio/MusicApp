package com.uet.teamduan.musicplayer.visual_effects

import android.content.Context
import android.graphics.Paint
import android.support.v4.content.res.ResourcesCompat
import android.util.Log
import android.view.SurfaceView
import com.uet.teamduan.musicplayer.R
private const val TAG = "SOME_ANIMATION"
class SomeAnimation(context: Context): SurfaceView(context), Runnable{
    var running:Boolean = true
    private var animationThread:Thread? = null

    private var timeThisFrame:Long = 0
    private var fps:Double = 0.0
    private val paint = Paint()

    init{
        paint.color = ResourcesCompat.getColor(context.resources,R.color.red,null)
    }
    override fun run() {
        while(running){
            val startFrameTime = System.currentTimeMillis()
            update()
            draw()
            timeThisFrame = System.currentTimeMillis() - startFrameTime

            if(timeThisFrame>=1)
                fps = 1000.0/timeThisFrame
        }
    }
    private fun update(){

    }
    private fun draw(){
        if(holder.surface.isValid){
            val canvas = holder.lockCanvas()
            canvas.drawColor(ResourcesCompat.getColor(context.resources,R.color.colorAbsoluteTransparency,null))
            canvas.drawCircle(100.0f,100.0f,100.0f,paint)
            holder.unlockCanvasAndPost(canvas)
        }
    }



    fun pause(){
        running = false
        try{
            animationThread?.join()
        }catch (e:InterruptedException){
            Log.e(TAG,"Joining Thread")
        }
    }
    fun resume(){
        running = true
        animationThread = Thread(this)
        animationThread?.start()
    }
}