package shimuli.cedric.awesomeplaces.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import shimuli.cedric.awesomeplaces.R
import shimuli.cedric.awesomeplaces.database.DatabaseHandle
import shimuli.cedric.awesomeplaces.databinding.ActivityAddHappyPlaceBinding
import shimuli.cedric.awesomeplaces.models.HappyPlaceModel
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
                _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        updateDateInView()
        binding.etDate.setOnClickListener(this)
        binding.tvAddImage.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)

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
                           0,
                           binding.etTitle.text.toString(),
                           savedImage.toString(),
                           binding.etDescription.text.toString(),
                           binding.etDate.text.toString(),
                           binding.etLocation.text.toString(),
                           mLatitude,
                           mLongitude
                       )
                   val dbHandler = DatabaseHandle(this)
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
        }
    }
    private fun showPermissionDialog() {
        AlertDialog.Builder(this).setMessage("You need to allow the app to access gallery first, enable in application settings")
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
    }
}