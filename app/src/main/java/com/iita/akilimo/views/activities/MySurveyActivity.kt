package com.iita.akilimo.views.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.androidadvance.androidsurvey.SurveyActivity
import com.iita.akilimo.R
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset


class MySurveyActivity : AppCompatActivity() {
    private val SURVEY_REQUEST = 1337

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_survey)

        val button_survey_example_1 = findViewById<View>(R.id.button_survey_example_1) as Button

        button_survey_example_1.setOnClickListener {
            val i_survey = Intent(
                this@MySurveyActivity,
                SurveyActivity::class.java
            )
            //you have to pass as an extra the json string.
            i_survey.putExtra("json_survey", loadSurveyJson("akilimo_survey.json"))
            startActivityForResult(i_survey, SURVEY_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SURVEY_REQUEST) {
            if (resultCode == RESULT_OK) {
                val answers_json: String? = data?.extras?.getString("answers")

                Log.d("****", "****************** WE HAVE ANSWERS ******************");
                Log.v("ANSWERS JSON", answers_json!!);
                Log.d("****", "*****************************************************");

            }
        }
    }

    private fun loadSurveyJson(filename: String): String? {
        return try {
            val inputStream: InputStream = assets.open(filename)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charset.defaultCharset())
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }
}