package lat.pam.kopidannyemil

data class Product(
    val id: Int,
    val name: String,
    val price: Int,
    val category: String, // "Kopi", "Non-Kopi", atau "Snack"
    val imageRes: Int
)
