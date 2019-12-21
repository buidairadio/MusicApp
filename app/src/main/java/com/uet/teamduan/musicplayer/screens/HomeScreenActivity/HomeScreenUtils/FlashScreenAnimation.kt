package com.uet.teamduan.musicplayer.screens.HomeScreenActivity.HomeScreenUtils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Handler
import android.view.View
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.HomeScreenActivity
import kotlinx.android.synthetic.main.activity_home_screen.*

object FlashScreenAnimation {
    fun startFlashScreen(homeScreenActivity: HomeScreenActivity){
        homeScreenActivity.tv_app_name.alpha = 0.0f
        homeScreenActivity.tv_company_name.alpha = 0.0f
        homeScreenActivity.tv_designer_name.alpha = 0.0f
        homeScreenActivity.tv_app_name.animate().alpha(1.0f).setDuration(700).setListener(object: AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
//                animate3(homeScreenActivity)
                Handler().postDelayed(object:Runnable{
                    override fun run(){
                        closeFlashScreen(homeScreenActivity)
                    }
                },500)
            }
        })
    }
    private fun animate2(homeScreenActivity: HomeScreenActivity){
        homeScreenActivity.tv_company_name.animate().alpha(1.0f).setListener(object: AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                animate3(homeScreenActivity)
            }
        })
    }
    private fun animate3(homeScreenActivity: HomeScreenActivity){
        homeScreenActivity.tv_designer_name.animate().alpha(1.0f).setDuration(500).setListener(object: AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                Handler().postDelayed(object:Runnable{
                    override fun run(){
                        closeFlashScreen(homeScreenActivity)
                    }
                },500)
            }
        })
    }
    fun closeFlashScreen(homeScreenActivity: HomeScreenActivity){
        homeScreenActivity.rl_flash_screen.animate().alpha(0.0f).setDuration(500).setListener(object: AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                homeScreenActivity.rl_flash_screen.visibility = View.GONE
            }
        })
    }



}