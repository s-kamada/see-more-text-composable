package com.example.seemore_text_composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seemore_text_composable.ui.theme.Typography

@Composable
fun NovelItem(
    modifier: Modifier = Modifier,
    novel: Novel
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = novel.title,
            style = Typography.subtitle1
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = novel.authorName,
            style = Typography.subtitle1
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExpandableLineLimitText(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = Color.Black)
                .padding(8.dp),
            text = novel.openingParagraph,
        )
    }
}

@Preview
@Composable
fun NovelItemPreview() {
    NovelItem(
        modifier = Modifier.fillMaxWidth().background(color = Color.White),
        novel = novels.first()
    )
}
