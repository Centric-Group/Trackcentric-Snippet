package com.appcentric.trackcentricmobilev2.activity


import android.content.Intent
import android.os.Bundle
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import com.appcentric.trackcentricmobilev2.R
import com.appcentric.trackcentricmobilev2.databinding.ActivityConfigurationScreenBinding
import com.appcentric.trackcentricmobilev2.utils.Common
import com.appcentric.trackcentricmobilev2.utils.Common.Companion.hideKeyboard
import com.appcentric.trackcentricmobilev2.utils.SharePref
import com.qualityapp.bizzadsmaker.retrofit.APIClient
/*
* This is configuration screen for first time log in. The app user must enter the configuration details from the admin before the app user can log in to his/her account. The app user is the fleet/truck driver.
* */
class ConfigurationScreen : AppCompatActivity() {

    private lateinit var binding: ActivityConfigurationScreenBinding // This is the binding class which auto generate class according to view ids
    private lateinit var sharePref: SharePref // SharedPreferences class object to get data from memory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This is binding of particular screen layout
        binding = ActivityConfigurationScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initilization()
        setListener()
    }

    /*
    * This function is used to register each and every event
    * */
    private fun setListener() {
        binding.btnSaveConfig.setOnClickListener {
            hideKeyboard(binding.btnSaveConfig)
            //Validation for Save Configuration
            if (binding.editUrl.text?.trim()?.length == 0) {
                Common.showSnackBar(
                    resources.getString(R.string.msgEnterUrl),
                    binding.btnSaveConfig
                )
            } else if (!URLUtil.isValidUrl(binding.editUrl.text?.trim()?.toString())) {
                Common.showSnackBar(
                    resources.getString(R.string.msgEnterValidUrl),
                    binding.btnSaveConfig
                )
            } else if (binding.editClientId.text?.trim()?.length == 0) {
                Common.showSnackBar(
                    resources.getString(R.string.msgEnterClientId),
                    binding.btnSaveConfig
                )
            } else if (binding.editUserName.text?.trim()?.length == 0) {
                Common.showSnackBar(
                    resources.getString(R.string.msgEnterUserName),
                    binding.btnSaveConfig
                )
            } else if (binding.editPassword.text?.trim()?.length == 0) {
                Common.showSnackBar(
                    resources.getString(R.string.msgEnterPassword),
                    binding.btnSaveConfig
                )
            } else {

                // Save the data in the memory
                sharePref.setClientBaseURL(binding.editUrl.text.toString())
                sharePref.setClientID(binding.editClientId.text.toString())
                sharePref.setClientUserName(binding.editUserName.text.toString())
                sharePref.setClientPassword(binding.editPassword.text.toString())

                Common.showSnackBar(
                    resources.getString(R.string.msgSaveSuccess),
                    binding.btnSaveConfig
                )

                //Redirection in the Login Screen
                var mIntent = Intent(applicationContext, LoginScreen::class.java);
                startActivity(mIntent)
                finish()

            }

        }
    }

    /*
    * This function is used to create object and set data when the screen is rendered
    * */
    private fun initilization() {
        setSupportActionBar(binding.configtool.toolbar)
        binding.configtool.titletoolbar.text = resources.getString(R.string.setConfiguration)
        val action = supportActionBar
        action?.title = ""
        action?.setDisplayHomeAsUpEnabled(false)

        sharePref = SharePref(applicationContext)

        binding.editUrl.setText(sharePref.getClientBaseURL())
        binding.editClientId.setText(sharePref.getClientID())
        binding.editUserName.setText(sharePref.getClientUserName())
        binding.editPassword.setText(sharePref.getClientPassword())

    }

}
