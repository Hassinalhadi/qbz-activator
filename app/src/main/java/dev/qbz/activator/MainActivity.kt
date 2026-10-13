package dev.qbz.activator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnLaunch = findViewById<Button>(R.id.btnLaunchQbz)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        tvStatus.text = "⚡ QBZ Activator Active\nTarget: net.jahez.lsposed\nLicense: Lifetime VIP (2099)"

        btnLaunch.setOnClickListener {
            try {
                val intent = packageManager.getLaunchIntentForPackage("net.jahez.lsposed")
                if (intent != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "QBZ (net.jahez.lsposed) is not installed!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to launch: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
