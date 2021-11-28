package com.hoc.flowmvi.ui.add

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.hoc.flowmvi.core_ui.navigator.IntentProviders
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddActivity : AppCompatActivity() {
  internal class IntentProvider @Inject constructor() : IntentProviders.Add {
    override fun makeIntent(context: Context): Intent =
      Intent(context, AddActivity::class.java)
  }
}
