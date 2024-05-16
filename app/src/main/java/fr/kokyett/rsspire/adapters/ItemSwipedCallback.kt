package fr.kokyett.rsspire.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Build
import android.util.DisplayMetrics
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import fr.kokyett.rsspire.R
import kotlin.math.roundToInt

class ItemSwipedCallback(var context: Context) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
    var paddingDip: Int = 16

    var swipeLeftEnabled = true
    @ColorInt
    var leftColor: Int = DEFAULT_COLOR
    @DrawableRes
    var leftIcon: Int = DEFAULT_ICON
    @ColorInt
    var leftIconColor: Int = DEFAULT_ICON_COLOR

    var swipeRightEnabled = true
    @ColorInt
    var rightColor: Int = DEFAULT_COLOR
    @DrawableRes
    var rightIcon: Int = DEFAULT_ICON
    @ColorInt
    var rightIconColor: Int = DEFAULT_ICON_COLOR

    var onItemSwiped: ((direction: Int, position: Int) -> Unit)? = null
    var onDrawItemSwiped: ((direction: Int, position: Int) -> Unit)? = null


    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX != 0f) {
            val position = viewHolder.adapterPosition
            val direction = if (dX < 0) ItemTouchHelper.LEFT else ItemTouchHelper.RIGHT
            onDrawItemSwiped?.let { it(direction, position) }

            if (dX > 0 && swipeLeftEnabled)
                drawSwipeLeft(canvas, recyclerView, viewHolder, dX)
            else
                drawSwipeRight(canvas, recyclerView, viewHolder, dX)
        } else if (swipeRightEnabled) {
            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun drawSwipeLeft(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float) {
        val itemView = viewHolder.itemView
        val paint = Paint()

        var padding = (paddingDip * (context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
        paint.color = leftColor
        canvas.drawRect(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat(), paint)

        val icon = drawableToBitmap(recyclerView, leftIcon, leftIconColor)
        icon?.let {
            if (dX < padding + icon.width)
                padding = (dX - icon.width).toInt()

            canvas.drawBitmap(
                it,
                itemView.left.toFloat() + padding,
                itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height) / 2,
                paint
            )
        }
        val alpha = MaterialColors.ALPHA_FULL - dX / viewHolder.itemView.width.toFloat()
        viewHolder.itemView.alpha = alpha
        viewHolder.itemView.translationX = dX
    }

    private fun drawSwipeRight(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float) {
        val itemView = viewHolder.itemView
        val paint = Paint()

        var padding = (paddingDip * (context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
        paint.color = rightColor
        canvas.drawRect(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat(), paint)

        val icon = drawableToBitmap(recyclerView, rightIcon, rightIconColor)!!
        icon.let {
            if (-dX < padding + icon.width)
                padding = (-dX - icon.width).toInt()

            canvas.drawBitmap(
                it,
                itemView.right.toFloat() - padding - icon.width,
                itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height) / 2,
                paint
            )
        }
        val alpha = MaterialColors.ALPHA_FULL + dX / viewHolder.itemView.width.toFloat()
        viewHolder.itemView.alpha = alpha
        viewHolder.itemView.translationX = dX
    }

    private fun drawableToBitmap(recyclerView: RecyclerView, @DrawableRes id: Int, @ColorInt color: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(recyclerView.context, id)
        return drawable?.let {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
            } else {
                drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
            }

            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        onItemSwiped?.let { it(direction, position) }
    }

    fun setSwipe(direction: Int, @ColorRes color:Int? = null, @DrawableRes icon: Int? = null, @ColorRes iconColor: Int? = null) {
        when (direction) {
            ItemTouchHelper.RIGHT -> {
                leftColor = color?.let { context.resources.getColor(it, context.theme) } ?: leftColor
                leftIcon = icon ?: leftIcon
                leftIconColor = iconColor?.let { context.resources.getColor(iconColor, context.theme) } ?: leftIconColor
            }

            ItemTouchHelper.LEFT -> {
                rightColor = color?.let { context.resources.getColor(color, context.theme) } ?: rightColor
                rightIcon = icon ?: rightIcon
                rightIconColor = iconColor?.let { context.resources.getColor(iconColor, context.theme) } ?: rightIconColor
            }
        }
    }

    companion object {
        @ColorInt
        private const val DEFAULT_COLOR: Int = Color.BLACK
        @DrawableRes
        private val DEFAULT_ICON: Int = R.drawable.ic_action_swipe_default
        @ColorInt
        private const val DEFAULT_ICON_COLOR: Int = Color.GRAY
    }
}
