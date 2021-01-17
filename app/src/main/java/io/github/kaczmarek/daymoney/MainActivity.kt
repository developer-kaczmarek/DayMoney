package io.github.kaczmarek.daymoney

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import java.util.*
import java.util.Calendar.DAY_OF_MONTH


class MainActivity : AppCompatActivity() {

    lateinit var tvTotalAmount: TextView
    lateinit var etMoneyOnDay: AppCompatEditText
    lateinit var clContainer: ConstraintLayout

    private val sharedPrefs by lazy {
        getSharedPreferences(DAY_MONEY_PREFERENCES, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clContainer = findViewById(R.id.clContainer)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        etMoneyOnDay = findViewById(R.id.etMoneyOnDay)

        val pbDaysInMonth = findViewById<ProgressBar>(R.id.pbDaysInMonth)
        val btnRecalculate = findViewById<AppCompatButton>(R.id.btnRecalculate)
        val swSelectCountDay = findViewById<SwitchCompat>(R.id.swSelectCountDay)

        var considerCurrentDay = sharedPrefs.getBoolean(KEY_ACCOUNTING_CURRENT_DAY, false)
        etMoneyOnDay.setText(sharedPrefs.getString(KEY_MONEY_ON_DAY, getString(R.string.money_on_day_default)))

        with(pbDaysInMonth) {
            max = Calendar.getInstance().getActualMaximum(DAY_OF_MONTH)
            progress = Calendar.getInstance().get(DAY_OF_MONTH)
        }

        with(swSelectCountDay) {
            isChecked = considerCurrentDay
            setOnCheckedChangeListener { _, isChecked ->
                considerCurrentDay = isChecked
                sharedPrefs.edit().putBoolean(KEY_ACCOUNTING_CURRENT_DAY, considerCurrentDay)
                    .apply()
                calculateMoneyOnMonth(
                    etMoneyOnDay.text.toString().toDoubleOrNull(),
                    getCountDays(considerCurrentDay),
                    isSaveValue = true
                )
            }
        }

        btnRecalculate.setOnClickListener {
            calculateMoneyOnMonth(
                etMoneyOnDay.text.toString().toDoubleOrNull(),
                getCountDays(considerCurrentDay),
                isSaveValue = true
            )
        }

        calculateMoneyOnMonth(
            etMoneyOnDay.text.toString().toDoubleOrNull(),
            getCountDays(considerCurrentDay)
        )
    }

    private fun calculateMoneyOnMonth(
        moneyOnDay: Double?,
        countDays: Int,
        isSaveValue: Boolean = false
    ) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(etMoneyOnDay.windowToken, 0)
        etMoneyOnDay.clearFocus()
        when {
            moneyOnDay == null -> Snackbar.make(
                clContainer,
                getString(R.string.error_money_on_day_is_null_or_empty),
                Snackbar.LENGTH_LONG
            ).show()
            moneyOnDay > 10000.0 -> Snackbar.make(
                clContainer,
                getString(R.string.more_money_on_day_description),
                Snackbar.LENGTH_LONG
            ).show()
            else -> {
                if (isSaveValue) sharedPrefs.edit()
                    .putString(KEY_MONEY_ON_DAY, moneyOnDay.toString()).apply()
                tvTotalAmount.text =
                    getString(
                        R.string.total_amount_with_currency_sign,
                        (moneyOnDay * countDays).toString()
                    )
            }
        }
    }

    private fun getCountDays(considerCurrentDay: Boolean): Int {
        val calendar = Calendar.getInstance()
        var dayOfMonth = calendar.get(DAY_OF_MONTH)
        if (considerCurrentDay) dayOfMonth -= 1
        val daysInMonth = calendar.getActualMaximum(DAY_OF_MONTH)
        return daysInMonth - dayOfMonth
    }
}