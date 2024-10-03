/*
 * Copyright 2022-2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.codelab.sdclibrary
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.google.android.fhir.datacapture.QuestionnaireFragment
import androidx.fragment.app.commit
import android.util.Log
import ca.uhn.fhir.context.FhirContext // HAPI FHIR context
import org.hl7.fhir.r4.model.Questionnaire
import com.google.android.fhir.datacapture.mapping.ResourceMapper





class MainActivity : AppCompatActivity() {

  var questionnaireJsonString: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Step 2: Configure a QuestionnaireFragment
    questionnaireJsonString = getStringFromAssets("questionnaire.json")


    if (savedInstanceState == null) {
      supportFragmentManager.commit {
        setReorderingAllowed(true)
        add(
          R.id.fragment_container_view,
          QuestionnaireFragment.builder().setQuestionnaire(questionnaireJsonString!!).build()
        )
      }
      supportFragmentManager.setFragmentResultListener(
        QuestionnaireFragment.SUBMIT_REQUEST_KEY,
        this,
      ) { _, _ ->
        submitQuestionnaire()
      }
    }
  }


  private fun submitQuestionnaire() =
    lifecycleScope.launch {

      // Get a questionnaire response
      val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
              as QuestionnaireFragment
      val questionnaireResponse = fragment.getQuestionnaireResponse()

// Print the response to the log
      val jsonParser = FhirContext.forR4().newJsonParser()
      val questionnaireResponseString =
        jsonParser.encodeResourceToString(questionnaireResponse)
      Log.d("response", questionnaireResponseString)

      val questionnaire =
        jsonParser.parseResource(questionnaireJsonString) as Questionnaire
      val bundle = ResourceMapper.extract(questionnaire, questionnaireResponse)
      Log.d("extraction result", jsonParser.encodeResourceToString(bundle))
    }

  private fun getStringFromAssets(fileName: String): String {
    return assets.open(fileName).bufferedReader().use { it.readText() }
  }
}