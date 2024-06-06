package com.example.searchmapapp

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable

class CustomAdapter(private val context: Context, private val results: List<String>, private val query: String) : BaseAdapter() {

    override fun getCount(): Int {
        return results.size
    }

    override fun getItem(position: Int): Any {
        return results[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false)
        val leftIcon: ImageView = view.findViewById(R.id.leftIcon)
        val resultText: TextView = view.findViewById(R.id.resultText)
        val rightIcon: ImageView = view.findViewById(R.id.rightIcon)

        // Set icons if needed, e.g., leftIcon.setImageResource(R.drawable.some_icon)
        leftIcon.setImageResource(R.drawable.left_icon)
        rightIcon.setImageResource(R.drawable.right_icon)

        // Highlight search keywords
        val result = results[position]
        val spannable = SpannableStringBuilder(result)
        val start = result.indexOf(query, ignoreCase = true)
        if (start >= 0) {
            val end = start + query.length
            spannable.setSpan(
                StyleSpan(android.graphics.Typeface.BOLD),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        resultText.text = spannable

        return view
    }
}
