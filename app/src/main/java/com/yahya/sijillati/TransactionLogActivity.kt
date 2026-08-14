package com.yahya.sijillati

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView

class TransactionLogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val manager = TransactionManager(this)
        val logs = manager.getActivityLog()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val title = TextView(this).apply {
            text = "سجل العمليات"
            textSize = 22f
            setTextColor(0xFF1A237E.toInt())
            setPadding(0, 0, 0, 24)
        }
        container.addView(title)

        if (logs.isEmpty()) {
            container.addView(TextView(this).apply {
                text = "لا يوجد سجل عمليات بعد"
                textSize = 16f
                setPadding(0, 40, 0, 0)
            })
        } else {
            logs.forEach { entry ->
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(24, 20, 24, 20)
                    setBackgroundColor(0xFFFFFFFF.toInt())
                    val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    lp.bottomMargin = 12
                    layoutParams = lp
                }
                card.addView(TextView(this).apply {
                    text = "${entry.action}: ${entry.details}"
                    textSize = 15f
                    setTextColor(0xFF000000.toInt())
                })
                card.addView(TextView(this).apply {
                    text = entry.timestamp
                    textSize = 12f
                    setTextColor(0xFF888888.toInt())
                    setPadding(0, 8, 0, 0)
                })
                container.addView(card)
            }
        }

        val scroll = NestedScrollView(this)
        scroll.addView(container)
        setContentView(scroll)
    }
}
