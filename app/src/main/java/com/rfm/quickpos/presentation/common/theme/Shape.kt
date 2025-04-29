package com.rfm.quickpos.presentation.common.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material 3 shape system with RFM customizations
val RfmShapes = Shapes(
    // Small components: chips, buttons, text fields
    small = RoundedCornerShape(4.dp),

    // Medium components: cards, dialogs, small sheets
    medium = RoundedCornerShape(8.dp),

    // Large components: navigation drawers, sheets
    large = RoundedCornerShape(12.dp),

    // Extra large components (custom extension): full-screen sheets, modal bottoms
    extraLarge = RoundedCornerShape(24.dp)
)

// Specific shapes for POS components
val BottomSheetShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

val PaymentMethodCardShape = RoundedCornerShape(12.dp)

val ProductCardShape = RoundedCornerShape(8.dp)

val DiscountTagShape = RoundedCornerShape(4.dp)

val TextFieldShape = RoundedCornerShape(8.dp)

val ButtonShape = RoundedCornerShape(8.dp)

val ModalShape = RoundedCornerShape(16.dp)

val CircularShape = RoundedCornerShape(percent = 50)