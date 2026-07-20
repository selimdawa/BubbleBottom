package com.flatcode.bubblebottom

import android.graphics.Typeface
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.flatcode.bubblebottom.databinding.ActivityMainBinding
import io.selimdawa.bubblebottom.BubbleBottomNavigation
import io.selimdawa.bubblebottom.Model

class MainActivity : AppCompatActivity() {

    companion object {
        private const val ID_HOME = 1
        private const val ID_EXPLORE = 2
        private const val ID_MESSAGE = 3
        private const val ID_NOTIFICATION = 4
        private const val ID_ACCOUNT = 5
    }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUI()
    }

    private fun setupUI() {
        binding.fragmentSelected.typeface =
            Typeface.createFromAsset(assets, getString(R.string.font_source_sans_pro_regular))

        binding.bottomNavigation.apply {
            add(Model(ID_HOME, R.drawable.ic_home))
            add(Model(ID_EXPLORE, R.drawable.ic_explore))
            add(Model(ID_MESSAGE, R.drawable.ic_message))
            add(Model(ID_NOTIFICATION, R.drawable.ic_notification))
            add(Model(ID_ACCOUNT, R.drawable.ic_account))
            
            setCount(ID_NOTIFICATION, getString(R.string.notification_count))

            setOnShowListener { model ->
                binding.fragmentSelected.text = getString(R.string.main_page_selected, getMenuName(model.id))
            }

            setOnClickMenuListener { _ ->
                // Handle menu click if needed
            }

            setOnReselectListener { model ->
                Toast.makeText(
                    this@MainActivity, getString(R.string.reselected_message, model.id), Toast.LENGTH_LONG
                ).show()
            }

            show(ID_HOME)
        }
    }

    private fun getMenuName(id: Int): String {
        val resId = when (id) {
            ID_HOME -> R.string.menu_home
            ID_EXPLORE -> R.string.menu_explore
            ID_MESSAGE -> R.string.menu_message
            ID_NOTIFICATION -> R.string.menu_notification
            ID_ACCOUNT -> R.string.menu_account
            else -> return ""
        }
        return getString(resId)
    }
}