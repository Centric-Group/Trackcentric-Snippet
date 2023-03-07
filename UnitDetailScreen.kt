package com.appcentric.trackcentricmobilev2.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appcentric.trackcentricmobilev2.R
import com.appcentric.trackcentricmobilev2.databinding.ActivityUnitDetailBinding
import com.appcentric.trackcentricmobilev2.dbmodels.DeliveryVarianceEntity
import com.appcentric.trackcentricmobilev2.dbmodels.TrackCentricDB
import com.appcentric.trackcentricmobilev2.dbmodels.VehicleReasonEntity
import com.appcentric.trackcentricmobilev2.utils.SharePref

/*
*  This screen is used if any delivery unit is rejected. The user can set how many quantity is rejected and add reason of rejection.
* */
class UnitDetailScreen : AppCompatActivity() {
    private lateinit var binding: ActivityUnitDetailBinding // This is the binding class which auto generate class according to view ids
    private lateinit var sharePref: SharePref // SharedPreferences class object to get data from memory
    private lateinit var dbObject: TrackCentricDB // Local Database object which gets data from local database
    private var reasonList = ArrayList<VehicleReasonEntity>()
    private var qty: Int = 0
    private var deliveryDocId: String? = null
    private var deliveryId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //This is for Binding a layout of shipment screen
        binding = ActivityUnitDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initilization()
        setListener()
    }

    /*
    * This function registers all the events
    * */
    private fun setListener() {
        binding.btnSaveUnite.setOnClickListener {

            if (binding.reasonRecycle.text.toString().trim().isEmpty()) {
                Toast.makeText(applicationContext, "Please Select Reason", Toast.LENGTH_LONG).show()
            } else if (binding.editUnite.text.toString().trim().isEmpty()) {
                Toast.makeText(applicationContext, "Please Enter Return Unit", Toast.LENGTH_LONG)
                    .show()
            } else if (binding.editUnite.text.toString().toDouble() > qty) {
                Toast.makeText(
                    applicationContext,
                    "You can't enter more quantity",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                var deliveryNumber = dbObject.deliveryDao().getParticularDelivery(deliveryId!!)

                var receiveQTY=(qty-binding.editUnite.text.toString().toInt())
                dbObject.deliveryVariance().insertDeliveryVariance(
                    DeliveryVarianceEntity(
                        deliveryDocId!!,
                        deliveryId!!,
                        deliveryNumber.deliveryNumber,
                        binding.reasonRecycle.text.toString(),
                        receiveQTY,
                        qty,
                        0
                    )
                )
                dbObject.deliverDoctItem()
                    .updateUnitItem(deliveryDocId!!, binding.reasonRecycle.text.toString(), 0)
                val returnIntent = Intent()
                setResult(RESULT_OK, returnIntent)
                finish()
            }

        }

    }

    /*
    * This function initializes all the objects when user run the activity
    * */
    private fun initilization() {
        dbObject = TrackCentricDB.getDatabase(applicationContext)
        setSupportActionBar(binding.uniteTool.toolbar)
        binding.uniteTool.titletoolbar.text = resources.getString(R.string.title_unite_detail)
        val action = supportActionBar
        action?.title = ""
        action?.setDisplayHomeAsUpEnabled(true)
        binding.uniteTool.toolbar.setNavigationOnClickListener {
            finish();
        }
        sharePref = SharePref(applicationContext)
        qty = intent.extras?.getInt("qty", 0)!!
        deliveryDocId = intent.extras?.getString("deliveryDocId")
        deliveryId = intent.extras?.getString("deliveryId")

        getListOfVehicleReason()

    }

    /*
    * This function gets the list of vehicle reason from database
    * */
    fun getListOfVehicleReason() {
        var data = dbObject.vehicleReasonDao().getVehicleReasonDB()
        reasonList.addAll(data)

        var itemList = ArrayList<String>()
        for (item in reasonList) {
            itemList.add(item.description)
        }

        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this, android.R.layout.simple_dropdown_item_1line, android.R.id.text1, itemList
        )
        binding.reasonRecycle.setAdapter(arrayAdapter) // Set List of data in the listview

    }
}
