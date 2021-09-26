package be.ugent.mydigipill.recyclerAdapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import be.ugent.mydigipill.R
import be.ugent.mydigipill.data.Medication
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

/**
 * This is an implementation of an
 * @see AbstractViewHolder
 * This class holds all the TextViews of a MedicationCardView
 * @see R.layout.overview_cardview_right or
 * @see R.layout.overview_cardview_left
 * @author Arthur Deruytter
 */
class MedicationViewHolder constructor(itemView: View) : AbstractViewHolder<Medication>(itemView) {

    private var include: ConstraintLayout = itemView.findViewById(R.id.cardview_content)
    private var description: TextView = include.findViewById(R.id.overview_description)
    private var title: TextView = include.findViewById(R.id.overview_title)
    private var ingestion: TextView = include.findViewById(R.id.overview_next_ingestion_text)
    private var timeNextIngestion: TextView = include.findViewById(R.id.overview_next_ingestion)
    private var imageView: ImageView = itemView.findViewById(R.id.overview_image)

    override fun bindTo(t: Medication) {
        description.text = t.note
        title.text = t.name
        ingestion.text = t.intake
        timeNextIngestion.text = t.timeToNext

        t.image?.let {
            Glide.with(itemView.context)
                .load(t.image)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_notification_dark)
                        .error(R.drawable.ic_notification_dark)
                )
                .into(imageView)
        } ?: imageView.setImageResource(R.drawable.ic_notification_dark)
    }
}