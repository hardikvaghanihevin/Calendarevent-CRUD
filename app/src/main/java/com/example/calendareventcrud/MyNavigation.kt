package com.example.calendareventcrud

import androidx.navigation.NavOptions
import com.example.calendareventcrud.R.*

object MyNavigation {
    val navOptions = NavOptions.Builder()
        .setEnterAnim(anim.slide_in_right)  // Custom enter animation (slide in from the right)
        .setExitAnim(anim.slide_out_left)   // Custom exit animation (slide out to the left)
        .setPopEnterAnim(anim.slide_in_left) // Animation when popping back (slide in from the left)
        .setPopExitAnim(anim.slide_out_right) // Animation when popping back (slide out to the right)
        .build()

   /* val navOptions = NavOptions.Builder()
        .setEnterAnim(R.anim.fade_in)  // Fragment fades in
        .setExitAnim(R.anim.fade_out)   // Fragment fades out
        .setPopEnterAnim(R.anim.fade_in) // Fragment fades in when popping
        .setPopExitAnim(R.anim.fade_out) // Fragment fades out when popping
        .build()*/

    /*val navOptions = NavOptions.Builder()
        .setEnterAnim(R.anim.zoom_in)  // Fragment zooms in
        //.setExitAnim(R.anim.zoom_out)   // Fragment zooms out
        //.setPopEnterAnim(R.anim.zoom_in) // Fragment zooms in when popping
        .setPopExitAnim(R.anim.zoom_out) // Fragment zooms out when popping
        .build()*/

}