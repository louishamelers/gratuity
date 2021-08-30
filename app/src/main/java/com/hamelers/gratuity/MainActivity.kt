package com.hamelers.gratuity

import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButtonToggleGroup
import com.hamelers.gratuity.databinding.ActivityMainBinding
import org.w3c.dom.Text
import java.text.NumberFormat
import java.util.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var amount: Int = 0
    private var tipPercentage: Double = 0.0
    private var collapsed = true
    private var lastSelectedPercentageButtonId = 0

    private val textWatcher: TextWatcher = object: TextWatcher {
        private var current: String = ""

        override fun onTextChanged(
            s: CharSequence,
            start: Int,
            before: Int,
            count: Int
        ) {
            if (s.toString() != current) {
                binding.amountEditInput.removeTextChangedListener(this)

                val cleanString: String = s.replace("[^0-9]".toRegex(), "")

                val parsed = cleanString.toDouble()
                amount = parsed.toInt()
                binding.finalAmount.text = formatToCurrency(amount.toDouble())
                calculateTip(amount, tipPercentage, binding.finalTip, binding.finalTotal)

                val formatted = formatToCurrency(parsed)
                current = formatted
                binding.amountEditInput.setText(formatted)
                binding.amountEditInput.setSelection(formatted.length)

                binding.amountEditInput.addTextChangedListener(this)
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(p0: Editable?) {
        }
    }
    private val toggleWatcher: MaterialButtonToggleGroup.OnButtonCheckedListener = object: MaterialButtonToggleGroup.OnButtonCheckedListener {
        override fun onButtonChecked(materialButtonToggleGroup: MaterialButtonToggleGroup, i: Int, b: Boolean) {
            if (!collapsed) {
                lastSelectedPercentageButtonId = i
                toggleMoreOptions()
            }
            when (i) {
                binding.leftButton.id -> tipPercentage = 0.14
                binding.centerButton.id -> tipPercentage = 0.21
                binding.rightButton.id -> tipPercentage = 0.28
            }
            calculateTip(amount, tipPercentage, binding.finalTip, binding.finalTotal)
        }

    }
    private val onMoreClickListener: View.OnClickListener = object: View.OnClickListener{
        override fun onClick(p0: View?) {
            toggleMoreOptions()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.amountEditInput.requestFocus()
        binding.amountEditInput.addTextChangedListener(textWatcher)
        binding.amountEditInput.text = SpannableStringBuilder(formatToCurrency(0.0))

        binding.tipPercentage.check(binding.centerButton.id)
        binding.tipPercentage.isSelectionRequired = true
        binding.tipPercentage.isSingleSelection = true
        binding.tipPercentage.addOnButtonCheckedListener(toggleWatcher)
        binding.tipPercentage.check(binding.centerButton.id)

        binding.moreTips.setOnClickListener(onMoreClickListener)

        setContentView(binding.root)
    }

    private fun formatToCurrency(parsed: Double): String {
        return NumberFormat.getCurrencyInstance().format((parsed / 100))
    }

    private fun calculateTip(amount: Int, tipPercentage: Double, tipAmount: TextView, totalAmount: TextView) {
        tipAmount.text = formatToCurrency(amount*tipPercentage)
        totalAmount.text = formatToCurrency(amount + (amount*tipPercentage))
    }

    private fun toggleMoreOptions() {
        if (collapsed) {
            lastSelectedPercentageButtonId = binding.tipPercentage.checkedButtonId
            binding.tipPercentage.isSelectionRequired = false
            binding.tipPercentage.uncheck(lastSelectedPercentageButtonId)
            binding.moreTips.text = "Less"
        } else {
            binding.tipPercentage.isSelectionRequired = true
            binding.tipPercentage.check(lastSelectedPercentageButtonId)
            binding.moreTips.text = "More"
        }
        collapsed = !collapsed
    }
}