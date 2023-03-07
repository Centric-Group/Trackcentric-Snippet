package com.appcentric.trackcentricmobilev2.activity

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.appcentric.trackcentricmobilev2.BuildConfig
import com.appcentric.trackcentricmobilev2.R
import com.appcentric.trackcentricmobilev2.databinding.ActivityLoginScreenBinding
import com.appcentric.trackcentricmobilev2.models.LoginData
import com.appcentric.trackcentricmobilev2.models.VehiclesData
import com.appcentric.trackcentricmobilev2.utils.Common
import com.appcentric.trackcentricmobilev2.utils.Common.Companion.hideKeyboard
import com.appcentric.trackcentricmobilev2.utils.Common.Companion.isNetworkAvailable
import com.appcentric.trackcentricmobilev2.utils.Constance
import com.appcentric.trackcentricmobilev2.utils.SharePref
import com.google.gson.Gson
import com.qualityapp.bizzadsmaker.retrofit.APIClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 *  This Login Screen is for user authentication. It holds the Login UI screen
 */
class LoginScreen : AppCompatActivity() {


    private lateinit var binding: ActivityLoginScreenBinding // This is the binding class which auto generate class according to view ids
    private lateinit var sharePref: SharePref // SharedPreferences class object to get data from memory
    private var mLastClickTime: Long = 0 //this for prevent multiple click on button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This is binding of particular screen layout
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initilization()
    }

    private fun initilization() {
        //This is used to setup toolbar and set text in the tool bar
        setSupportActionBar(binding.configtool.toolbar)
        binding.configtool.titletoolbar.text = resources.getString(R.string.loginScreen)
        val action = supportActionBar
        action?.title = ""
        action?.setDisplayHomeAsUpEnabled(false)
        sharePref = SharePref(applicationContext)

        binding.btnLogin.setOnClickListener {
            //this is to prevent double tap
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return@setOnClickListener;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            hideKeyboard(binding.btnLogin)

            //This condition checks the validation of plate number
            if (binding.editNumberPlat.text?.trim()?.length == 0) {
                Common.showSnackBar(
                    resources.getString(R.string.msgPlatNo),
                    binding.btnLogin
                )
            } else if (isNetworkAvailable(applicationContext)) {
                //Called Authentication API
                getLoginAPI()
            }

        }

        binding.btnClear.setOnClickListener {
            hideKeyboard(binding.btnLogin) // Hide the keyboard when user click on clear button
            binding.editNumberPlat.text?.clear()
        }
    }

    /*
    * This function for getting authentication from the server
    * */
    fun getLoginAPI() {
        binding.progressbar.visibility = View.VISIBLE
        val call: Call<LoginData> =
            APIClient.getClient.getLoginAPI(
                sharePref.getClientUserName(),
                sharePref.getClientPassword(),
                sharePref.getClientID(),
                Constance.source,
                BuildConfig.VERSION_CODE
            )
        call.enqueue(object : Callback<LoginData> {

            override fun onResponse(call: Call<LoginData>, response: Response<LoginData>) {
                //After getting success response store the value in the local memory
                if (response.isSuccessful) {
                    Log.i("LoginResponse", Gson().toJson(response.body()))
                    sharePref.setAccessToken(response.body()!!.id)
                    sharePref.setLoginResponse(Gson().toJson(response.body()))
                    sharePref.setPlatNumber(binding.editNumberPlat.text?.trim().toString())

                    //Getting vehicle list from server
                    getVehicle()

                } else {
                    //Display message if any error from server side
                    Common.showSnackBar(response.message(), binding.btnLogin)
                }

            }

            override fun onFailure(call: Call<LoginData>, t: Throwable) {
                binding.progressbar.visibility = View.GONE
                t.printStackTrace()
                Common.showSnackBar(t.localizedMessage, binding.btnLogin)
            }
        })
    }

    /*
    *  This function is called Vehicle API which gets the list of vehicles from server.
    * */
    private fun getVehicle() {
        var jsonObject = JSONObject()
        var jsonObjectdata = JSONObject()
        jsonObjectdata.put("clientId", sharePref.getClientID())
        jsonObjectdata.put("name", sharePref.getPlatNumber())
        jsonObject.put("where", jsonObjectdata)

        val call: Call<VehiclesData> =
            APIClient.getClient.getVehicle(jsonObject.toString())
        call.enqueue(object : Callback<VehiclesData> {

            override fun onResponse(call: Call<VehiclesData>, response: Response<VehiclesData>) {
                binding.progressbar.visibility = View.GONE
                if (response.isSuccessful && response.body()?.size!! > 0) {
                    var intent = Intent(applicationContext, SyncScreen::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Common.showSnackBar(
                        "Please Enter Valid Plate No." +
                                "", binding.root
                    )
                }

            }

            override fun onFailure(call: Call<VehiclesData>, t: Throwable) {
                binding.progressbar.visibility = View.GONE
                t.printStackTrace()
                Common.showSnackBar(t.localizedMessage, binding.root)
            }
        })
    }

    /*
    *  This function binds the menu view
    * */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_item, menu)
        return true
    }

    /*
    *  This function handles click event of each menu of screen
    * */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.itemsetting -> {
                var mIntent = Intent(applicationContext, ConfigurationScreen::class.java);
                startActivity(mIntent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
