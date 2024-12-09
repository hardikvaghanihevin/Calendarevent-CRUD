package com.example.calendareventcrud

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.calendareventcrud.databinding.ActivityMainBinding

const val BASE_TAG = "A_"
const val KEY_EVENT = "eventKey"

class MainActivity : AppCompatActivity() {

    private final val TAG = BASE_TAG + MainActivity::class.java.simpleName

    private lateinit var binding: ActivityMainBinding

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //val navController = findNavController(R.id.nav_host_fragment_content_main)
        //appBarConfiguration = AppBarConfiguration(setOf(R.id.eventFragment, R.id.newEventFragment), null)
        //setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}