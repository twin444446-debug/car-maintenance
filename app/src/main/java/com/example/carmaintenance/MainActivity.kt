
package com.example.carmaintenance

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.graphics.Color

class MainActivity : Activity() {

    private val sections = listOf(
        "Главная",
        "Перечень работ",
        "Регламент",
        "Статистика",
        "Ещё"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSection("Главная")
    }

    private fun showSection(section: String) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 16)
            setBackgroundColor(Color.rgb(248, 249, 252))
        }

        val title = TextView(this).apply {
            text = "Учёт автомобильного ТО"
            textSize = 25f
            setTextColor(Color.rgb(35, 55, 75))
            gravity = Gravity.CENTER
            setPadding(8, 16, 8, 20)
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = section
            textSize = 21f
            setTextColor(Color.rgb(45, 75, 100))
            setPadding(8, 8, 8, 16)
        }
        root.addView(subtitle)

        val content = when (section) {
            "Главная" ->
                "Добавь автомобиль, чтобы начать учёт ТО."
            "Перечень работ" ->
                "Здесь будет история обслуживания и ремонтов."
            "Регламент" ->
                "Здесь будут сроки ТО автомобиля и ГБО."
            "Статистика" ->
                "Здесь будут расходы по автомобилям и периодам."
            else ->
                "Автомобили, шаблоны, запчасти, шины, документы и резервные копии."
        }

        val info = TextView(this).apply {
            text = content
            textSize = 17f
            setTextColor(Color.DKGRAY)
            setPadding(12, 12, 12, 24)
        }
        root.addView(info)

        if (section == "Главная") {
            val addCar = Button(this).apply {
                text = "Добавить автомобиль"
                setOnClickListener {
                    showSection("Автомобили")
                }
            }
            root.addView(addCar)
        }

        val spacer = android.view.View(this)
        root.addView(
            spacer,
            LinearLayout.LayoutParams(1, 0, 1f)
        )

        sections.forEach { name ->
            val button = Button(this).apply {
                text = name
                textSize = 13f
                isAllCaps = false
                setOnClickListener { showSection(name) }
            }
            root.addView(
                button,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }
}
