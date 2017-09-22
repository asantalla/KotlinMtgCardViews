package co.develoop.kotlinmtgcardviewssample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import io.magicthegathering.kotlinsdk.api.MtgCardApiClient
import io.magicthegathering.kotlinsdk.model.card.MtgCard
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.imageResource
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {

    private val manaIcons = mapOf(Pair("{1}", R.drawable.ic_1), Pair("{U}", R.drawable.ic_u))
    private val manaPattern = Pattern.compile("(\\{[A-Z0-9]+\\})")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MtgCardApiClient.getCardObservable(227676)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ card: MtgCard ->
                    mtgCardImageView.loadCard(card.multiverseid)
                    mtgCardNameTextView.text = card.name
                    mtgCardArtistTextView.text = card.artist
                    mtgCardTextTextView.text = card.text
                    drawManaIcons(card.manaCost)
                    mtgCardSetTextView.text = card.setName
                    mtgCardTypeTextView.text = card.type
                    mtgCardRarityTextView.text = card.rarity
                })
    }

    private fun drawManaIcons(manaCost: String): MutableList<String> {
        val manaList = mutableListOf<String>()
        val matcher = manaPattern.matcher(manaCost)
        while (matcher.find()) {
            val imageView = ImageView(this)
            imageView.imageResource = manaIcons.get(matcher.group())!!
            mtgCardManaCostLayout.addView(imageView)
        }
        return manaList
    }
}