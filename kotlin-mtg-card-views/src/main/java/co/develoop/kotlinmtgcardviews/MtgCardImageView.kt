package co.develoop.kotlinmtgcardviews

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
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import io.magicthegathering.kotlinsdk.api.MtgCardApiClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class MtgCardImageView : ImageView {

    var minWidth: Int = 669
    var minHeight: Int = 930
    var cardRadius: Float = 10f
    var cardBackRadius: Float = 26f
    var zoomFactor: Float = 1.5f

    var xDest: Float? = 0f
    var yDest: Float? = 0f

    lateinit var target: Target

    var isZoomed: Boolean = false

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
            if (isZoomed) {
                zoomOut(this)
                isZoomed = false
            } else {
                zoomIn(this)
                isZoomed = true
            }
        }

        return true
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

        if (multiverseId > 0) {
            setMtgCardBackToImageView()
            MtgCardApiClient.getCardObservable(multiverseId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ card ->
                        Picasso.with(context)
                                .load(card.imageUrl)
                                .transform(RoundedCornersTransformation(cardRadius))
                                .into(target)
                    }, {
                        setMtgCardBackToImageView()
                    })
        } else {
            setMtgCardBackToImageView()
        }
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

    private fun zoomIn(view: View) {
        val locationOnScreen: IntArray = IntArray(2)
        getLocationOnScreen(locationOnScreen)

        xDest = (resources.displayMetrics.widthPixels / 2 - view.measuredWidth / 2 - locationOnScreen[0]).toFloat()
        yDest = (resources.displayMetrics.heightPixels / 2 - view.measuredHeight / 2 - locationOnScreen[1]).toFloat()

        val translateX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, xDest as Float)
        val translateY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, yDest as Float)
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, zoomFactor)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, zoomFactor)

        val animator: ValueAnimator = ObjectAnimator.ofPropertyValuesHolder(view, translateX, translateY, scaleX, scaleY)
        animator.duration = 200
        animator.start()

        requestFocusFromTouch()
    }

    private fun zoomOut(view: View) {
        val translateX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0f)
        val translateY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f)
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f)

        val animator: ValueAnimator = ObjectAnimator.ofPropertyValuesHolder(view, translateX, translateY, scaleX, scaleY)
        animator.duration = 200
        animator.start()
    }
}