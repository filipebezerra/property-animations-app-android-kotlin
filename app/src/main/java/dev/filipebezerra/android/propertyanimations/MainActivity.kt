package dev.filipebezerra.android.propertyanimations

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.doOnEnd
import androidx.databinding.DataBindingUtil
import dev.filipebezerra.android.propertyanimations.databinding.ActivityMainBinding
import dev.filipebezerra.android.propertyanimations.util.disableViewDuringAnimation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Create several kinds of animation like Rotation, Translation, Scalling, Fading in and out,
 * Colorizing different View properties and simulating complex animations in parallel.
 *
 * API's used here are:  [ObjectAnimator], [AnimatorSet], [LinearInterpolator], [PropertyValuesHolder]
 */
class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
            .apply {
                listOf(
                    rotateChip,
                    translateChip,
                    scaleChip,
                    fadeChip,
                    backgroundColorChip,
                    showerChip,
                ).forEach { chip -> chip.setOnClickListener { handleClickListener(it) } }
            }
            .also {
                viewBinding = it
            }
    }

    private fun handleClickListener(view: View) {
        when (view.id) {
            R.id.rotate_chip -> rotateStar()
            R.id.translate_chip -> translateStar()
            R.id.scale_chip -> scaleStar()
            R.id.fade_chip -> fadeStar()
            R.id.background_color_chip -> colorizeBackground()
            R.id.shower_chip -> starShower()
        }
    }

    // https://classroom.udacity.com/nanodegrees/nd940/parts/23bb2f5a-fe75-45e2-a108-212ab2b195c1/modules/716cd86f-fd2b-4c6d-92fc-d245b0bef01e/lessons/ac9c64a6-b5eb-40c7-8bfa-758987d83ce3/concepts/3e0a7991-7a9b-46b3-b36b-7197415982c6
    private fun rotateStar() =
        ObjectAnimator.ofFloat(viewBinding.star, View.ROTATION, -360f, 0f)
            .apply {
                duration = TimeUnit.SECONDS.toMillis(1)
                disableViewDuringAnimation(viewBinding.rotateChip)
            }
            .run { start() }

    // https://classroom.udacity.com/nanodegrees/nd940/parts/23bb2f5a-fe75-45e2-a108-212ab2b195c1/modules/716cd86f-fd2b-4c6d-92fc-d245b0bef01e/lessons/ac9c64a6-b5eb-40c7-8bfa-758987d83ce3/concepts/71c02f7f-4b2d-46bf-b1de-f8eb6e56851e
    private fun translateStar() =
        ObjectAnimator.ofFloat(viewBinding.star, View.TRANSLATION_X, 200f)
            .apply {
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = 1
                disableViewDuringAnimation(viewBinding.translateChip)
            }
            .run { start() }

    // https://classroom.udacity.com/nanodegrees/nd940/parts/23bb2f5a-fe75-45e2-a108-212ab2b195c1/modules/716cd86f-fd2b-4c6d-92fc-d245b0bef01e/lessons/ac9c64a6-b5eb-40c7-8bfa-758987d83ce3/concepts/e6b4ef66-0c9c-439a-b88e-24057849861d
    private fun scaleStar() =
        ObjectAnimator.ofPropertyValuesHolder(
            viewBinding.star,
            // Scaling to a value of 4f means the star will scale to 4 times its default size
            // X and Y need to be animated both of these in separate properties in parallel
            PropertyValuesHolder.ofFloat(View.SCALE_X, 4f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 4f)
        ).apply {
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = 1
            disableViewDuringAnimation(viewBinding.scaleChip)
        }.run { start() }

    // https://classroom.udacity.com/nanodegrees/nd940/parts/23bb2f5a-fe75-45e2-a108-212ab2b195c1/modules/716cd86f-fd2b-4c6d-92fc-d245b0bef01e/lessons/ac9c64a6-b5eb-40c7-8bfa-758987d83ce3/concepts/10d9c22e-dcdf-47c6-9d83-04a2395be775
    private fun fadeStar() =
    //  “alpha” is a term generally used, especially in computer graphics, to denote the amount
    //  of opacity in an object. A value of 0 indicates that the object is completely transparent,
    //  and a value of 1 indicates that the object is completely opaque. View objects have a
    //  default value of 1. Animations that fade views in or out animate the alpha value
        //  between 0 and 1.
        ObjectAnimator.ofFloat(viewBinding.star, View.ALPHA, 0f)
            .apply {
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = 1
                disableViewDuringAnimation(viewBinding.fadeChip)
            }.run { start() }

    // https://classroom.udacity.com/nanodegrees/nd940/parts/23bb2f5a-fe75-45e2-a108-212ab2b195c1/modules/716cd86f-fd2b-4c6d-92fc-d245b0bef01e/lessons/ac9c64a6-b5eb-40c7-8bfa-758987d83ce3/concepts/85bae4b0-f7ae-4ae8-870e-ead0ef4886c2
    private fun colorizeBackground() =
        ObjectAnimator.ofArgb(
            viewBinding.sky,
            "backgroundColor",
            Color.BLACK,
            Color.RED
        )
            .apply {
                duration = TimeUnit.SECONDS.toMillis(3)
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = 1
                disableViewDuringAnimation(viewBinding.backgroundColorChip)
            }.run { start() }

    // https://classroom.udacity.com/nanodegrees/nd940/parts/23bb2f5a-fe75-45e2-a108-212ab2b195c1/modules/716cd86f-fd2b-4c6d-92fc-d245b0bef01e/lessons/ac9c64a6-b5eb-40c7-8bfa-758987d83ce3/concepts/4c4860d9-982c-4cef-9bcb-61314aa55f72
    private fun starShower() {
        fun createAnimatorSet() {
            // Step 1: A Star is Born
            val container = viewBinding.sky
            val containerW = container.width
            val containerH = container.height
            val star = viewBinding.star
            var starW = star.width.toFloat()
            var starH = star.height.toFloat()

            // instantiate te ImageView and add to the FrameLayout
            val newStar = AppCompatImageView(this).apply {
                setImageResource(R.drawable.ic_star)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            }.also { container.addView(it) }

            // Step 2: Sizing and positioning the star
            // set star size and translate the X axis to random number
            newStar.scaleX = Math.random().toFloat() * 1.5f + .1f
            newStar.scaleY = newStar.scaleX
            starW *= newStar.scaleX
            starH *= newStar.scaleY
            newStar.translationX = Math.random().toFloat() * containerW - starW / 2

            // Step 3: Creating animators for star rotation and falling
            val fallingAnimation = ObjectAnimator.ofFloat(
                newStar,
                View.TRANSLATION_Y,
                -starH,
                containerH + starH
            )

            // Step 4: Running the animations in parallel with AnimatorSet

            // causes a gentle acceleration motion
            fallingAnimation.interpolator = AccelerateInterpolator(1f)

            val rotationAnimation = ObjectAnimator.ofFloat(
                newStar,
                View.ROTATION,
                (Math.random() * 1080).toFloat()
            )
            // rotation will proceed at a constant rate as the star falls
            rotationAnimation.interpolator = LinearInterpolator()

            AnimatorSet().apply {
                playTogether(fallingAnimation, rotationAnimation)
                duration = (Math.random() * 1500 + 500).toLong()
                doOnEnd { container.removeView(newStar) }
            }.run { start() }
        }

        GlobalScope.launch(context = Dispatchers.Main) {
            do {
                createAnimatorSet()
                delay((Math.random() * 1000 + 1000).toLong())
            } while (viewBinding.showerChip.isChecked)
        }
    }
}