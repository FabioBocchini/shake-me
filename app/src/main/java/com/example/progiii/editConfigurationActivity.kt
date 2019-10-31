package com.example.progiii

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_edit_configuration.*
import android.view.View
import android.widget.*



class EditConfigurationActivity : AppCompatActivity() {

    var action: String? = null
    private var selectedApp: String = ""

    private var isBrowserAction: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_configuration)

        val etConfUrl: TextView = findViewById(R.id.etConfUrl)
        val etConfPlaceholder: TextView = findViewById(R.id.etConfPlaceholder)
        val btnConfApp: Button = findViewById(R.id.button)
        val tvConfApp: TextView = findViewById(R.id.tvConfApp)
        val swIsBrowser: Switch = findViewById(R.id.swIsBrowser)

        val sharedPref: SharedPreferences = getSharedPreferences("config_file", Context.MODE_PRIVATE)

        val labelNames: Map<String, String> = mapOf(
            "shake" to "Shake",
            "flip" to "Screen Down",
            "lTilt" to "Left Up",
            "up" to "Vertical"
        )

        action = intent.getStringExtra("ACTION")

        findViewById<TextView>(R.id.tvAction).apply{
            text = labelNames[action!!]
        }

        swIsBrowser.setOnCheckedChangeListener{ _ , isChecked ->
            if (isChecked){
                isBrowserAction = false
                etConfUrl.text = ""
                tvConfApp.text = sharedPref.getString("$action.appName", "")
            }
            else {
                isBrowserAction = true
                etConfUrl.text = sharedPref.getString("$action.url", "")
                tvConfApp.text = ""
            }
        }

        etConfPlaceholder.text =  sharedPref.getString("$action.placeholder", "")
        swIsBrowser.isChecked = !sharedPref.getBoolean("$action.isBrowserIntent", true)

        if (isBrowserAction){
            etConfUrl.text = sharedPref.getString("$action.url", "")
            tvConfApp.text = ""
        }
        else{
            etConfUrl.text = ""
            tvConfApp.text = sharedPref.getString("$action.appName", "")
        }

        btnConfApp.setOnClickListener{

            findViewById<ProgressBar>(R.id.pbLoading).visibility = View.VISIBLE

            swIsBrowser.isChecked = true

            val appIntent = Intent(this, ChooserActivity::class.java)
            startActivityForResult(appIntent, 1)
        }
    }

    fun updateData(view: View) {

        val etConfUrl: TextView = findViewById(R.id.etConfUrl)
        val etConfPlaceholder: TextView = findViewById(R.id.etConfPlaceholder)
        val tvConfApp: TextView = findViewById(R.id.tvConfApp)

        val sharedPref: SharedPreferences = getSharedPreferences("config_file", Context.MODE_PRIVATE)

        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean("$action.isBrowserIntent", isBrowserAction)
        editor.putString("$action.placeholder",etConfPlaceholder.text.toString())
        editor.putString("$action.appName",tvConfApp.text.toString())

        if (isBrowserAction)
            editor.putString("$action.url", etConfUrl.text.toString() )
        else
            editor.putString("$action.app", selectedApp)

        editor.apply()

        Toast.makeText(applicationContext, "Saved", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            if (requestCode == 1) {

                val packageName: String? = data.getStringExtra("MESSAGE_package_name")
                if ( packageName != null) {

                    val pm: PackageManager= applicationContext.packageManager
                    val ai: ApplicationInfo?

                    ai = try {
                        pm.getApplicationInfo(packageName,0)

                    } catch ( e: PackageManager.NameNotFoundException){

                        null
                    }

                    if (ai != null){

                        selectedApp = packageName
                        tvConfApp.text = pm.getApplicationLabel(ai).toString()
                    }

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        findViewById<ProgressBar>(R.id.pbLoading).visibility = View.GONE
    }
}

