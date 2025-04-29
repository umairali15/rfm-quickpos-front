// app/src/main/java/com/rfm/quickpos/presentation/common/components/RfmDiscountTag.kt
package com.rfm.quickpos.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.theme.DiscountTagShape
import com.rfm.quickpos.presentation.common.theme.DiscountTagStyle
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.posColors

/**
 * Discount tag component for product cards
 */
@Composable
fun RfmDiscountTag(
    discountPercentage: Int,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(DiscountTagShape)
            .background(MaterialTheme.posColors.discount)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "-$discountPercentage%",
            style = DiscountTagStyle,
            color = MaterialTheme.posColors.onDiscount,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
fun RfmDiscountTagPreview() {
    RFMQuickPOSTheme {
        Surface {
            RfmDiscountTag(
                discountPercentage = 20,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}