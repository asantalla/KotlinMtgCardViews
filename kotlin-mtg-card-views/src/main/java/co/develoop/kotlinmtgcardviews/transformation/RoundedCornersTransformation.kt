package co.develoop.kotlinmtgcardviews.transformation

import android.graphics.*
import com.squareup.picasso.Transformation

class RoundedCornersTransformation(val radius: Float) : Transformation {

    override fun transform(source: Bitmap?): Bitmap {
        val output: Bitmap = applyRoundedCornersTransformationToBitmap(source!!, radius)
        source.recycle()
        return output
    }

    override fun key(): String = "rounded_corners"

    companion object {
        fun applyRoundedCornersTransformationToBitmap(image: Bitmap, radius: Float): Bitmap {
            val output: Bitmap = Bitmap.createBitmap(image.width, image.height, image.config)

            val canvas = Canvas(output)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val rect = Rect(0, 0, image.width, image.height)

            canvas.drawARGB(0, 0, 0, 0)
            canvas.drawRoundRect(RectF(rect), radius, radius, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(image, rect, rect, paint)

            return output
        }
    }
}