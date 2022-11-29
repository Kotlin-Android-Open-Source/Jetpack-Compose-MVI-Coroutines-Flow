package com.hoc.flowmvi.ui.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import arrow.core.getOrElse
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.hoc.flowmvi.domain.model.Email
import com.hoc.flowmvi.domain.model.FirstName
import com.hoc.flowmvi.domain.model.LastName
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.ui.theme.AppTheme

private object UserItemRowDefaults {
  val ImageSize = 72.dp
  val Padding = 8.dp
  val DismissBackgroundColor = Color.Red.copy(alpha = 0.75f)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun UserItemRow(
  item: UserItem,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val dismissState = rememberDismissState(
    confirmStateChange = { dismissValue ->
      if (dismissValue == DismissValue.DismissedToStart) {
        onRemove()
      }
      false
    }
  )

  SwipeToDismiss(
    state = dismissState,
    background = {
      val scale by animateFloatAsState(
        if (dismissState.targetValue == DismissValue.Default) {
          0.75f
        } else {
          1f
        }
      )

      Box(
        Modifier
          .fillMaxSize()
          .background(UserItemRowDefaults.DismissBackgroundColor)
          .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterEnd
      ) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = stringResource(R.string.delete),
          modifier = Modifier.scale(scale),
          tint = Color.White
        )
      }
    },
    directions = setOf(DismissDirection.EndToStart),
    dismissThresholds = { FractionalThreshold(0.25f) },
  ) {
    Row(
      modifier = modifier
        .background(MaterialTheme.colorScheme.background)
        .padding(all = UserItemRowDefaults.Padding),
    ) {
      UserItemAvatar(item)

      Spacer(modifier = Modifier.width(UserItemRowDefaults.Padding))

      Column(
        modifier = Modifier
          .weight(1f)
          .align(Alignment.CenterVertically),
        verticalArrangement = Arrangement.Center,
      ) {
        Text(
          modifier = Modifier.fillMaxWidth(),
          text = item.fullName,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(UserItemRowDefaults.Padding))

        Text(
          modifier = Modifier.fillMaxWidth(),
          text = item.email,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 12.sp,
            color = MaterialTheme.typography.bodySmall.color,
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      if (item.isDeleting) {
        CircularProgressIndicator(
          modifier = Modifier.align(Alignment.CenterVertically),
          strokeWidth = 2.dp,
        )
      }
    }
  }
}

@Composable
private fun UserItemAvatar(item: UserItem) {
  SubcomposeAsyncImage(
    modifier = Modifier
      .requiredWidth(UserItemRowDefaults.ImageSize)
      .requiredHeight(UserItemRowDefaults.ImageSize),
    model = ImageRequest.Builder(LocalContext.current)
      .data(item.avatar)
      .crossfade(true)
      .transformations(CircleCropTransformation())
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

@Preview
@Composable
fun PreviewUserItemRow() {
  AppTheme {
    UserItemRow(
      item = UserItem(
        domain = User(
          id = "",
          email = Email.create("hoc081098@gmail.com")
            .getOrElse { error("") },
          firstName = FirstName.create("Petrus")
            .getOrElse { error("") },
          lastName = LastName.create("Hoc")
            .getOrElse { error("") },
          avatar = ""
        )
      ).copy(isDeleting = true),
      onRemove = {},
    )
  }
}
