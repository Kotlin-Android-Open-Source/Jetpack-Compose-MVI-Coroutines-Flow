package com.hoc.flowmvi.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import timber.log.Timber

@Composable
internal fun UsersList(
  isRefreshing: Boolean,
  userItems: List<UserItem>,
  onRefresh: () -> Unit,
  onRemove: (UserItem) -> Unit,
  modifier: Modifier = Modifier
) {
  val lastIndex = userItems.lastIndex

  SwipeRefresh(
    state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
    onRefresh = onRefresh,
  ) {
    LazyColumn(
      modifier = modifier.fillMaxSize(),
    ) {
      itemsIndexed(
        userItems,
        key = { _, item -> item.id },
      ) { index, item ->

        UserRow(
          item = item,
          modifier = Modifier
            .fillParentMaxWidth(),
          onDelete = {
            Timber.d("Remove user $item")
            onRemove(item)
          }
        )

        if (index < lastIndex) {
          Divider(
            modifier = Modifier.padding(horizontal = 8.dp),
            thickness = 0.7.dp,
          )
        }
      }
    }
  }
}
