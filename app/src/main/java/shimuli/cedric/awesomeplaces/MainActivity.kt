package shimuli.cedric.awesomeplaces

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import shimuli.cedric.awesomeplaces.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
     private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabAddPlace.setOnClickListener{
            val addPlaceIntent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivity(addPlaceIntent)
        }
    }
}