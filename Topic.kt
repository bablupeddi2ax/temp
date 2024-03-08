package com.example.grammarous.mcq
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Layout.Alignment
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import com.example.grammarous.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Topic : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var topicList: MutableList<String>
    private lateinit var parentLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic)
        initDb()
        initViews()
    }

    private fun initDb() {
        topicList = mutableListOf()
        databaseReference = FirebaseDatabase.getInstance().getReference("mcq")
        CoroutineScope(Dispatchers.IO).launch {
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapShot in snapshot.children) {
                        val topicName = postSnapShot.key
                        Log.i("topicName", topicName.toString())
                        topicName?.let { topicList.add(it) }
                    }
                    initViews() // Call initViews here once the topicList is populated
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Topic", "Database error: ${error.message}")
                }
            })
        }
    }

    private fun initViews() {
        parentLayout = findViewById(R.id.parentLayout)
        val linearParent = findViewById<LinearLayout>(R.id.linearParent)
        var marginTop = 20 // Initial margin top

        if (topicList.isNotEmpty()) {
            for (topic in topicList) {
                val cardView = CardView(this)
                cardView.cardElevation = 10F
                cardView.radius = 10F
                cardView.setOnClickListener {
                    goToMCq(topic)
                }
                val cardParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    300
                )
                cardParams.setMargins(20, marginTop, 20, 20) // Set dynamic top margin

                val tv = TextView(this)
                tv.text = topic
                tv.textSize = 50F
                tv.setTextColor(Color.MAGENTA)
                tv.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

                // Load font from resources and apply it to TextView
                val customFont = ResourcesCompat.getFont(this, R.font.kgprimarypenmanship2)
                tv.typeface = customFont

                cardView.addView(tv)
                // Set margins for the TextView inside the CardView
                val tvParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                tvParams.setMargins(20, 0, 20, 0) // Set margins for the TextView
                tv.layoutParams = tvParams


                // Add CardView to LinearLayout
                linearParent.addView(cardView, cardParams)

                marginTop += 50 // Increase top margin for the next CardView
            }
        }
    }


    private fun goToMCq(topic:String){
        val intent = Intent(this@Topic,Mcq::class.java)
        intent.putExtra("topic",topic)
        startActivity(intent)
    }

}
