package com.yahya.sijillati

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import java.util.Locale

class ReportsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val manager = TransactionManager(this)
        val txns = manager.getAllTransactions(false)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        container.addView(TextView(this).apply { text = "التحليلات"; textSize = 22f; setPadding(0, 0, 0, 32) })

        listOf("د.ع", "$").forEach { currency ->
            val filtered = txns.filter { it.currency == currency }
            if (filtered.isEmpty()) return@forEach

            container.addView(TextView(this).apply { text = "عملة: $currency"; textSize = 18f; setPadding(0, 24, 0, 12) })

            val byType = filtered.groupBy { it.type }.mapValues { it.value.sumOf { t -> t.amount } }
            val maxVal = byType.values.maxOrNull() ?: 1.0

            byType.forEach { (type, total) ->
                val color = when (type) {
                    "دخل" -> Color.parseColor("#2E7D32")
                    "مصروف" -> Color.parseColor("#D32F2F")
                    "إقراض" -> Color.parseColor("#1976D2")
                    else -> Color.parseColor("#9C27B0")
                }
                container.addView(TextView(this).apply {
                    text = String.format(Locale.US, "%s: %,.0f %s", type, total, currency)
                    textSize = 14f
                    setPadding(0, 8, 0, 4)
                })
                val barRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                val barWidth = ((total / maxVal) * 100).toInt().coerceIn(2, 100)
                barRow.addView(android.view.View(this).apply {
                    setBackgroundColor(color)
                    layoutParams = LinearLayout.LayoutParams(0, 40, barWidth.toFloat())
                })
                barRow.addView(android.view.View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 40, (100 - barWidth).toFloat())
                })
                container.addView(barRow)
            }
        }

        if (txns.isEmpty()) {
            container.addView(TextView(this).apply { text = "لا توجد بيانات كافية للتحليل بعد"; textSize = 16f })
        }

        val scroll = NestedScrollView(this)
        scroll.addView(container)
        setContentView(scroll)
    }
}
