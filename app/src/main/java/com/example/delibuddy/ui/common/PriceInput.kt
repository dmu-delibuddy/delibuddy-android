package com.example.delibuddy.ui.common

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import com.example.delibuddy.R

class PriceInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    val editText: EditText

    init {
        LayoutInflater.from(context).inflate(R.layout.view_price_input, this)
        editText = findViewById(R.id.edit_amount)
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.PriceInput, defStyleAttr, 0)
        try {
            editText.hint = attributes.getText(R.styleable.PriceInput_android_hint)
            val maxLength = attributes.getInt(R.styleable.PriceInput_android_maxLength, 7)
            editText.filters = arrayOf(InputFilter.LengthFilter(maxLength))
            editText.id = attributes.getResourceId(R.styleable.PriceInput_amountEditId, NO_ID)
        } finally {
            attributes.recycle()
        }
    }

}
