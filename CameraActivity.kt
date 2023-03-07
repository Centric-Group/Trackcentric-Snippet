package com.appcentric.trackcentricmobilev2.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appcentric.trackcentricmobilev2.R
import com.appcentric.trackcentricmobilev2.adapter.PhotoAdapter
import com.appcentric.trackcentricmobilev2.databinding.ActivityCameraScreenBinding
import com.appcentric.trackcentricmobilev2.dbmodels.DeliveryDocumentEntity
import com.appcentric.trackcentricmobilev2.dbmodels.TrackCentricDB
import com.appcentric.trackcentricmobilev2.dbmodels.UploadDocumentEntity
import com.appcentric.trackcentricmobilev2.utils.Common
import com.appcentric.trackcentricmobilev2.utils.SharePref
import com.appcentric.trackcentricmobilev2.utils.SwipeToDeleteCallback
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.util.*

/*
* This is Camera Activity Screen. The user can select whether to upload a picture from gallery or capture a photo using the camera
* */
class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraScreenBinding // This is the binding class which auto generate class according to view ids
    private lateinit var sharePref: SharePref // SharedPreferences class object to get data from memory
    private lateinit var dbObject: TrackCentricDB // Local Database object which gets data from local database
    private var deliveryId:String?=null
    private var shipmentId:String?=null
    private var photoList= ArrayList<UploadDocumentEntity>()
    private var adapter: PhotoAdapter?=null // This adapter class is used to bind list of data
    private lateinit var deliveryObject: DeliveryDocumentEntity //This is particular item object of delivery document
    val REQUEST_ID_MULTIPLE_PERMISSIONS = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This is binding of particular screen layout
        binding = ActivityCameraScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initilization()
    }


    /*
   *  This method is used to create objects when screen is initialized
   * */
    private fun initilization() {

        dbObject = TrackCentricDB.getDatabase(applicationContext)
        setSupportActionBar(binding.uploadDocument.toolbar)
        binding.uploadDocument.titletoolbar.text = resources.getString(R.string.txt_upload_document)
        val action = supportActionBar
        action?.title = ""
        action?.setDisplayHomeAsUpEnabled(true)
        binding.uploadDocument.toolbar.setNavigationOnClickListener {
            finish()
        }

        sharePref = SharePref(applicationContext)
        deliveryId=intent.extras?.getString("deliveryId")
        shipmentId=intent.extras?.getString("shipmentId")


        deliveryObject=  dbObject.deliveryDao().getParticularDelivery(deliveryId!!)

        //Set the adapter in the list so all photos are displayed in the list
        adapter= PhotoAdapter(applicationContext,photoList)
        binding.recycleViewPhoto.setLayoutManager( LinearLayoutManager(this))
        binding.recycleViewPhoto.setAdapter(adapter)
        enableSwipeToDeleteAndUndo()


        binding.btnSignature.setOnClickListener {
            val intent = Intent(this, SignatureScreen::class.java) // this the redirection from Signature screen
            intent.putExtra("deliveryId",deliveryId) // this passes data from camera screen to signature
            intent.putExtra("shipmentId",shipmentId)
            startActivity(intent)

        }
        binding.takePhotoButton.setOnClickListener {

            //Checking permission of local storage
            if(checkAndRequestPermissions(this@CameraActivity)){
                chooseImage(this@CameraActivity)
            }
        }

        // Get list of data from Local storage database
        getListOfPhoto(deliveryObject.deliveryId)
    }


    /*
    *  this function is used to handle swipe-to-delete functionality
    * */
    private fun enableSwipeToDeleteAndUndo() {
        val swipeToDeleteCallback: SwipeToDeleteCallback = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                val position = viewHolder.adapterPosition
                val item: UploadDocumentEntity = adapter?.dataSet!!.get(position)
                adapter?.removeItem(position)
                val snackbar = Snackbar
                    .make(
                        binding.mainlayout,
                        "Item was removed from the list.",
                        Snackbar.LENGTH_LONG
                    )
                dbObject.uploadDocument().deleteDocument(item)
                snackbar.setAction("UNDO") {
                    adapter?.restoreItem(item, position)
                    binding.recycleViewPhoto.scrollToPosition(position)
                }
                snackbar.setActionTextColor(Color.YELLOW)
                snackbar.show()
            }
        }
        val itemTouchhelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchhelper.attachToRecyclerView(binding.recycleViewPhoto)
    }


    /*
    *  This function is used to store image file in the local storage
    * */
    fun saveFile(bitmap: Bitmap, fileName: String, path: String) {

        val dest = File(path, fileName)

        try {
            val out = FileOutputStream(dest)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    /*
    * This function is used to take photo by camera or choose from gallery
    * */
    private fun chooseImage(context: Context) {
        val optionsMenu = arrayOf<CharSequence>(
            "Take Photo",
            "Choose from Gallery",
            "Exit"
        ) // create a menuOption Array
        // create a dialog for showing the optionsMenu
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        // set the items in builder
        builder.setItems(optionsMenu,
            DialogInterface.OnClickListener { dialogInterface, i ->
                if (optionsMenu[i] == "Take Photo") {
                    // Open the camera and get the photo

                    val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(takePicture, 0)

                } else if (optionsMenu[i] == "Choose from Gallery") {
                    // choose from external storage
                    val pickPhoto =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(pickPhoto, 1)
                } else if (optionsMenu[i] == "Exit") {
                    dialogInterface.dismiss()
                }
            })
        builder.show()
    }


    /*
    *  This function will request permission from user to use external storage
    * */
    fun checkAndRequestPermissions(context: Activity?): Boolean {
        val WExtstorePermission = ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val cameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        )

        val listPermissionsNeeded: MutableList<String> = ArrayList()

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (WExtstorePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded
                .add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                context, listPermissionsNeeded
                    .toTypedArray(),
                REQUEST_ID_MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }

    /*
    *  This function is used to get data from table
    * */
    fun getListOfPhoto(deliveryId:String){
        photoList.clear()
        var data=dbObject.uploadDocument().getDocuments(deliveryId)

        if(data.isNullOrEmpty()){
            binding.Nodatafound.visibility=View.VISIBLE
            binding.recycleViewPhoto.visibility=View.GONE
        }else{
            binding.recycleViewPhoto.visibility=View.VISIBLE
            binding.Nodatafound.visibility=View.GONE
            photoList.addAll(data)
            adapter?.notifyDataSetChanged()
        }


    }

    /*
    *  This function is used to store data in the database
    * */
    fun addPhotoInDatabase(fileName:String,fileUrl:String){
        var data=dbObject.uploadDocument().insertDocument(UploadDocumentEntity(0,deliveryObject.deliveryId,fileName,fileUrl,Common.getCurrentDate(
            Date()
        )))
    }


    /*
    *  This call back function is used when user gives permission to access
    * */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> if (ContextCompat.checkSelfPermission(
                    this@CameraActivity,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    applicationContext,
                    "FlagUp Requires Access to Camara.", Toast.LENGTH_SHORT
                )
                    .show()
            } else if (ContextCompat.checkSelfPermission(
                    this@CameraActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    applicationContext,
                    "FlagUp Requires Access to Your Storage.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                chooseImage(this@CameraActivity)
            }
        }
    }


    /*
    *  This call back function is used when user choose photo from camera or gallery
    * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == RESULT_OK && data != null) {

                    val selectedImage = data.extras!!["data"] as Bitmap?
                    val filename = deliveryObject.deliveryId + "_" + System.currentTimeMillis() + ".jpg"
                    val sd :String? =Common.getRootDirPath(this@CameraActivity)
                    addPhotoInDatabase(filename,File(sd,filename).absolutePath)
                    getListOfPhoto(deliveryObject.deliveryId)
                    adapter?.notifyDataSetChanged()
                    saveFile(selectedImage!!, filename, sd!!)
                }
                1 -> if (resultCode == RESULT_OK && data != null) {

                    val selectedImage: Uri? = data.data
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    if (selectedImage != null) {
                        val cursor: Cursor? =
                            contentResolver.query(selectedImage, filePathColumn, null, null, null)
                        if (cursor != null) {
                            cursor.moveToFirst()
                            val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
                            val picturePath: String = cursor.getString(columnIndex)

                            var bitmap=BitmapFactory.decodeFile(picturePath)
                            val filename = deliveryObject.deliveryId + "_" + System.currentTimeMillis() + ".jpg"
                            val sd :String? =Common.getRootDirPath(this@CameraActivity)
                            addPhotoInDatabase(filename,File(sd,filename).absolutePath)
                            saveFile(bitmap!!, filename, sd!!)
                            getListOfPhoto(deliveryObject.deliveryId)
                            adapter?.notifyDataSetChanged()
                            cursor.close()
                        }
                    }
                }
            }
        }
    }

}
