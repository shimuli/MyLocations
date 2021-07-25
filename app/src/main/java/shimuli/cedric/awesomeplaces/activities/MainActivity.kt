package shimuli.cedric.awesomeplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import shimuli.cedric.awesomeplaces.adapters.HappyPlacesAdapter
import shimuli.cedric.awesomeplaces.database.DatabaseHandle
import shimuli.cedric.awesomeplaces.databinding.ActivityMainBinding
import shimuli.cedric.awesomeplaces.models.HappyPlaceModel
import java.text.FieldPosition

class MainActivity : AppCompatActivity() {
     private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getHappyPlaces()
        binding.fabAddPlace.setOnClickListener{
            val addPlaceIntent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivityForResult(addPlaceIntent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }
    }

    private fun setHappyPlaces(happyPlacesList:ArrayList<HappyPlaceModel>){
        binding.rvHappyPlaces.layoutManager = LinearLayoutManager(this)
        val placesAdapter = HappyPlacesAdapter(this, happyPlacesList)
        binding.rvHappyPlaces.setHasFixedSize(true)
        binding.rvHappyPlaces.adapter = placesAdapter

        placesAdapter.setOnClickListener(object : HappyPlacesAdapter.IonClickListener{
            override fun onClick(position:Int, model: HappyPlaceModel){
                val intent = Intent(this@MainActivity, HappyPlaceDetails::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })
    }

    // get all place
    private  fun getHappyPlaces(){
        val dbHandler = DatabaseHandle(this)
        val getHappyPlaceList: ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()

        if(getHappyPlaceList.size >0){
            binding.rvHappyPlaces.visibility = View.VISIBLE
            binding.noRecordsTv.visibility = View.GONE
            setHappyPlaces(getHappyPlaceList)

            for(i in getHappyPlaceList){
                i.title?.let { Log.e("Title", it) }
                i.description?.let { Log.e("Description", it) }
            }
        }
        else{
            binding.rvHappyPlaces.visibility = View.GONE
            binding.noRecordsTv.visibility = View.VISIBLE
        }
    }

    // refresh view
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                getHappyPlaces()
            }
            else{
                Log.e("Activity", "Canceled or back pressed")
            }
        }
    }
    companion object{
        var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        var EXTRA_PLACE_DETAILS ="extra_places_details"
    }
}