package fr.kokyett.rsspire.views

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet

class InstantAutoComplete : androidx.appcompat.widget.AppCompatAutoCompleteTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet?, style: Int) : super(context, attributeSet, style)

    override fun enoughToFilter(): Boolean {
        return true
    }

    override fun onFocusChanged(
        focused: Boolean, direction: Int,
        previouslyFocusedRect: Rect?
    ) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (focused && filter != null) {
            performFiltering(text, 0)
        }
    }
}