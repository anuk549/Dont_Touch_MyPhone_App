package com.example.donttouchmyphone

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.donttouchmyphone.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private var acceleration = 0f
    private var accelerationCurrent = 0f
    private var accelerationLast = 0f
    private val threshold = 10.0f
    private lateinit var mediaPlayer: MediaPlayer
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Set Full Screen
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        // Init MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.alarmpolice)
        mediaPlayer.isLooping = true
        // Init Sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI)
        }
        // Stop alarm button
        binding.btnStopAlarm.setOnClickListener {
            stopAlarm()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val x = event?.values?.get(0) ?: 0f
        val y = event?.values?.get(1) ?: 0f
        val z = event?.values?.get(2) ?: 0f

        accelerationLast = accelerationCurrent
        accelerationCurrent = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta = kotlin.math.abs(accelerationCurrent - accelerationLast)
        acceleration = acceleration * 0.7f + delta

        if (acceleration > threshold) {
            binding.apply {
                tvMotionAlert.text = getString(R.string.motion_detected)
                tvMotionAlert.setTextColor(ActivityCompat.getColor(this@MainActivity, R.color.yellow))
                mediaPlayer.start()
                animationView.setAnimation(R.raw.danger)
                animationView.playAnimation()
                btnStopAlarm.visibility = android.view.View.VISIBLE
            }
            // Capture photo or log event here
            Log.d(TAG, "Motion detected at: ${System.currentTimeMillis()}")
            // Show a notification
            Toast.makeText(this, "Motion detected!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopAlarm() {
        mediaPlayer.stop()
        binding.apply {
            tvMotionAlert.text = getString(R.string.waiting_for_motion)
            tvMotionAlert.setTextColor(ActivityCompat.getColor(this@MainActivity, R.color.green))
            animationView.setAnimation(R.raw.shield)
            animationView.playAnimation()
            btnStopAlarm.visibility = android.view.View.GONE
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle sensor accuracy changes if needed
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        mediaPlayer.release()
    }
}
