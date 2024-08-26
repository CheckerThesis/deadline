package com.example.deadline

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import java.io.IOException
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener
import com.github.barteksc.pdfviewer.listener.OnTapListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import android.view.animation.AlphaAnimation
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity(), OnPageScrollListener {
    private lateinit var pdfView: PDFView

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private var isToolbarVisible = false

    private val toolbarAutoHideDelay: Long = 3000

    private val hideToolbarRunnable = Runnable {
        hideToolbar()
    }

    private val handler = Handler()

    private var currentPdfUri: Uri? = null
    private var uri: Uri? = null // Declare the uri variable

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val layoutParams = window.attributes
            layoutParams.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = layoutParams
        }
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        window.insetsController?.hide(WindowInsets.Type.statusBars())

        // Adjust layout to handle display cutouts (optional)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.decorView.setOnApplyWindowInsetsListener { _, insets ->
                val cutoutSafeInsets = insets.getInsets(WindowInsets.Type.systemBars())
                // Adjust your layout with cutoutSafeInsets if needed
                insets
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }

        setContentView(R.layout.activity_main)

        pdfView = findViewById(R.id.pdfView)

        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        toolbar.visibility = View.GONE

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val isPdfSwipeHorizontal = sharedPreferences.getBoolean("pdf_swipe_horizontal_value", true)

        val intent = intent
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_VIEW == action && "application/pdf" == type) {
            uri = intent.data // Assign value to the class-level uri variable
            if (uri != null) {
                displayPdfFromUri(uri, true)
            }
        }
    }

    fun getUri(): Uri? {
        return currentPdfUri
    }

    fun displayPdfFromUri(uri: Uri?, isPdfSwipeHorizontal: Boolean) {
        try {
            val inputStream = contentResolver.openInputStream(uri!!)

            val pdfViewBuilder = pdfView.fromStream(inputStream)
                .defaultPage(0)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .onPageScroll(this)
                .onTap(onTapListener)
                .scrollHandle(DefaultScrollHandle(this))
                .nightMode(isDarkModeEnabled())
                .enableDoubletap(true)

            pdfViewBuilder.load()

            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayName = it.getString(it.getColumnIndexOrThrow(
                        android.provider.OpenableColumns.DISPLAY_NAME))
                    supportActionBar?.title = displayName // Set the support action bar title
                }
            }
            currentPdfUri = uri
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }



    private val onTapListener = object : OnTapListener {
        override fun onTap(e: MotionEvent?): Boolean {
            if (isToolbarVisible) {
                hideToolbar()
            } else {
                showToolbar()
                handler.removeCallbacks(hideToolbarRunnable)
                handler.postDelayed(hideToolbarRunnable, toolbarAutoHideDelay)
            }
            return false
        }
    }

    private fun showToolbar() {
        val fadeInAnimation = AlphaAnimation(0f, 1f)
        fadeInAnimation.duration = 200 // Adjust the duration of the fade-in animation
        toolbar.startAnimation(fadeInAnimation)
        toolbar.visibility = View.VISIBLE
        isToolbarVisible = true
    }

    private fun hideToolbar() {
        if (!isToolbarVisible) {
            return // Toolbar is already hidden, no need to hide it again
        }

        val fadeOutAnimation = AlphaAnimation(1f, 0f)
        fadeOutAnimation.duration = 200 // Adjust the duration of the fade-out animation
        toolbar.startAnimation(fadeOutAnimation)
        toolbar.visibility = View.GONE
        isToolbarVisible = false
    }

    private fun isDarkModeEnabled(): Boolean {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }

    override fun onPageScrolled(page: Int, positionOffset: Float) {
        // Hide the toolbar when the PDFView is being scrolled up
        if (positionOffset > 0.2) { // Adjust this value for sensitivity
            hideToolbar()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the pending callbacks when the activity is destroyed
        handler.removeCallbacks(hideToolbarRunnable)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // Find the settings menu item by its ID
        val moreItem = menu.findItem(R.id.action_more)

        // Set the custom icon with padding for the settings menu item
        moreItem?.setIcon(R.drawable.ic_more_with_padding)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_more -> {
                // Show the popup menu when the settings menu item is clicked
                showPopupMenu(findViewById(R.id.action_more))
                return true
            }
            // Add more menu item cases if needed
        }
        return super.onOptionsItemSelected(item)
    }

    // Inside your MainActivity class
    private fun sharePdf(uri: Uri?) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "application/pdf"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(shareIntent, "Share PDF"))
    }

    private fun showPopupMenu(anchorView: View) {
        val popupMenu = PopupMenu(this, anchorView)
        popupMenu.inflate(R.menu.menu_popup)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    // Handle the "Settings" option click here
                    // Replace the following line with the code to open the SettingsActivity
                    // for example:
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_share -> {
                    currentPdfUri?.let {
                        sharePdf(it)  // Use the stored URI to share the PDF
                    }
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }
}