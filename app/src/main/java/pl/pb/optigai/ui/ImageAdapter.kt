package pl.pb.optigai.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pl.pb.optigai.R
import pl.pb.optigai.utils.PhotoUtils
import pl.pb.optigai.utils.data.Image

class ImageAdapter(
    private val context: android.content.Context,
    private val images: List<Image>,
    private val onImageClick: (position: Int) -> Unit,
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    class ImageViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    /**
     * Called when the RecyclerView needs a new ViewHolder to represent an item.
     * This function inflates the item layout and returns a new ImageViewHolder.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ImageViewHolder {
        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_photo, parent, false)
        return ImageViewHolder(view)
    }

    /**
     * Called by the RecyclerView to display the data at the specified position.
     * This function loads the image using Glide and sets a click listener on the item.
     */
    override fun onBindViewHolder(
        holder: ImageViewHolder,
        position: Int,
    ) {
        val image = images[position]
        Glide
            .with(holder.itemView.context)
            .load(image.uri)
            .sizeMultiplier(0.5f)
            .centerCrop()
            .into(holder.imageView)
        holder.itemView.setOnClickListener {
            onImageClick(position)
        }
        val dateAndTime = PhotoUtils.extractDateAndTime(image.dateAddedTimeStamp)
        holder.itemView.contentDescription =
            context.getString(
                R.string.gallery_image_description,
                dateAndTime.first,
                dateAndTime.second,
                image.fileName,
            )
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     */
    override fun getItemCount() = images.size
}
