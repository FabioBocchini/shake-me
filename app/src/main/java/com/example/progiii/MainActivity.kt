package com.example.progiii

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.TextView
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.view.View
import com.github.nisrulz.sensey.*


private const val VERSION_NUMBER: String = "1.0.1"

class MainActivity : AppCompatActivity() {

    data class Action(var isBrowserIntent: Boolean, var url: String,var app: String, var placeholder: String)

    private val defaultLinks: Map<String, String> = mapOf(
        "shake" to "https://tinyurl.com/yxg7yhmm",
        "flip" to "https://tinyurl.com/yylheamx",
        "tilt" to "https://tinyurl.com/y3lmur3b",
        "up" to "https://tinyurl.com/y4k42qwt"
    )

    private var shakeListener:  ShakeDetector.ShakeListener? = null
    private var flipListener: FlipDetector.FlipListener? = null
    private var rotationListener: RotationAngleDetector.RotationAngleListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get Id
        val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val tvFlip: TextView = findViewById(R.id.tvGestureFlip)
        val tvShake: TextView = findViewById(R.id.tvGestureShake)
        val tvTilt: TextView = findViewById(R.id.tvGestureChop)
        val tvUp: TextView = findViewById(R.id.tvGestureUp)
        val tvVersion: TextView = findViewById(R.id.tvVersion)
        tvVersion.text = VERSION_NUMBER

        //create Action data objects
        val shakeAction = getActionPreferences("shake")
        val flipAction= getActionPreferences("flip")
        val tiltAction= getActionPreferences("tilt")
        val upAction= getActionPreferences("up")

        //set Labels
        tvShake.text = shakeAction.placeholder
        tvFlip.text = flipAction.placeholder
        tvTilt.text = tiltAction.placeholder
        tvUp.text = upAction.placeholder

        //create Listeners
        shakeListener = object: ShakeDetector.ShakeListener{
            override fun onShakeDetected() {
                vibrate(vibrator, 400)
                createIntent(shakeAction)
            }

            override fun onShakeStopped() {
               //do nothing
            }
        }

        flipListener= object: FlipDetector.FlipListener{
            override fun onFaceUp() {
               //do nothing
            }

            override fun onFaceDown() {
                vibrate(vibrator, 400)
                createIntent(flipAction)
            }

        }

        rotationListener= RotationAngleDetector.RotationAngleListener { _, y, z ->
                if(((z > 88) and (z < 92)) and ((y > 0) or (y < -4))) {
                    vibrate(vibrator, 200)
                    createIntent(tiltAction)
                }
                else if(((y > -4) and (y < 0)) and ((z > 92) or (z < 88))){
                    vibrate(vibrator, 200)
                    createIntent(upAction)
                }
            }

        startListeners()
    }

    fun vibrate(vibrator:Vibrator, milliseconds: Long){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    milliseconds,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            //deprecated in API 26
            vibrator.vibrate(milliseconds)
        }
    }

    fun createIntent(action: Action){

        if(action.isBrowserIntent) {
            var url: String = action.url
            url = if (!url.startsWith("http://") && !url.startsWith("https://"))
                    "http://$url"
                else
                    url

            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }
        else{
            val applicationIntent = Intent(applicationContext.packageManager.getLaunchIntentForPackage(action.app))
            startActivity(applicationIntent)
        }
    }

    fun viewConfigurations(view: View){
        //creating an intent to start another activity
        val viewIntent= Intent(this, ViewConfigurationActivity::class.java)
        startActivity(viewIntent)
    }

    private fun getActionPreferences(actionType: String): Action {

        val sharedPref: SharedPreferences = getSharedPreferences("config_file", Context.MODE_PRIVATE)

        //check if user sharedPreferences are present

        val actionUrl: String = sharedPref.getString("$actionType.url","")!!
        val actionApp: String = sharedPref.getString("$actionType.app","")!!

        if((actionUrl.trim() != "") or (actionApp.trim() != "")){
            //set preferences

            val isBrowser: Boolean =  sharedPref.getBoolean("$actionType.isBrowserIntent", true)
            val placeholder: String = sharedPref.getString("$actionType.placeholder","")!!.trim()

            val visualizedPlaceholder: String = if(placeholder != ""){
                 placeholder
            }
            else{
                if (isBrowser)
                    actionUrl
                else
                    sharedPref.getString("$actionType.appName","")!!
            }

            return Action(
                isBrowser,
                actionUrl,
                actionApp,
                visualizedPlaceholder
            )
        }
        else {
            //use default values
            return Action(
                true,
                defaultLinks.getValue(actionType),
                "",
                "-- DEFAULT --"
            )
        }
    }

    private fun startListeners(){
        Sensey.getInstance().init(applicationContext)

        Sensey.getInstance().startShakeDetection(15F,1, shakeListener)
        Sensey.getInstance().startFlipDetection(flipListener)
        Sensey.getInstance().startRotationAngleDetection(rotationListener)
    }

    private fun stopListeners(){
        Sensey.getInstance().stopShakeDetection(shakeListener)
        Sensey.getInstance().stopFlipDetection(flipListener)
        Sensey.getInstance().stopRotationAngleDetection(rotationListener)

        Sensey.getInstance().stop()
    }

    private fun refresh(){
        val tvFlip: TextView = findViewById(R.id.tvGestureFlip)
        val tvShake: TextView = findViewById(R.id.tvGestureShake)
        val tvTilt: TextView = findViewById(R.id.tvGestureChop)
        val tvUp: TextView = findViewById(R.id.tvGestureUp)

        val shakeAction = getActionPreferences("shake")
        val flipAction= getActionPreferences("flip")
        val tiltAction= getActionPreferences("tilt")
        val upAction= getActionPreferences("up")

        tvShake.text = shakeAction.placeholder
        tvFlip.text = flipAction.placeholder
        tvTilt.text = tiltAction.placeholder
        tvUp.text = upAction.placeholder

        startListeners()
    }

    override fun onPause() {
        super.onPause()

        stopListeners()
    }

    override fun onResume() {
        super.onResume()

        refresh()

    }

    override fun onRestart() {
        super.onRestart()

        startListeners()
    }

    override fun onDestroy() {
        super.onDestroy()

        stopListeners()
    }
}
