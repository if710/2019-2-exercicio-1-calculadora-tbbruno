package br.ufpe.cin.android.calculadora

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    companion object {
        val CURRENT_NUMBER = "currentNumber"
        val CURRENT_EXPR = "currentExpr"
    }

    var currentNumber: String = ""
    var currentExpr: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Add listeners to all digit buttons
        val digitBtns = arrayOf(btn_0, btn_1, btn_2, btn_3, btn_4, btn_5, btn_6, btn_7, btn_8, btn_9, btn_Dot)
        for (btn in digitBtns) {
            btn.setOnClickListener { digitTapped(it) }
        }

        // Add listeners to all operations buttons
        val opBtns = arrayOf(btn_Add, btn_Subtract, btn_Multiply, btn_Divide, btn_Power, btn_LParen, btn_RParen)
        for (btn in opBtns) {
            btn.setOnClickListener { operationTapped(it) }
        }

        // Add listener to clear button
        btn_Clear.setOnClickListener { clear() }

        // Add listener to equals button
        btn_Equal.setOnClickListener {
            updateExpression()
            try {
                // Tries to evaluate the expression
                val result = eval(currentExpr)

                // Update the interface to clear previous info and display the result
                clear()
                currentNumber = "$result"
                text_calc.setText(currentNumber)
            }
            catch (e: Exception) {
                if (e.message != null)
                    displayMsg(e.message!!)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Save the currentNumber typed and the current
        //   expression to be evaluated, before configuration changes.
        outState.run {
            putString(CURRENT_NUMBER, currentNumber)
            putString(CURRENT_EXPR, currentExpr)
        }

        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        savedInstanceState.run {
            currentNumber = getString(CURRENT_NUMBER).toString()
            currentExpr = getString(CURRENT_EXPR).toString()

            text_calc.setText(currentNumber)
            text_info.setText(currentExpr)
        }
    }

    fun digitTapped(view: View) {
        if (view is Button) {
            val value = view.text

            // Add the last digit typed to the current number
            this.currentNumber = currentNumber + value
            // Update 'text_cal' TextField with the updated current number
            text_calc.setText(currentNumber)
        }
    }

    fun operationTapped(view: View) {
        if (view is Button) {
            val op = view.text as String

            updateExpression(op)
        }
    }

    fun updateExpression(op: String? = null) {
        // Add currentNumber and tapped operation (if any) to the current expression
        this.currentExpr = currentExpr + currentNumber + (op ?: "")
        // Update 'text_info' TextField with the updated expression
        text_info.setText(currentExpr)

        // Clear currentNumber variable and its TextField
        this.currentNumber = ""
        text_calc.setText("")
    }

    fun clear() {
        if (currentNumber != "") {
            // On the first time it is tapped, clears the current number field
            currentNumber = ""
            text_calc.setText("")
        } else {
            // If the current number field is already cleared, it clears the whole expression
            currentExpr = ""
            text_info.setText("")
        }
    }

    fun displayMsg(msg: String) {
        val toast = Toast.makeText(this, msg, Toast.LENGTH_LONG)
        toast.show()
    }

    //Como usar a função:
    // eval("2+2") == 4.0
    // eval("2+3*4") = 14.0
    // eval("(2+3)*4") = 20.0
    //Fonte: https://stackoverflow.com/a/26227947
    fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch: Char = ' '
            fun nextChar() {
                val size = str.length
                ch = if ((++pos < size)) str.get(pos) else (-1).toChar()
            }

            fun eat(charToEat: Char): Boolean {
                while (ch == ' ') nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Caractere inesperado: " + ch)
                return x
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            // | number | functionName factor | factor `^` factor
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'))
                        x += parseTerm() // adição
                    else if (eat('-'))
                        x -= parseTerm() // subtração
                    else
                        return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'))
                        x *= parseFactor() // multiplicação
                    else if (eat('/'))
                        x /= parseFactor() // divisão
                    else
                        return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+')) return parseFactor() // + unário
                if (eat('-')) return -parseFactor() // - unário
                var x: Double
                val startPos = this.pos
                if (eat('(')) { // parênteses
                    x = parseExpression()
                    eat(')')
                } else if ((ch in '0'..'9') || ch == '.') { // números
                    while ((ch in '0'..'9') || ch == '.') nextChar()
                    x = java.lang.Double.parseDouble(str.substring(startPos, this.pos))
                } else if (ch in 'a'..'z') { // funções
                    while (ch in 'a'..'z') nextChar()
                    val func = str.substring(startPos, this.pos)
                    x = parseFactor()
                    if (func == "sqrt")
                        x = Math.sqrt(x)
                    else if (func == "sin")
                        x = Math.sin(Math.toRadians(x))
                    else if (func == "cos")
                        x = Math.cos(Math.toRadians(x))
                    else if (func == "tan")
                        x = Math.tan(Math.toRadians(x))
                    else
                        throw RuntimeException("Função desconhecida: " + func)
                } else {
                    throw RuntimeException("Caractere inesperado: " + ch.toChar())
                }
                if (eat('^')) x = Math.pow(x, parseFactor()) // potência
                return x
            }
        }.parse()
    }
}
