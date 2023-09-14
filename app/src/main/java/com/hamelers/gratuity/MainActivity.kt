package com.hamelers.gratuity

import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButtonToggleGroup
import com.hamelers.gratuity.databinding.ActivityMainBinding
import java.text.NumberFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var amount: Int = 0
    private var tipPercentage: Int = 0
    private var collapsed = true
    private var percentages = arrayOf<Int>(0, 0, 0)
    private var definitionCollapsed = true

    private val textWatcher: TextWatcher = object : TextWatcher {
        private var current: String = ""

        override fun onTextChanged(
            s: CharSequence,
            start: Int,
            before: Int,
            count: Int
        ) {
            if (s.toString() != current) {
                binding.amountEditInput.removeTextChangedListener(this)

                val cleanString: String = s.replace("\\D".toRegex(), "")

                val parsed = cleanString.toDouble()
                amount = parsed.toInt()
                calculateTip()

                val formatted = formatToCurrency(amount)
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
    private val toggleWatcher: MaterialButtonToggleGroup.OnButtonCheckedListener =
        MaterialButtonToggleGroup.OnButtonCheckedListener { _, i, _ -> setButtonPercentageValue(i) }

    private val onMoreClickListener: View.OnClickListener =
        View.OnClickListener { toggleMoreOptions() }

    private val onTextTouchListener: View.OnTouchListener =
        View.OnTouchListener { p0, _ ->
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(p0, InputMethodManager.SHOW_IMPLICIT)
            p0.performClick()
            true
        }

    private val onDefinitionTouchListener: View.OnTouchListener = View.OnTouchListener { view, event ->
        if (event.action == MotionEvent.ACTION_DOWN) toggleDefinition()
        view.performClick()
        true
    }

    private val percentagePlusClickListener: View.OnClickListener = View.OnClickListener {
        val selectedPercentage: Int = when (binding.tipPercentage.checkedButtonId) {
            binding.leftButton.id -> 0
            binding.centerButton.id -> 1
            binding.rightButton.id -> 2
            else -> 0
        }
        percentages[selectedPercentage]++
        tipPercentage++
        calculateTip()
        savePercentages()
    }

    private val percentageMinusClickListener: View.OnClickListener = View.OnClickListener {
        val selectedPercentage: Int = when (binding.tipPercentage.checkedButtonId) {
            binding.leftButton.id -> 0
            binding.centerButton.id -> 1
            binding.rightButton.id -> 2
            else -> 0
        }
        percentages[selectedPercentage]--
        tipPercentage--
        calculateTip()
        savePercentages()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.hide()

        loadPercentages()

        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.appTitle.setOnTouchListener(onDefinitionTouchListener)
        binding.definitionContainer.setOnTouchListener(onDefinitionTouchListener)
        binding.definitionContainer.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        binding.amountEditInput.requestFocus()
        binding.amountEditInput.addTextChangedListener(textWatcher)
        binding.amountEditInput.text = SpannableStringBuilder(formatToCurrency(0))
        binding.amountEditInput.setOnTouchListener(onTextTouchListener)

        binding.tipPercentage.check(binding.centerButton.id)
        binding.tipPercentage.isSelectionRequired = true
        binding.tipPercentage.isSingleSelection = true
        binding.tipPercentage.addOnButtonCheckedListener(toggleWatcher)
        binding.tipPercentage.check(binding.centerButton.id)

        binding.moreTips.setOnClickListener(onMoreClickListener)

        binding.percentagePlus.setOnClickListener(percentagePlusClickListener)
        binding.percentageMinus.setOnClickListener(percentageMinusClickListener)



        setContentView(binding.root)
        savePercentages()
    }

    private fun calculateTip() {
        binding.finalAmount.text = formatToCurrency(amount)
        binding.finalTip.text = formatToCurrency(amount * tipPercentage / 100)
        binding.finalTotal.text = formatToCurrency(amount + (amount * tipPercentage / 100))
        binding.specificTip.text = "$tipPercentage%"
    }

    private fun formatToCurrency(parsed: Int): String {
        return NumberFormat.getCurrencyInstance().format((parsed.toDouble() / 100))
    }

    private fun toggleDefinition() {
        TransitionManager.beginDelayedTransition(binding.definitionContainer)

        if (definitionCollapsed) {
            binding.definitionContainer.visibility = View.VISIBLE
//            binding.appTitle.gravity = Gravity.START
        } else {
            binding.definitionContainer.visibility = View.GONE
//            binding.appTitle.gravity = Gravity.CENTER
        }

        definitionCollapsed = !definitionCollapsed
    }

    private fun toggleMoreOptions() {
        if (collapsed) { // Show specific tip editor
            binding.tipPercentage.visibility = View.INVISIBLE
            binding.specificPercentageContainer.alpha = 0f
            binding.specificPercentageContainer.visibility = View.VISIBLE
            binding.specificPercentageContainer.animate()
                .alpha(1f)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setStartDelay(50).duration = 250
            binding.moreTips.text = "done"
        } else { // Hide specific tip editor
            binding.specificPercentageContainer.visibility = View.INVISIBLE
            binding.tipPercentage.alpha = 0f
            binding.tipPercentage.visibility = View.VISIBLE
            binding.tipPercentage.animate()
                .alpha(1f)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setStartDelay(50).duration = 250
            binding.moreTips.text = "edit"
        }
        collapsed = !collapsed
        setButtonPercentageValue(binding.tipPercentage.checkedButtonId)
    }

    private fun setButtonPercentageValue(id: Int) {
        when (id) {
            binding.leftButton.id -> tipPercentage = percentages[0]
            binding.centerButton.id -> tipPercentage = percentages[1]
            binding.rightButton.id -> tipPercentage = percentages[2]
        }
        calculateTip()
    }

    private fun loadPercentages() {
        val sharedPreferences = getPreferences(MODE_PRIVATE)
        val leftButtonPercentage = sharedPreferences.getInt("leftPercentage", 14)
        val centerButtonPercentage = sharedPreferences.getInt("centerPercentage", 21)
        val rightButtonPercentage = sharedPreferences.getInt("rightPercentage", 28)
        percentages[0] = leftButtonPercentage
        percentages[1] = centerButtonPercentage
        percentages[2] = rightButtonPercentage
    }

    private fun savePercentages() {
        binding.leftButton.text = percentages[0].toString() + "%"
        binding.centerButton.text = percentages[1].toString() + "%"
        binding.rightButton.text = percentages[2].toString() + "%"
        savePercentagesToSharedPrefs()
    }

    private fun savePercentagesToSharedPrefs() {
        val sharedPreferences = getPreferences(MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
//        val prefsString = StringBuilder()
//        for (i in percentages) {
//            prefsString.append(i).append(",")
//        }
        editor.putInt("leftPercentage", percentages[0])
        editor.putInt("centerPercentage", percentages[1])
        editor.putInt("rightPercentage", percentages[2])
//        editor.putString("percentages", percentages.toString())
        editor.apply()
    }
}