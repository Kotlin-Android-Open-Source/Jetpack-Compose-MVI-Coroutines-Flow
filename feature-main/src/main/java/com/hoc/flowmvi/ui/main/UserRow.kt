package com.hoc.flowmvi.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import arrow.core.valueOr
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.hoc.flowmvi.core.unit
import com.hoc.flowmvi.domain.model.User

@Composable
@OptIn(
  ExperimentalMaterialApi::class,
  ExperimentalCoilApi::class,
)
internal fun UserRow(
  item: UserItem,
  modifier: Modifier = Modifier,
  onDelete: (UserItem) -> Unit,
) {
  val imageSize = 72.dp
  val padding = 8.dp
  val itemHeight = imageSize + padding * 2

  val dismissState = rememberDismissState(
    confirmStateChange = { dismissValue ->
      (dismissValue == DismissValue.DismissedToStart).also {
        if (it) {
          onDelete(item)
        }
      }
    }
  )

  LaunchedEffect(
    key1 = item.isDeleting,
    key2 = item.id,
  ) {
    if (item.isDeleting) {
      dismissState.snapTo(DismissValue.DismissedToStart)
    } else {
      dismissState.snapTo(DismissValue.Default)
    }
  }

  SwipeToDismiss(
    state = dismissState,
    background = {
      val scale by animateFloatAsState(
        if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
      )

      Box(
        Modifier
          .fillMaxSize()
          .background(Color.Red.copy(alpha = 0.5f))
          .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterEnd
      ) {
        Icon(
          if (item.isDeleting) Icons.Default.Downloading else Icons.Default.Delete,
          contentDescription = "Delete",
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
        .requiredHeight(itemHeight)
        .background(Color.White)
        .padding(all = padding),
    ) {
      val painter = rememberImagePainter(
        data = item.avatar,
        builder = {
          crossfade(true)
          transformations(CircleCropTransformation())
        },
      )

      Box(
        modifier = Modifier
          .requiredWidth(imageSize)
          .requiredHeight(imageSize),
      ) {
        Image(
          painter = painter,
          contentDescription = "Avatar for ${item.fullName}",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )

        when (painter.state) {
          ImagePainter.State.Empty -> Unit
          is ImagePainter.State.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
          is ImagePainter.State.Success -> Unit
          is ImagePainter.State.Error -> {
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

              Spacer(modifier = Modifier.height(8.dp))

              Text("Error", style = MaterialTheme.typography.caption)
            }
          }
        }.unit
      }

      Spacer(modifier = Modifier.width(padding))

      Column(
        modifier = Modifier
          .weight(1f)
          .align(Alignment.CenterVertically),
        verticalArrangement = Arrangement.Center,
      ) {
        Text(
          item.fullName,
          style = MaterialTheme.typography.subtitle1,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
          item.email,
          style = MaterialTheme.typography.body2.copy(
            fontSize = 12.sp,
            color = MaterialTheme.typography.caption.color,
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun _Place(userItems: List<UserItem>) {
  LazyColumn {
    items(userItems) { item ->
      var unread by remember { mutableStateOf(false) }
      val dismissState = rememberDismissState(
        confirmStateChange = {
          if (it == DismissValue.DismissedToEnd) unread = !unread
          it != DismissValue.DismissedToEnd
        }
      )
      SwipeToDismiss(
        state = dismissState,
        modifier = Modifier.padding(vertical = 4.dp),
        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
        dismissThresholds = { direction ->
          FractionalThreshold(if (direction == DismissDirection.StartToEnd) 0.25f else 0.5f)
        },
        background = {
          val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
          val color by animateColorAsState(
            when (dismissState.targetValue) {
              DismissValue.Default -> Color.LightGray
              DismissValue.DismissedToEnd -> Color.Green
              DismissValue.DismissedToStart -> Color.Red
            }
          )
          val alignment = when (direction) {
            DismissDirection.StartToEnd -> Alignment.CenterStart
            DismissDirection.EndToStart -> Alignment.CenterEnd
          }
          val icon = when (direction) {
            DismissDirection.StartToEnd -> Icons.Default.Done
            DismissDirection.EndToStart -> Icons.Default.Delete
          }
          val scale by animateFloatAsState(
            if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
          )

          Box(
            Modifier
              .fillMaxSize()
              .background(color)
              .padding(horizontal = 20.dp),
            contentAlignment = alignment
          ) {
            Icon(
              icon,
              contentDescription = "Localized description",
              modifier = Modifier.scale(scale)
            )
          }
        },
        dismissContent = {
          Card(
            elevation = animateDpAsState(
              if (dismissState.dismissDirection != null) 4.dp else 0.dp
            ).value
          ) {
            ListItem(
              text = {
                Text(item.toString(), fontWeight = if (unread) FontWeight.Bold else null)
              },
              secondaryText = { Text("Swipe me left or right!") }
            )
          }
        }
      )
    }
  }
}

@Preview(
  widthDp = 300,
)
@Composable
fun UserRowPreview() {
  UserRow(
    item = UserItem(
      User.create(
        id = "123",
        email = "hoc081098@gmail.com",
        firstName = "Hoc",
        lastName = "Petrus ".repeat(10),
        avatar = "test",
      ).valueOr { error("") }
    ),
    onDelete = {}
  )
}
