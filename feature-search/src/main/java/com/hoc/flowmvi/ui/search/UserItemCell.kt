package com.hoc.flowmvi.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

@Composable
internal fun UserItemCell(
  userItem: UserItem,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .wrapContentHeight(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    UserItemAvatar(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f),
      item = userItem,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      modifier = Modifier
        .fillMaxWidth(),
      text = userItem.fullName,
      style = MaterialTheme.typography.titleMedium,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      modifier = Modifier
        .fillMaxWidth(),
      text = userItem.email,
      style = MaterialTheme.typography
        .bodySmall
        .copy(fontSize = 13.sp),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )

    Spacer(modifier = Modifier.height(8.dp))
  }
}

@Composable
private fun UserItemAvatar(
  item: UserItem,
  modifier: Modifier = Modifier,
) {
  SubcomposeAsyncImage(
    modifier = modifier,
    model = ImageRequest.Builder(LocalContext.current)
      .data(item.avatar)
      .crossfade(true)
      .placeholder(R.drawable.ic_baseline_person_24)
      .build(),
    contentDescription = "Avatar of ${item.fullName}",
    error = {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Icon(
          imageVector = Icons.Filled.ErrorOutline,
          contentDescription = "Error",
          modifier = Modifier.size(width = 20.dp, height = 20.dp)
        )
      }
    }
  )
}
