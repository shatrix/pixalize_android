package com.shatrix.pixalize

/**
 * Sample wallpaper data for demo purposes.
 * These wallpapers are loaded when Firebase is not configured.
 * All images are from Unsplash (free to use).
 */
object SampleData {
    
    /**
     * Get sample wallpapers for a category.
     * Uses Unsplash Source API for direct image URLs.
     */
    fun getWallpapers(category: String): List<Wallpaper> {
        return when (category) {
            "Dark" -> darkWallpapers
            "Light" -> lightWallpapers
            "Animals" -> animalWallpapers
            "Birds" -> birdWallpapers
            else -> allWallpapers
        }
    }
    
    private val darkWallpapers = listOf(
        Wallpaper("Starry Night", "https://images.unsplash.com/photo-1507400492013-162706c8c05e?w=1080&q=80"),
        Wallpaper("Dark Mountains", "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1080&q=80"),
        Wallpaper("Night Sky", "https://images.unsplash.com/photo-1475274047050-1d0c0975c63e?w=1080&q=80"),
        Wallpaper("Dark Forest", "https://images.unsplash.com/photo-1448375240586-882707db888b?w=1080&q=80"),
        Wallpaper("Nebula Dreams", "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?w=1080&q=80"),
        Wallpaper("Dark Ocean", "https://images.unsplash.com/photo-1505142468610-359e7d316be0?w=1080&q=80")
    )
    
    private val lightWallpapers = listOf(
        Wallpaper("Sunny Beach", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1080&q=80"),
        Wallpaper("White Mountains", "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=1080&q=80"),
        Wallpaper("Pastel Sky", "https://images.unsplash.com/photo-1501854140801-50d01698950b?w=1080&q=80"),
        Wallpaper("Morning Light", "https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?w=1080&q=80"),
        Wallpaper("Soft Clouds", "https://images.unsplash.com/photo-1534088568595-a066f410bcda?w=1080&q=80"),
        Wallpaper("Pink Sunset", "https://images.unsplash.com/photo-1495616811223-4d98c6e9c869?w=1080&q=80")
    )
    
    private val animalWallpapers = listOf(
        Wallpaper("Majestic Lion", "https://images.unsplash.com/photo-1546182990-dffeafbe841d?w=1080&q=80"),
        Wallpaper("Arctic Fox", "https://images.unsplash.com/photo-1474511320723-9a56873571b7?w=1080&q=80"),
        Wallpaper("Wild Tiger", "https://images.unsplash.com/photo-1549480017-d76466a4b7e8?w=1080&q=80"),
        Wallpaper("Elephant", "https://images.unsplash.com/photo-1557050543-4d5f4e07ef46?w=1080&q=80"),
        Wallpaper("Deer in Forest", "https://images.unsplash.com/photo-1484406566174-9da000fda645?w=1080&q=80"),
        Wallpaper("Wolf Pack", "https://images.unsplash.com/photo-1564466809058-bf4114d55352?w=1080&q=80")
    )
    
    private val birdWallpapers = listOf(
        Wallpaper("Colorful Parrot", "https://images.unsplash.com/photo-1552728089-57bdde30beb3?w=1080&q=80"),
        Wallpaper("Peacock Beauty", "https://images.unsplash.com/photo-1515442261605-65987783cb6a?w=1080&q=80"),
        Wallpaper("Hummingbird", "https://images.unsplash.com/photo-1444464666168-49d633b86797?w=1080&q=80"),
        Wallpaper("Eagle Soaring", "https://images.unsplash.com/photo-1611689102192-1f6e0e52df0a?w=1080&q=80"),
        Wallpaper("Flamingo Pink", "https://images.unsplash.com/photo-1497206365907-f5e630693df0?w=1080&q=80"),
        Wallpaper("Owl Wisdom", "https://images.unsplash.com/photo-1543549790-8b5f4a028cfb?w=1080&q=80")
    )
    
    private val allWallpapers: List<Wallpaper> by lazy {
        (darkWallpapers + lightWallpapers + animalWallpapers + birdWallpapers).shuffled()
    }
}
