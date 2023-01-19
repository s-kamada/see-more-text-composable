package com.example.seemore_text_composable

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontLoader
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import com.example.seemore_text_composable.ui.theme.Typography

@Composable
fun ExpandableLineLimitText(
    modifier: Modifier = Modifier,
    text: String,
    ellipsisText: String = "...",
    showMoreText: String = "もっと見る",
    showLessText: String = "閉じる",
    maxLine: Int = 2,
    lineHeight: TextUnit = 1.55.em,
    onClickMore: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }

    val bodyStyle = SpanStyle(
        color = Color.Gray,
        fontSize = Typography.body1.fontSize
    )
    val suffixStyle = SpanStyle(
        color = Color.Black,
        fontSize = Typography.body1.fontSize,
        textDecoration = TextDecoration.Underline
    )

    BoxWithConstraints(
        modifier = modifier
    ) {

        // 先に文字を全て描画した場合の計算を行い結果に応じて表示の整形を行う。
        val noEllipsedBodyParagraph = MultiParagraph(
            intrinsics = MultiParagraphIntrinsics(
                annotatedString = buildAnnotatedString { withStyle(bodyStyle) { append(text) } },
                style = Typography.body1,
                density = LocalDensity.current,
                resourceLoader = LocalFontLoader.current,
                placeholders = emptyList()
            ),
            width = with(LocalDensity.current) { maxWidth.toPx() }
        )

        val ellipsedBodyParagraph = MultiParagraph(
            intrinsics = noEllipsedBodyParagraph.intrinsics,
            ellipsis = true,
            maxLines = maxLine,
            width = with(LocalDensity.current) { maxWidth.toPx() }
        )

        val clickable: Boolean
        val displayText: AnnotatedString
        if (noEllipsedBodyParagraph.lineCount <= maxLine) {
            // テキストがmaxLineに収まる場合はそのまま表示を行う。
            clickable = false
            displayText = buildAnnotatedString { withStyle(bodyStyle) { append(text) } }
        } else {
            // テキストがmaxLineを超える場合は「もっと見る」「閉じる」の状態に応じてテキストを整形する。
            clickable = true
            displayText = if (isExpanded) {
                buildAnnotatedString {
                    withStyle(bodyStyle) { append(text) }
                    withStyle(suffixStyle) { append(showLessText) }
                }
            } else {
                // ellipsisとshowMoreTextの文字幅の計算。この幅が収まるだけ本文の末尾を取り除く処理のために必要。
                val seeMoreTextWidth = MultiParagraph(
                    intrinsics = MultiParagraphIntrinsics(
                        annotatedString = buildAnnotatedString {
                            withStyle(bodyStyle) { append(ellipsisText) }
                            // 本文の末尾を取り除く計算の際に1文字ずれて「もっと見る」が1文字だけ見切れる可能性があるため、ここで空文字を1つ付けておく事で事前に回避する。
                            // https://github.com/JetBrains/compose-jb/issues/2570
                            withStyle(suffixStyle) { append("$showMoreText　") }
                        },
                        style = Typography.body1,
                        density = LocalDensity.current,
                        resourceLoader = LocalFontLoader.current,
                        placeholders = emptyList()
                    ),
                    width = with(LocalDensity.current) { maxWidth.toPx() }
                ).getBoundingBox("$ellipsisText$showMoreText　".length - 1).right

                // 最終行の下端のY座標
                val maxLineBottomYCoordinate = noEllipsedBodyParagraph.getLineBottom(maxLine - 1)
                // ellipseの左端の座標
                val seeMoreTextStartCoordinateOffset = Offset(x = noEllipsedBodyParagraph.width - seeMoreTextWidth, y = maxLineBottomYCoordinate)
                // 実際に表示できる本文(ellipseやseeMoreText以外)の文字数
                val displayBodyLength = ellipsedBodyParagraph.getOffsetForPosition(seeMoreTextStartCoordinateOffset)
                // maxLineまでの本文
                val textInMaxLine = text.substring(startIndex = 0, endIndex = noEllipsedBodyParagraph.getLineEnd(maxLine - 1, true))

                // 実際に表示する文字は以下の手順で作られる
                // 1. maxLineまでの本文の末尾を数文字取り除く
                // (改行が入っている等の理由で最終行が短くなっている場合はdropCountが負になる。そのままdropしようとするとIllegalArgumentExceptionになる。)
                // 2. 取り覗かれた部分にellipsisを付け足す
                // 3. showMoreText("...もっと見る")を付け足す
                val dropCount = textInMaxLine.length - displayBodyLength
                val body = if (dropCount > 0) {
                    textInMaxLine.dropLast(textInMaxLine.length - displayBodyLength)
                } else {
                    textInMaxLine
                }
                buildAnnotatedString {
                    withStyle(bodyStyle) { append(body) }
                    withStyle(bodyStyle) { append(ellipsisText) }
                    withStyle(suffixStyle) { append(showMoreText) }
                }
            }
        }

        SelectionContainer(
            modifier = Modifier
                .clickable(enabled = clickable) {
                    if (!isExpanded) {
                        onClickMore.invoke()
                    }
                    isExpanded = !isExpanded
                }
                .animateContentSize()
        ) {
            Text(
                text = displayText,
                maxLines = if (isExpanded) Int.MAX_VALUE else maxLine,
                lineHeight = lineHeight
            )
        }

    }
}

@Preview(name = "Small", group = "font scales", showBackground = true, fontScale = 0.85f)
@Preview(name = "Default", group = "font scales", showBackground = true, fontScale = 1f)
@Preview(name = "Large", group = "font scales", showBackground = true, fontScale = 1.15f)
@Preview(name = "Largest", group = "font scales", showBackground = true, fontScale = 1.3f)
@Composable
fun ExpandableLineLimitTextPreview() {
    ExpandableLineLimitText(modifier = Modifier.background(color = Color.White), text = novels.first().openingParagraph)
}
