package com.uet.teamduan.musicplayer.visual_effects

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View

object AnimationEffects {

    fun touchBoundEffect(view: View){

        view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).setListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(p0: Animator?) {
                view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setListener(object:AnimatorListenerAdapter(){
                    override fun onAnimationEnd(p0: Animator?) {
                        view.scaleX = 1.0f
                        view.scaleY = 1.0f
                    }
                }).start()
            }
        }).start()
    }
}