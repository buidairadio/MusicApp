package com.uet.teamduan.musicplayer.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.uet.teamduan.musicplayer.R


object Utils {
    fun animateRecyclerView(context: Context, view: View){
//        val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
//        view.startAnimation(animation)
    }
    fun hideKeyBoard(activity: Activity){
        val im = activity.getSystemService(Activity.INPUT_METHOD_SERVICE)
        var view = activity.currentFocus
        if(view == null){
            view = View(activity)
        }
        (im as InputMethodManager).hideSoftInputFromWindow(view.windowToken,0)
    }
    fun showKeyBoard(activity: Activity){
        val im = activity.getSystemService(Activity.INPUT_METHOD_SERVICE)
        var view = activity.currentFocus
        if(view == null){
            view = View(activity)
        }
        (im as InputMethodManager).showSoftInput(view,InputMethodManager.SHOW_IMPLICIT)
    }
    @RequiresApi(Build.VERSION_CODES.M)
    fun checkPermission(context: Context, permissions: Array<String>): Array<String>? {
        var builder = StringBuilder()
        for (permission in permissions) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                builder.append(permission).append(";")
            }
        }
        val s = builder.toString()
        return if (s.equals(""))  null
        else s.split(";").toTypedArray()
    }

    fun DPToPX(dps: Int, context:Context): Int {
        val scale = context.getResources().getDisplayMetrics().density
        return (dps * scale + 0.5f).toInt()
    }
    fun map(a:Float, a1:Float, a2:Float,b1:Float, b2:Float):Float{
        return (a-a1)/(a2-a1)*(b2-b1)+b1
    }
}



fun setStatusBarGradiant(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val window = activity.window
        val background = ResourcesCompat.getDrawable(activity.resources, R.drawable.gradient_left_right,null)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ResourcesCompat.getColor(activity.resources,android.R.color.transparent,null)
        window.navigationBarColor = ResourcesCompat.getColor(activity.resources,android.R.color.transparent,null)
        window.setBackgroundDrawable(background)
    }
}

fun setStatusBarColor(activity: Activity,color:Int){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val window = activity.window
        val color = ResourcesCompat.getColor(activity.resources, color,null)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
        window.navigationBarColor = color
    }
}

