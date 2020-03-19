package com.example.yelpdiscovery

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.net.URLEncoder

public class LineItem(
    val context: MainActivity,
    val name: String,
    val address: String,
    val businessURL: String,
    val imgURLs: List<String>,
    val categories: String,
    val rating: String,
    val reviewCount: Int,
    val reviewData: ArrayList<ReviewData>
) {
    /*
     * UI elements
     */

    private lateinit var container: TableLayout
    private lateinit var imageRow: LinearLayout
    private lateinit var image: ArrayList<ImageView>
    private lateinit var labelRow: LinearLayout
    private lateinit var labelName: TextView
    private lateinit var imageRating: ImageView
    private lateinit var labelDesc: TextView
    private lateinit var detailRow: LinearLayout
    private lateinit var detailReviewCount: TextView
    private lateinit var reviewContainer: LinearLayout
    private lateinit var reviewRows: ArrayList<LinearLayout>

    private lateinit var btnNavigate: ImageView
    private lateinit var btnYelp: ImageView

    private var collapsed = true

    fun load() {
        // create the underlying container
        container = TableLayout(context)
        container.id = View.generateViewId()
        container.setPadding(10, 10, 10, 40)
        container.setOnClickListener { showHide() }
        context.picTableLayout.addView(container, 0)

        // create the labels
        labelRow = newLabelRow()
        labelName = newLabelName()
        imageRating = newTotalRating()

        // create the collapsing details
        detailRow = newDetailRow()
        labelDesc = newLabelDesc()
        detailReviewCount = newDetailReviewCount()

        // create the images
        imageRow = newImageRow()
        image = newImages()

        // create the review sub-rows
        reviewContainer = newReviewContainer()
        reviewRows = newReviewRows()

        fadeIn()
    }

    fun unload() {
        context.picTableLayout.removeView(container)
    }

    /*
     * event handlers
     */

    private fun showHide() {
        if (collapsed) {
            labelDesc.visibility = View.VISIBLE
            labelDesc.alpha = 0.0f
            labelDesc.animate()
                .alpha(1.0f)
                .setListener(null)
            detailRow.visibility = View.VISIBLE
            detailRow.alpha = 0.0f
            detailRow.animate()
                .alpha(1.0f)
                .setListener(null)
            reviewContainer.visibility = View.VISIBLE
            reviewContainer.alpha = 0.0f
            reviewContainer.animate()
                .alpha(1.0f)
                .setListener(null)
        } else {
            labelDesc.animate()
                .alpha(0.0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        labelDesc.visibility = View.GONE
                    }
                })
            detailRow.animate()
                .alpha(0.0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        detailRow.visibility = View.GONE
                    }
                })
            reviewContainer.animate()
                .alpha(0.0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        reviewContainer.visibility = View.GONE
                    }
                })
        }

        collapsed = !collapsed
    }

    private fun fadeIn() {
        container.alpha = 0.0f
        container.animate()
            .alpha(1.0f)
            .setListener(null)
    }

    /*
     * GUI items that add themselves to the main layout
     */

    private fun newLabelRow(): LinearLayout {
        val newRow = LinearLayout(context)
        newRow.id = View.generateViewId()
        newRow.orientation = LinearLayout.HORIZONTAL
        newRow.setPadding(15, 0, 15, 0)

        val params = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.MATCH_PARENT
        )

        container.addView(newRow, params)
        return newRow
    }

    private fun newLabelName(): TextView {
        val newView = TextView(context)
        newView.id = View.generateViewId()
        newView.text = name
        newView.textSize = 20f
        newView.setTextColor(Color.DKGRAY)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        params.setMargins(0, 0, 0, 0)
        params.weight = 1f
        params.gravity = Gravity.LEFT

        labelRow.addView(newView, params)
        return newView
    }

    private fun newTotalRating(): ImageView {
        val view = ImageView(context).apply {
            when (rating) {
                "0.0", "0.5" -> setImageResource(R.drawable.stars_small_0)
                "1.0" -> setImageResource(R.drawable.stars_small_1)
                "1.5" -> setImageResource(R.drawable.stars_small_1_half)
                "2.0" -> setImageResource(R.drawable.stars_small_2)
                "2.5" -> setImageResource(R.drawable.stars_small_2_half)
                "3.0" -> setImageResource(R.drawable.stars_small_3)
                "3.5" -> setImageResource(R.drawable.stars_small_3_half)
                "4.0" -> setImageResource(R.drawable.stars_small_4)
                "4.5" -> setImageResource(R.drawable.stars_small_4_half)
                "5.0" -> setImageResource(R.drawable.stars_small_5)
            }
            setPadding(0, 10, 0, 0)
            id = View.generateViewId()
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        params.setMargins(0, 0, 0, 0)
        params.gravity = Gravity.RIGHT

        labelRow.addView(view, params)

        return view
    }

    private fun newDetailRow(): LinearLayout {
        val newRow = LinearLayout(context)
        newRow.id = View.generateViewId()
        newRow.orientation = LinearLayout.HORIZONTAL
        newRow.visibility = View.GONE
        newRow.setPadding(15, 0, 15, 0)

        val params = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.MATCH_PARENT
        )

        container.addView(newRow, params)
        return newRow
    }

    private fun newLabelDesc(): TextView {
        val newView = TextView(context)
        newView.id = View.generateViewId()
        newView.text = categories
        newView.textSize = 16f
        newView.setTextColor(Color.GRAY)
        newView.visibility = View.GONE

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        params.setMargins(0, 0, 0, 0)
        params.weight = 1f
        params.gravity = Gravity.LEFT

        detailRow.addView(newView, params)
        return newView
    }


    private fun newDetailReviewCount(): TextView {
        val newView = TextView(context)
        newView.id = View.generateViewId()
        newView.text = "(${reviewCount} reviews)"
        newView.textSize = 16f
        newView.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
        newView.setTextColor(Color.LTGRAY)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        params.setMargins(0, 0, 0, 0)
        params.gravity = Gravity.RIGHT

        detailRow.addView(newView, params)
        return newView
    }

    private fun newReviewContainer(): LinearLayout {
        val newRow = LinearLayout(context)
        newRow.id = View.generateViewId()
        newRow.orientation = LinearLayout.VERTICAL
        newRow.visibility = View.GONE
        newRow.setPadding(15, 0, 15, 0)

        val params = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.MATCH_PARENT
        )

        container.addView(newRow, params)
        return newRow
    }

    private fun newReviewRows(): ArrayList<LinearLayout> {
        val views = arrayListOf<LinearLayout>()

        // create the external app links
        val linkRow = LinearLayout(context).apply {
            id = View.generateViewId()
            gravity = Gravity.RIGHT
            orientation = LinearLayout.HORIZONTAL

            addView(
                TextView(context).apply {
                    text = "Yelp:"
                    textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                    setPadding(170, 10, 0, 10)
                }, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 0f
                    gravity = Gravity.RIGHT

                })
            addView(newButtonYelp(), LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                weight = 1f
            })
            addView(
                TextView(context).apply {
                    text = "Navigate:"
                    textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                    setPadding(20, 10, 0, 10)
                }, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 0f
                    gravity = Gravity.RIGHT
                })
            addView(newButtonNavigate(), LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                weight = 1f
            })
        }

        reviewContainer.addView(linkRow, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(200, 0, 0, 0)
        })

        reviewData.forEach {
            val newRow = LinearLayout(context).apply {
                id = View.generateViewId()
                orientation = LinearLayout.VERTICAL
            }

            // fill the row with elements of that review
            newRow.addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 10, 0, 0)
                addView(newReviewName(it.name), LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { this.weight = 1f })
                addView(
                    newReviewRating(it.rating), LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                )
            })
            newRow.addView(LinearLayout(context).apply {
                layoutMode = LinearLayout.LayoutParams.MATCH_PARENT
                orientation = LinearLayout.HORIZONTAL
                addView(newReviewImg(it.picURL))
                addView(newReviewText(it.text))
            })

            reviewContainer.addView(newRow)
            views.add(newRow)
        }

        return views
    } // handles the layout of child review items

    private fun newReviewName(name: String): TextView {
        val view = TextView(context)
        view.id = View.generateViewId()
        view.text = name
        view.textSize = 16f
        view.setTextColor(Color.LTGRAY)

        return view
    }

    private fun newReviewRating(rating: String): ImageView {
        val view = ImageView(context).apply {
            when (rating) {
                "0.0", "0.5" -> setImageResource(R.drawable.stars_small_0)
                "1.0" -> setImageResource(R.drawable.stars_small_1)
                "1.5" -> setImageResource(R.drawable.stars_small_1_half)
                "2.0" -> setImageResource(R.drawable.stars_small_2)
                "2.5" -> setImageResource(R.drawable.stars_small_2_half)
                "3.0" -> setImageResource(R.drawable.stars_small_3)
                "3.5" -> setImageResource(R.drawable.stars_small_3_half)
                "4.0" -> setImageResource(R.drawable.stars_small_4)
                "4.5" -> setImageResource(R.drawable.stars_small_4_half)
                "5.0" -> setImageResource(R.drawable.stars_small_5)
            }
            setPadding(0, 5, 0, 0)
            id = View.generateViewId()
        }

        return view
    }

    private fun newReviewImg(url: String): ImageView {
        val view = ImageView(context)
        view.id = View.generateViewId()
        view.setPadding(0, 10, 10, 10)
        view.minimumWidth =
            (context.picTableLayout.width - context.picTableLayout.marginLeft - context.picTableLayout.marginRight) / 6 - 30
        view.minimumHeight = view.minimumWidth
        view.maxWidth = view.minimumWidth
        view.maxHeight = view.minimumWidth

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        Glide.with(context)
            .load(url)
            .apply(RequestOptions().centerCrop())
            .transition(withCrossFade())
            .into(view)

        return view
    }

    private fun newReviewText(text: String): TextView {
        val view = TextView(context)
        view.id = View.generateViewId()
        view.setPadding(10, 0, 0, 0)
        view.text = text
        view.textSize = 16f
        view.setTextColor(Color.GRAY)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        params.setMargins(0, 0, 0, 0)
        params.gravity = Gravity.LEFT

        return view
    }

    private fun newButtonNavigate(): ImageView {
        val view = ImageView(context).apply {
            setImageResource(R.drawable.iconnav)
            setPadding(0, 0, 0, 0)
            id = View.generateViewId()
            setOnClickListener {
                launchMaps()
            }
        }

        return view
    }

    private fun newButtonYelp(): ImageView {
        val view = ImageView(context).apply {
            setImageResource(R.drawable.iconyelp)
            setPadding(0, 0, 0, 0)
            id = View.generateViewId()
            setOnClickListener {
                launchYelp()
            }
        }

        return view
    }

    private fun newImageRow(): LinearLayout {
        val newRow = LinearLayout(context)
        newRow.id = View.generateViewId()

        val params = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.MATCH_PARENT
        )
        params.setMargins(0, 0, 0, 0)

        container.addView(newRow, params)
        return newRow
    }

    private fun newImages(): ArrayList<ImageView> {
        val views = arrayListOf<ImageView>()

        imgURLs.forEach {
            val view = ImageView(context)
            view.id = View.generateViewId()
            view.minimumWidth =
                (context.picTableLayout.width - context.picTableLayout.marginLeft - context.picTableLayout.marginRight) / 3 - 30
            view.minimumHeight = view.minimumWidth
            view.maxWidth = view.minimumWidth
            view.maxHeight = view.minimumWidth

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            params.setMargins(15, 15, 15, 15)

            imageRow.addView(view, params)

            Glide.with(context)
                .load(it)
                .apply(RequestOptions().centerCrop())
                .transition(withCrossFade())
                .into(view)

            views.add(view)
        }

        return views
    }

    private fun launchMaps() {
        val intent = Intent(
            Intent.ACTION_VIEW,
//            Uri.parse(
//                "google.navigation:q=" + URLEncoder.encode(
//                    "${name} $address",
//                    "utf-8"
//                )
//            )
            Uri.parse("google.navigation:q=${name} $address")
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(context.getPackageManager()) != null) context.startActivity(
            intent
        )
        else Toast.makeText(context, "Google maps not found on device.", Toast.LENGTH_LONG)
    }

    private fun launchYelp() {
        val uri = businessURL
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(intent)
    }

    data class ReviewData(
        val name: String,
        val picURL: String,
        val text: String,
        val rating: String
    )
}
