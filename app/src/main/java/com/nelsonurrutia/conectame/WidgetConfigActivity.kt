package com.nelsonurrutia.conectame

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** "Which meeting should this button open?" for a big home-screen button that has none yet. */
class WidgetConfigActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        val meetings = MeetingRepository(applicationContext).meetings

        setContent {
            AppTheme {
                Scaffold { padding ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(20.dp)
                            .readableWidth(),
                    ) {
                        Text(
                            stringResource(if (meetings.isEmpty()) R.string.widget_no_meetings else R.string.widget_choose_title),
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.semantics { heading() },
                        )
                        if (meetings.isEmpty()) {
                            Button(
                                onClick = {
                                    startActivity(Intent(this@WidgetConfigActivity, MainActivity::class.java))
                                    finish()
                                },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                            ) { Text(stringResource(R.string.open_app), style = MaterialTheme.typography.titleMedium) }
                        }
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                        ) {
                            items(meetings, key = { it.id }) { meeting ->
                                Button(
                                    onClick = { choose(widgetId, meeting) },
                                    colors = ButtonDefaults.buttonColors(containerColor = meeting.color.color, contentColor = Color.White),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp),
                                ) { Text(meeting.title, style = MaterialTheme.typography.headlineSmall) }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun choose(widgetId: Int, meeting: Meeting) {
        MeetingWidget.assign(applicationContext, widgetId, meeting.id)
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        finish()
    }
}
