package pl.pb.optigai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class AlbumViewAdapter(private val context: Context, private val albums: List<Album>) :
    RecyclerView.Adapter<AlbumViewAdapter.ViewHolder>() {

    private val TAG = "AlbumViewAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.i(TAG, "onCreateViewHolder")
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.photo, parent, false))
    }

    override fun getItemCount() = albums.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.i(TAG, "onBindViewHolder at position $position")
        val album = albums[position]
        holder.bind(album)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val photoImageView: ImageView = itemView.findViewById(R.id.photo) // Get ImageView

        fun bind(album: Album) {
            // Load image using AsyncTask
            LoadImageTask(photoImageView).execute(album.imageUrl)
        }
    }

    // AsyncTask to load images in the background
    private class LoadImageTask(private val imageView: ImageView) : AsyncTask<String, Void, Bitmap>() {

        override fun doInBackground(vararg params: String): Bitmap? {
            val imageUrl = params[0]
            var bitmap: Bitmap? = null
            var connection: HttpURLConnection? = null
            try {
                val url = URL(imageUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream = connection.inputStream
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: IOException) {
                Log.e("LoadImageTask", "Error loading image: ${e.message}")
            } finally {
                connection?.disconnect()
            }
            return bitmap
        }

        override fun onPostExecute(result: Bitmap?) {
            if (result != null) {
                imageView.setImageBitmap(result)
            } else {
               println("Nothing")
            }
        }
    }
}
