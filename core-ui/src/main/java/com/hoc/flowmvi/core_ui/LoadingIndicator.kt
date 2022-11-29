package com.hoc.flowmvi.core_ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hoc.flowmvi.ui.theme.AppTheme

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    CircularProgressIndicator()
  }
}

@Preview(
  showBackground = true,
  widthDp = 360,
  heightDp = 640,
)
@Composable
fun PreviewLoadingIndicator() {
  AppTheme {
    LoadingIndicator(
      modifier = Modifier.fillMaxSize(),
    )
  }
}
