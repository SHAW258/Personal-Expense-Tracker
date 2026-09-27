package com.example.financetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.financetracker.data.model.PredefinedCategories

@Composable
fun CategoryIcon(
    categoryName: String,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    iconSize: Dp = 22.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp)
) {
    val categoryDef = PredefinedCategories.getCategoryByName(categoryName)
    val color = Color(categoryDef.colorHex)
    val icon = getIconForCategory(categoryDef.iconKey)

    Box(
        modifier = modifier
            .size(size)
            .background(color.copy(alpha = 0.16f), shape = shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = categoryName,
            tint = color,
            modifier = Modifier.size(iconSize)
        )
    }
}

fun getIconForCategory(iconKey: String): ImageVector {
    return when (iconKey) {
        "restaurant" -> Icons.Default.Restaurant
        "home" -> Icons.Default.Home
        "directions_car" -> Icons.Default.DirectionsCar
        "shopping_cart" -> Icons.Default.ShoppingCart
        "bolt" -> Icons.Default.Bolt
        "movie" -> Icons.Default.Movie
        "local_hospital" -> Icons.Default.LocalHospital
        "shopping_bag" -> Icons.Default.ShoppingBag
        "school" -> Icons.Default.School
        "payments" -> Icons.Default.Payments
        "laptop_mac" -> Icons.Default.LaptopMac
        "trending_up" -> Icons.Default.TrendingUp
        "card_giftcard" -> Icons.Default.CardGiftcard
        "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
        else -> Icons.Default.Category
    }
}
