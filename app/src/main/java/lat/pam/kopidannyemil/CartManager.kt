package lat.pam.kopidannyemil


object CartManager {

    const val SHIPPING_FEE = 5000

    private val items = mutableListOf<CartItem>()

    fun getItems(): List<CartItem> = items.toList()

    fun add(product: Product) {
        val existing = items.find { it.product.id == product.id }
        if (existing != null) existing.quantity++ else items.add(CartItem(product, 1))
    }

    fun increase(productId: Int) {
        items.find { it.product.id == productId }?.let { it.quantity++ }
    }

    fun decrease(productId: Int) {
        val item = items.find { it.product.id == productId } ?: return
        if (item.quantity > 1) item.quantity-- else items.remove(item)
    }

    fun remove(productId: Int) {
        items.removeAll { it.product.id == productId }
    }

    fun clear() = items.clear()

    fun isEmpty() = items.isEmpty()

    fun totalQuantity(): Int = items.sumOf { it.quantity }

    fun subtotal(): Int = items.sumOf { it.product.price * it.quantity }

    fun shipping(): Int = if (items.isEmpty()) 0 else SHIPPING_FEE

    fun total(): Int = subtotal() + shipping()
}
