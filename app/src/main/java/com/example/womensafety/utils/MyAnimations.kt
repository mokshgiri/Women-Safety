package com.example.womensafety.utils

import android.content.Context
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.example.womensafety.R
import de.hdodenhof.circleimageview.CircleImageView

object MyAnimations {
    fun startMicrophoneAnimation(context : Context, btn: CircleImageView) {
        val animation = AnimationUtils.loadAnimation(context, R.anim.microphone_animation)

        animation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation) {}
            override fun onAnimationRepeat(animation: android.view.animation.Animation) {}
        })
        btn.startAnimation(animation)
    }

    fun startRefreshButtonAnimation(context: Context, refreshBtn: CircleImageView) {
        val rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_animation)
        rotateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {}

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        refreshBtn.startAnimation(rotateAnimation)
    }
}