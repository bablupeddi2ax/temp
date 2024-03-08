package com.example.grammarous.mcq

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.get
import com.example.grammarous.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Mcq : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var txtQuestion: TextView
    private lateinit var optionGroup: RadioGroup
    private lateinit var btnNext: Button
    private lateinit var txtTopic:TextView
    private var correctOption: Int = -1
    private var correctCount: Int = 0
    private var currentQuestionIndex: Int = 0
    private lateinit var questionsList: MutableList<Question>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mcq)
        val topic = intent.getStringExtra("topic")
        if(!topic.isNullOrBlank()){
            initViews()
            txtTopic.text = topic
            initClickListeners()
            initDb()
        }



    }
    private fun initClickListeners() {
        btnNext.setOnClickListener {
            if (btnNext.text == "Play Again") {
                resetGame()
                btnNext.text = "Next"
                displayQuestion(currentQuestionIndex)
            } else {
                handleNextButtonClick()
            }
        }
    }
    private fun initDb() {
        questionsList = mutableListOf()
        databaseReference = FirebaseDatabase.getInstance().getReference("mcq/${txtTopic.text}/easy")
        CoroutineScope(Dispatchers.IO).launch {
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapShot in snapshot.children) {
                        val question = postSnapShot.child("question").value.toString()
                        val optionsSnapshot = postSnapShot.child("options")
                        val answer = postSnapShot.child("answer").value.toString().toIntOrNull() ?: -1
                        val options = mutableListOf<String>()
                        for (optionSnapshot in optionsSnapshot.children) {
                            val option = optionSnapshot.getValue(String::class.java) ?: ""
                            options.add(option)
                        }
                        val newQuestion = Question(question, options, answer)
                        questionsList.add(newQuestion)
                    }
                    // Ensure questionsList is populated before accessing it
                    if (questionsList.isNotEmpty()) {
                        displayQuestion(currentQuestionIndex)
                    } else {
                        Log.e("initDb", "No questions found in the database")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Topic", "Database error: ${error.message}")
                }
            })
        }
    }


    private fun displayQuestion(index: Int) {
        if (questionsList.isNotEmpty()) {
            val question = questionsList[index]
            txtQuestion.text = question.question
            optionGroup.removeAllViews()
            for (i in question.options.indices) {
                val radioButton = RadioButton(this@Mcq)
                radioButton.text = question.options[i]
                radioButton.buttonDrawable = null
                radioButton.setBackgroundResource(R.drawable.radio_button_bg)
                radioButton.id = ViewCompat.generateViewId()
                radioButton.textAlignment = ViewGroup.TEXT_ALIGNMENT_CENTER

                val btnParams = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.WRAP_CONTENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                )
                btnParams.setMargins(0, 0, 0, 20)
                radioButton.layoutParams = btnParams
                optionGroup.addView(radioButton)
            }
            correctOption = question.answer
        }
    }

    private fun handleNextButtonClick() {


        if (optionGroup.checkedRadioButtonId != -1) {
            val selectedOptionIndex = optionGroup.indexOfChild(findViewById(optionGroup.checkedRadioButtonId))
            if (selectedOptionIndex == correctOption) {
                correctCount++
            }
            currentQuestionIndex++
            if (currentQuestionIndex < questionsList.size) {
                displayQuestion(currentQuestionIndex)
            } else {
                showResult()
            }
        } else {
            Log.d("Answer", "No option selected!")
        }
    }

    private fun showResult() {
        val resultMessage = "Correct Answers: $correctCount / ${questionsList.size}"
        txtQuestion.text = resultMessage
        optionGroup.removeAllViews()
        btnNext.text = "Play Again"
    }

    private fun resetGame() {
        correctCount = 0
        currentQuestionIndex = 0
    }

    private fun initViews() {
        txtQuestion = findViewById(R.id.txtQuestion)
        optionGroup = findViewById(R.id.optionsGroup)
        txtTopic =findViewById(R.id.txtTopic)
        btnNext = findViewById(R.id.btnNext)
    }
}

data class Question(val question: String, val options: List<String>, val answer: Int)
