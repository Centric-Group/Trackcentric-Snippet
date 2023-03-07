package com.appcentric.trackcentricmobilev2.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.appcentric.trackcentricmobilev2.Listioner.NoDataFound
import com.appcentric.trackcentricmobilev2.Listioner.onClickNextScreen
import com.appcentric.trackcentricmobilev2.R
import com.appcentric.trackcentricmobilev2.adapter.DeliveryItemAdapter
import com.appcentric.trackcentricmobilev2.databinding.ActivityDeliveryItemScreenBinding
import com.appcentric.trackcentricmobilev2.dbmodels.*
import com.appcentric.trackcentricmobilev2.utils.Common
import com.appcentric.trackcentricmobilev2.utils.SharePref
import com.google.android.material.textfield.TextInputLayout
import java.util.*


/*
* This is the Delivery document screen. This screen displays the list of delivery units.
* */
class DeliveryDocumentItemScreen : AppCompatActivity(),NoDataFound,onClickNextScreen {
    private lateinit var binding: ActivityDeliveryItemScreenBinding // This is the binding class which auto generates class according to view ids
    private lateinit var sharePref: SharePref // SharedPreferences class object to get data from memory
    private lateinit var dbObject: TrackCentricDB // Local Database object which gets data from local database
    private lateinit var adapter: DeliveryItemAdapter // This adapter class which binds the list of delivery unit
    private var deliveryItemList=ArrayList<DeliveryDocumentItemEntity>() // Array list which holds the data of deliver units
    private var listEvent=ArrayList<EventEntity>()
    private var listVehicleReason=ArrayList<VehicleReasonEntity>()
    var arrayAdapterEvent: ArrayAdapter<String>?=null
    var arrayAdapterVehicle: ArrayAdapter<String>?=null
    private lateinit var onClickNextScreen: onClickNextScreen // This is custom interface which handles click event of particular item
    private lateinit var noDataFound:NoDataFound; // This is custom interface which handles events

    private lateinit var deliveryObject: DeliveryDocumentEntity // Particular object deliver documents

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This is binding of particular screen layout
        binding = ActivityDeliveryItemScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initilization()
        setListener()
    }

    /*
    * The function of registered listener is to handle the events that are triggered when a user clicks a button. 
    * */
    private fun setListener() {
        binding.editSearchDeliveryItem.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                adapter.getFilter().filter(s.toString())
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        binding.btnOffLoad.setOnClickListener {

            // Need to check Permission
            // Need to check Arrived

            addDeliveryEvent(deliveryObject.deliveryId,Common.EVENT_ARRIVED,null)

            getDeliveryCustomDialog()

        }
    }

    /*
    * The function of this dialog box is for delivery confirmation. User can select delivery confirmation status such as Delivered or Undelivered. For Undelivered, user can also select the variance reason or the reason why the delivery is unsuccessful.
       * */
    private fun getDeliveryCustomDialog() {
        var postionEvent:Int=0;
        var postionReason:Int=0;



        listVehicleReason= dbObject.vehicleReasonDao().getVehicleReasonDB() as ArrayList<VehicleReasonEntity>

        var data=  dbObject.deliverDoctItem().getRejectedItem(deliveryItemList.get(0).deliveryId)
        if(data.size>0){
            listEvent= dbObject.eventDao().getPartialDelivery() as ArrayList<EventEntity>
        }else{
            listEvent= dbObject.eventDao().getDeliveryEvent() as ArrayList<EventEntity>
        }

        var eventString=ArrayList<String>()

        for(item in listEvent){
            eventString.add(item.name)
        }
        arrayAdapterEvent = ArrayAdapter<String>(
            this,android.R.layout.simple_dropdown_item_1line,android.R.id.text1,eventString
        )

        var reasonString=ArrayList<String>()

        for(item in listVehicleReason){
            reasonString.add(item.description)
        }
        arrayAdapterVehicle = ArrayAdapter<String>(
            this,android.R.layout.simple_dropdown_item_1line,android.R.id.text1,reasonString
        )

        val dialog = Dialog(this@DeliveryDocumentItemScreen)
        dialog.setContentView(R.layout.custom_delivery_dialog)
        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow()?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false)
        var selectDelivery=dialog.findViewById<AutoCompleteTextView>(R.id.selectDelivery)
        var selectDeliveryReason=dialog.findViewById<AutoCompleteTextView>(R.id.selectDeliveryReason)
        var layouDeliveryReason=dialog.findViewById<TextInputLayout>(R.id.layouDeliveryReason)
        var btnSaveUnite=dialog.findViewById<Button>(R.id.btnSaveUnite)
        var btnCancel=dialog.findViewById<Button>(R.id.btnCancel)
        selectDelivery.setAdapter(arrayAdapterEvent)
        selectDeliveryReason.setAdapter(arrayAdapterVehicle)
        selectDelivery.setOnItemClickListener { arg0, view, arg2, arg3 ->
            postionEvent=arg2
            if(listEvent.get(arg2).name.equals("Undelivered",true)){
                layouDeliveryReason.visibility=View.VISIBLE
            }else{
                layouDeliveryReason.visibility=View.GONE
            }
        }

        selectDeliveryReason.setOnItemClickListener { arg0, view, arg2, arg3 ->
            postionReason=arg2

        }
        btnSaveUnite.setOnClickListener {



            if(selectDelivery.text.toString().trim().isEmpty()){
                Toast.makeText(applicationContext,"Please Select item",Toast.LENGTH_LONG).show()
            }else{
                if(listEvent.get(postionEvent).name.equals("delivered",true)){

                    addDeliveryEvent(deliveryObject.deliveryId,Common.EVENT_DELIVERED,null)

                    updateDeliveryDocumentStatus(deliveryItemList.get(0).deliveryId,Common.DELIVERY_DELIVERED);
                    dialog.dismiss()

                    goToCamera(deliveryItemList.get(0).shipmentId,deliveryItemList.get(0).deliveryId)
                    /*  if(Common.isNetworkAvailable(applicationContext)) {
                          getDeliveryEventStatus(deliveryItemList.get(0).deliveryId,"DELIVERED",deliveryCreatedOn,null)
                      }else{
                          goToCamera(deliveryItemList.get(0).shipmentId,deliveryItemList.get(0).deliveryId)
                      }*/

                }/*else if(listEvent.get(postionEvent).name.equals("arrived",true)){

                    updateDeliveryDocumentStatus(deliveryItemList.get(0).deliveryId,Common.DELIVERY_ON_THE_WAY);
                    dialog.dismiss()

                    if(Common.isNetworkAvailable(applicationContext)) {
                        getDeliveryEventStatus(deliveryItemList.get(0).deliveryId,"ARRIVED",deliveryCreatedOn)
                    }else{
                        goToCamera(deliveryItemList.get(0).shipmentId,deliveryItemList.get(0).deliveryId)
                    }

                }else if(listEvent.get(postionEvent).name.equals("departed",true)){

                    updateDeliveryDocumentStatus(deliveryItemList.get(0).deliveryId,Common.DELIVERY_DELIVERED);
                    dialog.dismiss()

                    if(Common.isNetworkAvailable(applicationContext)) {
                        getDeliveryEventStatus(deliveryItemList.get(0).deliveryId,"DEPARTED",deliveryCreatedOn)
                    }else{
                        goToCamera(deliveryItemList.get(0).shipmentId,deliveryItemList.get(0).deliveryId)
                    }
                }*/else if(listEvent.get(postionEvent).name.equals("Undelivered",true)){

                    if(selectDeliveryReason.text.toString().trim().length==0){
                        Toast.makeText(applicationContext,"Please Select Reason of Delivery",Toast.LENGTH_LONG).show()
                    }else{


                        //Update Reason of complete
                        updateDeliveryDocumentStatus(
                            deliveryItemList.get(0).deliveryId,

                            Common.DELIVERY_UNDELIVERED);

                        updateDeliveryReason(deliveryItemList.get(0).deliveryId,listVehicleReason.get(postionReason).id,listVehicleReason.get(postionReason).description)

                        /*if(Common.isNetworkAvailable(applicationContext)) {
                            getDeliveryEventStatus(deliveryItemList.get(0).deliveryId,"UNDELIVERED",deliveryCreatedOn,selectDeliveryReason.text.toString())
                        }else{
                            goToCamera(deliveryItemList.get(0).shipmentId,deliveryItemList.get(0).deliveryId)
                        }*/
                        dialog.dismiss()
                        goToCamera(deliveryItemList.get(0).shipmentId,deliveryItemList.get(0).deliveryId)
                    }

                }else if(listEvent.get(postionEvent).name.equals("Partial Delivered",true)){

                    addDeliveryEvent(deliveryObject.deliveryId,Common.EVENT_DELIVERED,null)

                    updateDeliveryDocumentStatus(
                        deliveryItemList.get(0).deliveryId,
                        Common.DELIVERY_DELIVERED);

                    /*if(Common.isNetworkAvailable(applicationContext)) {
                        getDeliveryEventStatus(deliveryItemList.get(0).deliveryId,"PARTIAL DELIVERED",deliveryCreatedOn,null)
                    }else{
                        goToCamera(deliveryItemList.get(0).shipmentId,deliveryItemList.get(0).deliveryId)
                    }*/
                    dialog.dismiss()
                    goToCamera(deliveryItemList.get(0).shipmentId,deliveryItemList.get(0).deliveryId)
                }

            }


        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show();

    }

    /*
    *  This function redirects to Camera Screen
    * */
    fun goToCamera(shipmentId:String,deliveryId:String){
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra("deliveryId",deliveryId)
        intent.putExtra("shipmentId",shipmentId)
        startActivity(intent)
    }

    /*
    * This function updates delivery reason in the database
    * */
    fun updateDeliveryReason(deliveryId:String,vehicleReasonId:Int,vehicleReason:String){
        dbObject.deliveryDao().updateDeliveryReason(deliveryId,vehicleReasonId,vehicleReason)
    }

    /*
    * This function updates delivery status in the database
    * */
    fun updateDeliveryDocumentStatus(deliveryId: String,status:Int) {
        dbObject.deliveryDao().updateParticularStatus(deliveryId,status)
    }


    /*
    *  This function is used to create initial object when screen is rendered
    * */
    private fun initilization() {
        noDataFound=this
        onClickNextScreen=this
        dbObject= TrackCentricDB.getDatabase(applicationContext)
        setSupportActionBar(binding.deliveryItem.toolbar)
        binding.deliveryItem.titletoolbar.text = resources.getString(R.string.deliveryItemlist)
        val action = supportActionBar
        action?.title = ""
        action?.setDisplayHomeAsUpEnabled(true)
        binding.deliveryItem.toolbar.setNavigationOnClickListener {
            finish();
        }
        sharePref= SharePref(applicationContext)

        //Create Adapter class object
        adapter= DeliveryItemAdapter(applicationContext,deliveryItemList,deliveryItemList,
            noDataFound,onClickNextScreen
        )
        binding.deliveryDocItem.setLayoutManager( LinearLayoutManager(this))
        binding.deliveryDocItem.setAdapter(adapter); // Set list view
        getParticularDelivery()
        //Data is from local database
        getDeliveryListFromDB();


    }

    /*
    * This function adds delivery event in the local database
    * */
    fun addDeliveryEvent(deliveryId: String,event: String,reason: String?){
        var data = dbObject.deliveryEvent().getParticularEvent(deliveryObject.deliveryId,event)
        if(data.isNullOrEmpty()) {
            dbObject.deliveryEvent().insertDeliveryEvent(DeliveryEventEntity(0,deliveryId,event,reason,Common.getCurrentDate(Date()),false))
        }

    }

    /*
    * This function gets a particular delivery object from delivery table
    * */
    fun getParticularDelivery(){
        deliveryObject=  dbObject.deliveryDao().getParticularDelivery(intent.extras?.getString("deliveryId").toString())
        binding.deliveryNo.text=deliveryObject.deliveryNumber
        binding.deliveryTo.text=deliveryObject.deliverTo

        if(deliveryObject.status==Common.DELIVERY_NO_STATUS){
            binding.btnOffLoad.visibility=View.GONE
        }else{
            binding.btnOffLoad.visibility=View.VISIBLE
        }

    }

    /*
    * This function gets the list of delivery data from delivery table
    * */
    fun getDeliveryListFromDB(){
        deliveryItemList.clear()
        var data=  dbObject.deliverDoctItem().getDeliveryDocumentItem(intent.extras?.getString("deliveryId")
            .toString())
        if(data.isEmpty()){
            binding.noDataFound.visibility= View.VISIBLE
        }else{
            binding.noDataFound.visibility= View.GONE
        }
        deliveryItemList.addAll(data)
        adapter.notifyDataSetChanged()
    }

    /*
    * This is an override method which handles events like no data found
    * */
    override fun noDataFound(count: Int) {
        if(adapter.dataSet.size==0){
            binding.noDataFound.setText("No Data Found")
            binding.noDataFound.visibility=View.VISIBLE
        }else{
            binding.noDataFound.visibility=View.GONE
        }
    }

    /*private fun getShipmentEnd(shipmentStartDate: String) {

      var data=  dbObject.shipmentDao().getParticularShipment(deliveryItemList.get(0).shipmentId)

        binding.progressbar.visibility=View.VISIBLE
        val call: Call<ResponseBody> =
            APIClient.getClient.getShipmentEnd(shipmentStartDate, data.shipmentNumber)
        call.enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {

                    goToCamera(deliveryItemList.get(0).shipmentId,deliveryItemList.get(0).deliveryId)
                } else {
                    Common.showSnackBar(response.message(), binding.root)
                }
                binding.progressbar.visibility=View.GONE
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                 binding.progressbar.visibility = View.GONE
                t.printStackTrace()
                Common.showSnackBar(t.localizedMessage, binding.root)
            }
        })
    }*/


    /*private fun getDeliveryEventStatus(deliveryId: String,event:String,createOn:String,reason:String?) {


        binding.progressbar.visibility=View.VISIBLE
        val call: Call<ResponseBody> =
            APIClient.getClient.getDeliveryEvent(deliveryId,event,createOn,reason)
        call.enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {

                    goToCamera(deliveryItemList.get(0).shipmentId,deliveryItemList.get(0).deliveryId)
                } else {
                    Common.showSnackBar(response.message(), binding.root)
                }
                binding.progressbar.visibility=View.GONE
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                binding.progressbar.visibility = View.GONE
                t.printStackTrace()
                Common.showSnackBar(t.localizedMessage, binding.root)
            }
        })
    }*/

    /*
    *  This function handles click event for the list of items
    * */
    override fun onClickNextScreen(position:Int) {
        try {
            if(deliveryObject.status!=Common.DELIVERY_NO_STATUS) {
                var intent = Intent(this, UnitDetailScreen::class.java)
                intent.putExtra("shipmentId", deliveryItemList.get(position).shipmentId)
                intent.putExtra("qty", deliveryItemList.get(position).quantity)
                intent.putExtra("deliveryDocId", deliveryItemList.get(position).deliveryDocItemId)
                intent.putExtra("deliveryId", deliveryItemList.get(position).deliveryId)
                startActivityForResult(intent, 200)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    /*
    * This is the call back function which handles any update from other screens
    * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK){
            getDeliveryListFromDB()
        }
    }

}
