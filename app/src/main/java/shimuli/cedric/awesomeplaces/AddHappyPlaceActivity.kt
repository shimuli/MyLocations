package shimuli.cedric.awesomeplaces

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import shimuli.cedric.awesomeplaces.databinding.ActivityAddHappyPlaceBinding
import shimuli.cedric.awesomeplaces.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAddHappyPlaceBinding

    // calender
    private var calendar = Calendar.getInstance()
    private lateinit var datesetListener:DatePickerDialog.OnDateSetListener
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // toolbar
        setSupportActionBar(binding.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAddPlace.setNavigationOnClickListener{
            onBackPressed()
        }

        // get date picker
        datesetListener = DatePickerDialog.OnDateSetListener {
                datePicker, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        binding.etDate.setOnClickListener(this)

    }

    // all Onclick listeners
    override fun onClick(view: View?) {
       when(view!!.id){
           R.id.et_date->{
               DatePickerDialog(this@AddHappyPlaceActivity, datesetListener,
                   calendar.get(Calendar.YEAR),
                   calendar.get(Calendar.MONTH),
                   calendar.get(Calendar.DAY_OF_MONTH)).show()
           }
       }
    }
    // update date in view
    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etDate.setText(sdf.format(calendar.time).toString())
    }
}