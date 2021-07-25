package shimuli.cedric.awesomeplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import shimuli.cedric.awesomeplaces.databinding.ActivityAddHappyPlaceBinding
import shimuli.cedric.awesomeplaces.databinding.ActivityHappyPlaceDetailsBinding

class HappyPlaceDetails : AppCompatActivity() {
    private lateinit var binding: ActivityHappyPlaceDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}