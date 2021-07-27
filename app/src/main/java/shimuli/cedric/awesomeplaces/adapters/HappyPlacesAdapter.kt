package shimuli.cedric.awesomeplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import shimuli.cedric.awesomeplaces.R
import shimuli.cedric.awesomeplaces.activities.AddHappyPlaceActivity
import shimuli.cedric.awesomeplaces.activities.MainActivity
import shimuli.cedric.awesomeplaces.databinding.HappyPlacesListBinding
import shimuli.cedric.awesomeplaces.models.HappyPlaceModel

class HappyPlacesAdapter(
    private val context: Context,
    private var list: ArrayList<HappyPlaceModel>
) :RecyclerView.Adapter<HappyPlacesAdapter.ViewHolder>(){
    private var onClickListener: IonClickListener? = null

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
            holder.itemView.setOnClickListener{
                if (onClickListener != null){
                    onClickListener!!.onClick(position, model)
                }
            }

    }
    // swipe to edit
    fun notifyEditItem(activity:Activity, position: Int, requestCode:Int){
        val intent = Intent(context, AddHappyPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    interface IonClickListener{
        fun onClick(position: Int, model: HappyPlaceModel)
    }

    fun setOnClickListener(onClickListener:IonClickListener){
        this.onClickListener = onClickListener
    }

    override fun getItemCount(): Int {
        return  list.size
    }

}