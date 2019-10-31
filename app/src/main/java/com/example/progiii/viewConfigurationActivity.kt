package com.example.progiii

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView


class ActionConfiguration(
    private val action: String,
    private val tvAction: TextView,
    private val tvUrl: TextView,
    private val tvApp: TextView,
    private val tvPlaceholder: TextView,
    private val sharedPref: SharedPreferences
) {

    fun fetchPreferences() {

        if (sharedPref.getBoolean("$action.isBrowserIntent", true)) {
            tvUrl.text = sharedPref.getString("$action.url", "")
            tvApp.text = " "
            tvPlaceholder.text = sharedPref.getString("$action.placeholder", action)
        } else {
            tvApp.text = sharedPref.getString("$action.appName", "")
            tvUrl.text = " "
            tvPlaceholder.text = sharedPref.getString("$action.placeholder", action)
        }
    }

    fun getAction(): String {
        return this.action
    }

    fun getTvAction(): TextView {
        return this.tvAction
    }
}

class ViewConfigurationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_configuration)

        loadData()
    }

    private fun loadData(){

        val sharedPref: SharedPreferences = getSharedPreferences("config_file", Context.MODE_PRIVATE)

        val actionConfigurationList: Map<String, ActionConfiguration> = mapOf(

            "shake" to ActionConfiguration(
                "shake",
                findViewById((R.id.tvEditShake)),
                findViewById(R.id.tvShakeUrlSelected),
                findViewById(R.id.tvShakeAppSelected),
                findViewById(R.id.tvShakePlaceholderSelected),
                sharedPref
            ),
            "flip" to ActionConfiguration(
                "flip",
                findViewById(R.id.tvEditFlip),
                findViewById(R.id.tvFlipUrlSelected),
                findViewById(R.id.tvFlipAppSelected),
                findViewById(R.id.tvFlipPlaceholderSelected),
                sharedPref
            ),
            "tilt" to ActionConfiguration(
                "tilt",
                findViewById(R.id.tvEditTilt),
                findViewById(R.id.tvTiltUrlSelected),
                findViewById(R.id.tvTiltAppSelected),
                findViewById(R.id.tvTiltPlaceholderSelected),
                sharedPref
            ),

            "up" to ActionConfiguration(
                "up",
                findViewById(R.id.tvEditUp),
                findViewById(R.id.tvUpUrlSelected),
                findViewById(R.id.tvUpAppSelected),
                findViewById(R.id.tvUpPlaceholderSelected),
                sharedPref
            )
        )

        for (action in actionConfigurationList) {
            action.value.fetchPreferences()

            action.value.getTvAction().setOnClickListener { v ->

                editConfiguration(v, action.value.getAction())
            }
        }
    }

    private fun editConfiguration(view: View, action: String){
        //creating an intent to start another activity
        val editIntent= Intent(this, EditConfigurationActivity::class.java).apply {
            putExtra("ACTION", action )
        }

        startActivity(editIntent)
    }

    override fun onRestart() {
        super.onRestart()

        loadData()
    }
}