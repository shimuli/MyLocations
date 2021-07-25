package shimuli.cedric.awesomeplaces.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import shimuli.cedric.awesomeplaces.R
import shimuli.cedric.awesomeplaces.databinding.HappyPlacesListBinding
import shimuli.cedric.awesomeplaces.models.HappyPlaceModel

class HappyPlacesAdapter(
    private val context: Context,
    private var list: ArrayList<HappyPlaceModel>
) :RecyclerView.Adapter<HappyPlacesAdapter.ViewHolder>(){

    inner class ViewHolder( val binding: HappyPlacesListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HappyPlacesListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val model = list[position]
            holder.binding.ivPlaceImage.setImageURI(Uri.parse(model.image))
            holder.binding.tvTitle.text = model.title
            holder.binding.tvDescription.text = model.description

    }

    override fun getItemCount(): Int {
        return  list.size
    }

}