package com.hoc.flowmvi.core_ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hoc.flowmvi.ui.theme.AppTheme

@Composable
fun RetryButton(
  errorMessage: String,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = errorMessage,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Button(
      onClick = onRetry,
      contentPadding = PaddingValues(
        vertical = 12.dp,
        horizontal = 24.dp,
      ),
      shape = RoundedCornerShape(6.dp),
    ) {
      Text(text = stringResource(R.string.retry))
    }
  }
}

@Preview(
  showBackground = true,
  widthDp = 360,
  heightDp = 640,
)
@Composable
fun PreviewRetryButton() {
  AppTheme {
    RetryButton(
      errorMessage = "Error message",
      onRetry = {},
    )
  }
}
