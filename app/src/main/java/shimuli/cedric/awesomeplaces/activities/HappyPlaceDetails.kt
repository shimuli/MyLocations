package shimuli.cedric.awesomeplaces.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import shimuli.cedric.awesomeplaces.databinding.ActivityAddHappyPlaceBinding
import shimuli.cedric.awesomeplaces.databinding.ActivityHappyPlaceDetailsBinding
import shimuli.cedric.awesomeplaces.models.HappyPlaceModel

class HappyPlaceDetails : AppCompatActivity() {
    private lateinit var binding: ActivityHappyPlaceDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // toolbar


        var happyPlaceDetailsModel:HappyPlaceModel? = null
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            happyPlaceDetailsModel =
                intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel?
        }
        if(happyPlaceDetailsModel !=null){
            setSupportActionBar(binding.toolbarHappyPlaceDetails)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = happyPlaceDetailsModel.title
            binding.toolbarHappyPlaceDetails.setNavigationOnClickListener{
                onBackPressed()
            }
            binding.ivPlaceImage.setImageURI(Uri.parse(happyPlaceDetailsModel.image))
            binding.tvDescription.text = happyPlaceDetailsModel.description
            binding.tvLocation.text = happyPlaceDetailsModel.location

            binding.btnViewOnMap.setOnClickListener{
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, happyPlaceDetailsModel)
                startActivity(intent)
            }
        }
    }
}