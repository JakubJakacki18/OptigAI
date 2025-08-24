package pl.pb.optigai.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import pl.pb.optigai.R
import pl.pb.optigai.utils.data.Image

class ImageAdapter(
    private val images: List<Image>,
    private val onImageClick: (position: Int) -> Unit,
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    class ImageViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

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

    override fun onBindViewHolder(
        holder: ImageViewHolder,
        position: Int,
    ) {
        val image = images[position]
        holder.imageView.setImageURI(image.uri)

        holder.itemView.setOnClickListener {
            onImageClick(position)
        }
    }

    override fun getItemCount() = images.size
}
