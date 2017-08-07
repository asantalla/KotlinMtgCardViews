package co.develoop.kotlinmtgcardviews

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
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
        val scaleFactor = attributes.getFloat(R.styleable.MtgCardImageView_card_scaleFactor, 1f)

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

    private fun zoomIn(v: View) {
        val anim = ScaleAnimation(
                1f, 1.5f, // Start and end values for the X axis scaling
                1f, 1.5f, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f) // Pivot point of Y scaling
        anim.fillAfter = true // Needed to keep the result of the animation
        anim.duration = 300
        v.startAnimation(anim)
    }

    private fun zoomOut(v: View) {
        val anim = ScaleAnimation(
                1.5f, 1f, // Start and end values for the X axis scaling
                1.5f, 1f, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f) // Pivot point of Y scaling
        anim.fillAfter = true // Needed to keep the result of the animation
        anim.duration = 300
        v.startAnimation(anim)
    }
}