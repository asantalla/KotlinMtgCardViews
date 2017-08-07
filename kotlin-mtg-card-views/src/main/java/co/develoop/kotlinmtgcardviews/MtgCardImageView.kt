package co.develoop.kotlinmtgcardviews

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import co.develoop.kotlinmtgcardviews.transformation.RoundedCornersTransformation
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import io.magicthegathering.kotlinsdk.api.MtgCardApiClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MtgCardImageView : ImageView {

    val minWidth: Int = 669
    val minHeight: Int = 930

    lateinit var target: Target

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        initTarget()

        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.MtgCardImageView, 0, 0)

        val multiverseId: Int = attributes.getInteger(R.styleable.MtgCardImageView_card_multiverseid, -1)

        if (multiverseId > 0) {
            setMtgCardBackToImageView()
            MtgCardApiClient.getCardObservable(multiverseId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ card ->
                        Picasso.with(context)
                                .load(card.imageUrl)
                                .transform(RoundedCornersTransformation(10f))
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
        setImageBitmap(RoundedCornersTransformation.applyRoundedCornersTransformationToBitmap(image, 25f))
    }
}