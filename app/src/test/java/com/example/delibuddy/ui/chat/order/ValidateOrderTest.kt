package com.example.delibuddy.ui.chat.order

import com.example.delibuddy.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidateOrderTest {

    @Test
    fun rejectsMissingOrZeroPrice() {
        assertEquals(R.string.my_order_price_required, validateOrder(null, true, null))
        assertEquals(R.string.my_order_price_required, validateOrder(0, true, null))
    }

    @Test
    fun requiresPhotoWhenNoneSaved() {
        assertEquals(R.string.my_order_photo_required, validateOrder(1000, false, null))
        assertEquals(R.string.my_order_photo_required, validateOrder(1000, false, " "))
    }

    @Test
    fun acceptsNewOrSavedPhoto() {
        assertNull(validateOrder(1000, true, null))
        assertNull(validateOrder(1000, false, "https://example.com/cart.jpg"))
    }
}
