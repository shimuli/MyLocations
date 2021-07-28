package shimuli.cedric.awesomeplaces.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import shimuli.cedric.awesomeplaces.R
import shimuli.cedric.awesomeplaces.database.DatabaseHandle
import shimuli.cedric.awesomeplaces.databinding.ActivityAddHappyPlaceBinding
import shimuli.cedric.awesomeplaces.models.HappyPlaceModel
import shimuli.cedric.awesomeplaces.utils.GetAddressFromLatLng
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAddHappyPlaceBinding

    // calender
    private var calendar = Calendar.getInstance()
    private lateinit var datesetListener:DatePickerDialog.OnDateSetListener
    private var savedImage: Uri? = null
    private  var mLatitude: Double = 0.0
    private  var mLongitude: Double = 0.0

    private  var mHappyPlaceDetails: HappyPlaceModel? =null
    private lateinit var  mFusedLocationClient : FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // for location init
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // toolbar
        setSupportActionBar(binding.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAddPlace.setNavigationOnClickListener{
            onBackPressed()
        }

        // maps init
        if(!Places.isInitialized()){
            Places.initialize(this@AddHappyPlaceActivity,
            resources.getString(R.string.google_maps_key))
        }

        // for swap
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel?
        }

        // get date picker
        datesetListener = DatePickerDialog.OnDateSetListener {
                _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        updateDateInView()

        if(mHappyPlaceDetails != null){
            supportActionBar?.title = "Edit Happy Place"

            binding.etTitle.setText(mHappyPlaceDetails!!.title)
            binding.etDescription.setText(mHappyPlaceDetails!!.description)
            binding.etLocation.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude

            savedImage = Uri.parse(mHappyPlaceDetails!!.image)
            binding.ivPlaceImage.setImageURI(savedImage)
            binding.btnSave.text = "Update"
        }

        binding.etDate.setOnClickListener(this)
        binding.tvAddImage.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        binding.etLocation.setOnClickListener(this)
        binding.tvSelectCurrentLocation.setOnClickListener(this)

    }

    // all Onclick listeners
    override fun onClick(view: View?) {
       when(view!!.id){
           R.id.et_date ->{
               DatePickerDialog(this@AddHappyPlaceActivity, datesetListener,
                   calendar.get(Calendar.YEAR),
                   calendar.get(Calendar.MONTH),
                   calendar.get(Calendar.DAY_OF_MONTH)).show()
           }
           R.id.et_location->{
               try {
                   val fields = listOf(
                       Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                       Place.Field.ADDRESS
                   )
                   // Start the autocomplete intent with a unique request code.
                   val intent =
                       Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                           .build(this@AddHappyPlaceActivity)
                   startActivityForResult(intent, PLACE_AUTO_COMPLETE_REQUEST_CODE)
               } catch(e:Exception){
                   e.printStackTrace()
               }
           }
           R.id.tv_select_current_location->{

               if (!isLocationEnabled()) {
                   Toast.makeText(
                       this,
                       "Your location provider is turned off. Please turn it on.",
                       Toast.LENGTH_SHORT
                   ).show()

                   // This will redirect you to settings from where you need to turn on the location provider.
                   val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                   startActivity(intent)
               } else {
                   // For Getting current location of user please have a look at below link for better understanding
                   // https://www.androdocs.com/kotlin/getting-current-location-latitude-longitude-in-android-using-kotlin.html
                   Dexter.withContext(this)
                       .withPermissions(
                           Manifest.permission.ACCESS_FINE_LOCATION,
                           Manifest.permission.ACCESS_COARSE_LOCATION
                       )
                       .withListener(object : MultiplePermissionsListener {
                           override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                               if (report!!.areAllPermissionsGranted()) {

                                   requestNewLocationData()
                               }
                           }

                           override fun onPermissionRationaleShouldBeShown(
                               permissions: MutableList<PermissionRequest>?,
                               token: PermissionToken?
                           ) {
                               showPermissionDialog()
                           }
                       }).onSameThread()
                       .check()
               }
           }
           R.id.tv_add_image ->{

               // create a dialog
               val imageDialog = AlertDialog.Builder(this)
               imageDialog.setTitle("Select Option")
               val dialogOptions = arrayOf("Select picture from gallery",
               "Capture photo from camera")

               imageDialog.setItems(dialogOptions){
                   _, which->
                   when(which){
                       // array index 0 = open gallery
                       0-> openGallery()
                       1-> openCamera()
                   }
               }
               imageDialog.show()
           }
           R.id.btn_save->{
               when{
                   binding.etTitle.text.isNullOrEmpty()->{
                       Toast.makeText(this, "Enter a title", Toast.LENGTH_LONG).show()
                   }
                   binding.etDescription.text.isNullOrEmpty()->{
                       Toast.makeText(this, "Enter a description", Toast.LENGTH_LONG).show()
                   }

                   binding.etLocation.text.isNullOrEmpty()->{
                       Toast.makeText(this, "Enter a Location", Toast.LENGTH_LONG).show()
                   }

                   savedImage == null ->{
                       Toast.makeText(this, "Enter a Location", Toast.LENGTH_LONG).show()
                   }else ->{
                       val happyPlaceModel = HappyPlaceModel(
                           if(mHappyPlaceDetails == null) 0 else mHappyPlaceDetails!!.id,
                           binding.etTitle.text.toString(),
                           savedImage.toString(),
                           binding.etDescription.text.toString(),
                           binding.etDate.text.toString(),
                           binding.etLocation.text.toString(),
                           mLatitude,
                           mLongitude
                       )
                   val dbHandler = DatabaseHandle(this)

                   if (mHappyPlaceDetails == null){
                       val addHappyPlace = dbHandler.saveHappyPlace(happyPlaceModel)
                       if(addHappyPlace >0){
                           setResult(Activity.RESULT_OK)
                           Toast.makeText(this, "The awesome place was saved successfully", Toast.LENGTH_LONG).show()
                           finish()
                   }
                       else{
                           Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                       }
                   }
                   else{
                       val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)
                       if(updateHappyPlace >0){
                           setResult(Activity.RESULT_OK)
                           Toast.makeText(this, "The awesome place was saved successfully", Toast.LENGTH_LONG).show()
                           finish()
                       }
                       else{
                           Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                       }
                   }

                   }

               }

           }
       }
    }

    private fun openCamera() {
        // ask for permissions
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ). withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?)

            // check permission
            {
                if (report!!.areAllPermissionsGranted()){
                    //if yes open gallery
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                // why we need permissions
                showPermissionDialog()
            }
        }).onSameThread().check()
    }

    // open galley function
    private fun openGallery() {
        // ask for permissions
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
           Manifest.permission.WRITE_EXTERNAL_STORAGE
        ). withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?)

            // check permission
            {
                if (report!!.areAllPermissionsGranted()){
                    //if yes open gallery
                  val galleryIntent = Intent(Intent.ACTION_PICK,
                  MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                // why we need permissions
                showPermissionDialog()
            }
        }).onSameThread().check()
    }

    // check location can be found
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    // get user location

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(){
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallBAck,
            Looper.myLooper()
        )
    }

    private val mLocationCallBAck = object:LocationCallback(){
        override fun onLocationResult(locationResults: LocationResult) {
            val mLastLocation:Location =locationResults.lastLocation
            mLatitude = mLastLocation.latitude
            Log.e("Location Lat", "$mLatitude" )
            mLongitude = mLastLocation.latitude
            Log.e("Location Long", "$mLongitude" )

            val addressTask = GetAddressFromLatLng(this@AddHappyPlaceActivity, mLatitude, mLongitude)
            addressTask.setAddressListener(object:GetAddressFromLatLng.AddressListener{
               override fun onAddressFound(address:String?){
                   Log.e("Address ::", "" + address)
                   binding.etLocation.setText(address)
               }

                override fun onError() {
                   Log.e("Get Error::", "Something went wrong")
                }

            })
            addressTask.getAddress()

        }

    }

    //results of start activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode== GALLERY){
                if(data != null){
                    val contentUrl = data.data
                    try{
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,contentUrl)
                         savedImage = saveImageToInternalStorage(selectedImageBitmap)
                       // Toast.makeText(this, "saved at = $savedImage", Toast.LENGTH_LONG).show()
                        Log.e("saved image : ", "Path:: $savedImage")
                        binding.ivPlaceImage.setImageBitmap(selectedImageBitmap)
                    }
                    catch(e: IOException){
                        e.printStackTrace()
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                    }
                }
            }
            else if(requestCode == CAMERA){
                val thumbNail: Bitmap = data!!.extras!!.get("data") as Bitmap
                savedImage = saveImageToInternalStorage(thumbNail)
                binding.ivPlaceImage.setImageBitmap(thumbNail)
            }
            else if(requestCode == PLACE_AUTO_COMPLETE_REQUEST_CODE){
                val place:Place = Autocomplete.getPlaceFromIntent(data!!)
                binding.etLocation.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }
        }
    }
    private fun showPermissionDialog() {
        AlertDialog.Builder(this).setMessage("You need to give permissions for this feature, enable in application settings")
            .setPositiveButton("Go To SETTINGS"){

                // open settings in phone
               _,_->
               try{
                   val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                   val uri = Uri.fromParts("package",packageName, null)
                   intent.data = uri
                   startActivity(intent)
               }catch(e:ActivityNotFoundException){
                   e.printStackTrace()
               }
            }
            .setNegativeButton("Cancel"){
                dialog, _->
                dialog.dismiss()
            }
            .show()
    }

    // update date in view
    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etDate.setText(sdf.format(calendar.time).toString())
    }

    // save images to local storage
    private fun saveImageToInternalStorage(bitmap:Bitmap):Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIR, Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }
        catch(e:IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    //permission codes
    companion object{
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIR = "AwesomePlacesImages"
        private  const val PLACE_AUTO_COMPLETE_REQUEST_CODE = 3
    }
}