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
            Typeface.createFromAsset(assets, "fonts/SourceSansPro-Regular.ttf")

        binding.bottomNavigation.apply {
            add(BubbleBottomNavigation.Model(ID_HOME, R.drawable.ic_home))
            add(BubbleBottomNavigation.Model(ID_EXPLORE, R.drawable.ic_explore))
            add(BubbleBottomNavigation.Model(ID_MESSAGE, R.drawable.ic_message))
            add(BubbleBottomNavigation.Model(ID_NOTIFICATION, R.drawable.ic_notification))
            add(BubbleBottomNavigation.Model(ID_ACCOUNT, R.drawable.ic_account))
            
            setCount(
                ID_NOTIFICATION, "115"
            )

            setOnShowListener { model ->
                val name = getMenuName(model.id)
                binding.fragmentSelected.text = getString(R.string.main_page_selected, name)
            }

            setOnClickMenuListener { model ->
                val name = getMenuName(model.id)
            }

            setOnReselectListener { model ->
                Toast.makeText(
                    this@MainActivity, "Item ${model.id} is reselected.", Toast.LENGTH_LONG
                ).show()
            }

            show(ID_HOME)
        }
    }

    private fun getMenuName(id: Int): String {
        return when (id) {
            ID_HOME -> "HOME"
            ID_EXPLORE -> "EXPLORE"
            ID_MESSAGE -> "MESSAGE"
            ID_NOTIFICATION -> "NOTIFICATION"
            ID_ACCOUNT -> "ACCOUNT"
            else -> ""
        }
    }
}