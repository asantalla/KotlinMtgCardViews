package co.develoop.kotlinmtgcardviews

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import co.develoop.kotlinmtgcardviews.transformation.RoundedCornersTransformation
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

class MtgCardImageView : ImageView {

    private lateinit var target: Target

    private var minWidth: Int = 669
    private var minHeight: Int = 930
    private var cardRadius: Float = 10f
    private var cardBackRadius: Float = 26f
    private var zoomFactor: Float = 1.5f
    private var xDest: Float? = 0f
    private var yDest: Float? = 0f
    private var isZoomed: Boolean = false

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event!!.action == MotionEvent.ACTION_UP) {
            isZoomed = if (isZoomed) {
                zoomOut(this)
            } else {
                zoomIn(this)
            }
        }

        return true
    }

    fun loadCard(multiverseId: Int?) {
        if (multiverseId != null && multiverseId > 0) {

            Picasso.with(context)
                    .load(resources.getString(R.string.gatherer_wizards_card_url).replace("[multiverseId]", "$multiverseId"))
                    .transform(RoundedCornersTransformation(cardRadius))
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(target)
        }
    }

    fun reset() {
        setMtgCardBackToImageView()
        isZoomed = zoomOut(this)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        initTarget()

        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.MtgCardImageView, 0, 0)

        val multiverseId: Int = attributes.getInteger(R.styleable.MtgCardImageView_card_multiverseId, -1)
        val scaleFactor = attributes.getFloat(R.styleable.MtgCardImageView_card_scale, 1f)
        zoomFactor = attributes.getFloat(R.styleable.MtgCardImageView_card_zoom, 1.5f)

        minWidth = (minWidth.toFloat() * scaleFactor).toInt()
        minHeight = (minHeight.toFloat() * scaleFactor).toInt()
        cardRadius = (cardRadius * scaleFactor)
        cardBackRadius = (cardBackRadius * scaleFactor)

        setMtgCardBackToImageView()
        loadCard(multiverseId)
    }

    private fun initTarget() {
        target = object : Target {

            override fun onBitmapFailed(errorDrawable: Drawable?) {
                setMtgCardBackToImageView()
            }

            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                setImageBitmap(Bitmap.createScaledBitmap(bitmap, minWidth, minHeight, false))
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                setMtgCardBackToImageView()
            }
        }
    }

    private fun setMtgCardBackToImageView() {
        val source: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_mtg_card_back)
        val image: Bitmap = Bitmap.createScaledBitmap(source, minWidth, minHeight, false)
        setImageBitmap(RoundedCornersTransformation.applyRoundedCornersTransformationToBitmap(image, cardBackRadius))
    }

    private fun zoomIn(view: View): Boolean {
        val locationOnScreen = IntArray(2)
        getLocationOnScreen(locationOnScreen)

        xDest = (resources.displayMetrics.widthPixels / 2 - view.measuredWidth / 2 - locationOnScreen[0]).toFloat()
        yDest = (resources.displayMetrics.heightPixels / 2 - view.measuredHeight / 2 - locationOnScreen[1]).toFloat()

        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, zoomFactor)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, zoomFactor)
        val scaleAnimator: ValueAnimator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY)
        scaleAnimator.duration = 200

        val translateX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, xDest as Float)
        val translateY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, yDest as Float)
        val translateAnimator: ValueAnimator = ObjectAnimator.ofPropertyValuesHolder(view, translateX, translateY)
        translateAnimator.duration = 200

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleAnimator, translateAnimator)
        animatorSet.start()

        return true
    }

    private fun zoomOut(view: View): Boolean {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f)
        val scaleAnimator: ValueAnimator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY)
        scaleAnimator.duration = 200

        val translateX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0f)
        val translateY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f)
        val translateAnimator: ValueAnimator = ObjectAnimator.ofPropertyValuesHolder(view, translateX, translateY)
        translateAnimator.duration = 200

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleAnimator, translateAnimator)
        animatorSet.start()

        return false
    }
}