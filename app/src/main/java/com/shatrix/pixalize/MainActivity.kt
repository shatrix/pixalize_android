package com.shatrix.pixalize

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WallpaperAdapter
    private var db: FirebaseFirestore? = null  // Nullable for when Firebase is not configured
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private var selectedCategory: String = "All" // Default category
    private var isFirebaseConfigured = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Try to initialize Firebase (may fail if google-services.json is missing)
        try {
            FirebaseApp.initializeApp(this)
            db = FirebaseFirestore.getInstance()
            isFirebaseConfigured = true
        } catch (e: Exception) {
            isFirebaseConfigured = false
            // Firebase not configured - show message after UI is set up
        }

        val fabAbout: ImageButton = findViewById(R.id.button_app_logo)

        // Set onClickListener to open the "About" dialog
        fabAbout.setOnClickListener {
            showAboutDialog()
        }

        // Set the background color for the entire window
        window.setBackgroundDrawableResource(R.color.back_too_dark)

        recyclerView = findViewById(R.id.recyclerView_wallpapers)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        setupBottomNavigation()

        // Set up pull-to-refresh functionality
        swipeRefreshLayout.setOnRefreshListener {
            fetchWallpapers(selectedCategory) // Refresh wallpapers based on current category
        }

        // Set up GridLayoutManager to display 2 thumbnails per row
        recyclerView.layoutManager = GridLayoutManager(this, 2) // Set 2 columns in grid

        // Show Firebase setup message if not configured
        // Show Firebase setup message if not configured, but still load demo wallpapers
        if (!isFirebaseConfigured) {
            loadDemoWallpapers(selectedCategory)
        } else {
            // Load initial wallpapers
            fetchWallpapers(selectedCategory)
        }
    }
    
    /**
     * Load demo wallpapers from SampleData when Firebase is not configured.
     * Shows a Snackbar with setup instructions.
     */
    private fun loadDemoWallpapers(category: String) {
        val sampleWallpapers = SampleData.getWallpapers(category)
        
        adapter = WallpaperAdapter(sampleWallpapers) { wallpaper ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("imageUrl", wallpaper.imageUrl)
                putExtra("imageName", wallpaper.name)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        swipeRefreshLayout.isRefreshing = false
        
        // Show info message only once
        if (category == "All") {
            Snackbar.make(
                recyclerView,
                "Demo mode: Using sample wallpapers. Set up Firebase for your own content.",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun showAboutDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_about)

        // Set app icon, about text and button listeners
        val aboutTextView: TextView = dialog.findViewById(R.id.about_text)
        val appIcon: ImageView = dialog.findViewById(R.id.app_icon)
        val buttonTwitter: Button = dialog.findViewById(R.id.button_twitter)
        val buttonGithub: Button = dialog.findViewById(R.id.button_github)

        // Set the about text
        val aboutText = """
        Pixalize Wallpapers App
        High-quality, AI-generated wallpapers, Free for everyone
        ***
        Developed by Shatrix
        """.trimIndent()
        aboutTextView.text = aboutText

        // Set listener for Twitter button
        buttonTwitter.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://twitter.com/shatrix"))
            startActivity(intent)
        }

        // Set listener for GitHub button
        buttonGithub.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://github.com/shatrix"))
            startActivity(intent)
        }

        // Show the dialog
        dialog.show()
    }

    // Setting up the static BottomNavigationView for predefined categories
    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            selectedCategory = when (item.itemId) {
                R.id.category_dark -> "Dark"
                R.id.category_light -> "Light"
                R.id.category_all -> "All"
                R.id.category_animals -> "Animals"
                R.id.category_birds -> "Birds"
                else -> "All"
            }
            fetchWallpapers(selectedCategory) // Load wallpapers based on selected category
            true
        }

        // Set a default category (e.g., "All")
        bottomNavigationView.selectedItemId = R.id.category_all
    }

    // Function to fetch wallpapers from Firestore based on category
    private fun fetchWallpapers(category: String) {
        // Check if Firebase is configured
        val firestore = db ?: run {
            loadDemoWallpapers(category)
            return
        }
        
        val query = if (category == "All") {
            firestore.collection("wallpapers")
        } else {
            firestore.collection("wallpapers").whereArrayContains("categories", category)
        }

        query.get()
            .addOnSuccessListener { result ->
                val wallpapers = result.documents.mapNotNull { doc ->
                    val name = doc.getString("name")
                    val imageUrl = doc.getString("imageUrl")
                    if (name != null && imageUrl != null) {
                        Wallpaper(name, imageUrl)
                    } else {
                        null
                    }
                }

                if (wallpapers.isEmpty()) {
                    // Show a message if no wallpapers are found
                    Snackbar.make(recyclerView, "No wallpapers found", Snackbar.LENGTH_SHORT).show()
                }

                // Shuffle the wallpapers list to randomize the order
                val shuffledWallpapers = wallpapers.shuffled()

                adapter = WallpaperAdapter(shuffledWallpapers) { wallpaper ->
                    val intent = Intent(this, DetailActivity::class.java).apply {
                        putExtra("imageUrl", wallpaper.imageUrl)
                        putExtra("imageName", wallpaper.name)
                    }
                    startActivity(intent)
                }
                recyclerView.adapter = adapter
                swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { exception ->
                // Show error message using Snackbar or Toast
                Snackbar.make(recyclerView, "Failed to load wallpapers: ${exception.message}", Snackbar.LENGTH_LONG).show()
                swipeRefreshLayout.isRefreshing = false
            }
    }
}
