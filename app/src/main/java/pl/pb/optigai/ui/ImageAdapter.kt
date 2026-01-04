/**
 * ImageAdapter
 *
 * RecyclerView adapter used to display a list of images in a gallery-style view.
 * Utilizes [Glide] to efficiently load images and supports click handling.
 *
 * @property context The context used for loading resources and creating views.
 * @property images List of [Image] objects to be displayed.
 * @property onImageClick Lambda function invoked when an image is clicked, receiving the clicked position.
 */
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
    /**
     * ViewHolder for the image item layout.
     * Holds a reference to the ImageView used to display the image.
     */
    class ImageViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }
    /**
     * Called when the RecyclerView needs a new ViewHolder to represent an item.
     * Inflates the item layout and returns a new [ImageViewHolder].
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new view (unused here, single type).
     * @return A new instance of [ImageViewHolder].
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
     * Loads the image into the holder using [Glide] and sets a click listener.
     * Also sets a content description for accessibility including date, time, and file name.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the data set.
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
     *
     * @return The size of the [images] list.
     */
    override fun getItemCount() = images.size
}
