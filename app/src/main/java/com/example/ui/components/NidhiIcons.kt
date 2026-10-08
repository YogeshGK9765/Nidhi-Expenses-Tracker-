package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

object NidhiIcons {

    fun getCategoryIcon(nameOrIcon: String): ImageVector {
        return when (nameOrIcon.lowercase()) {
            "food", "restaurant", "canteen", "breakfast", "lunch", "dinner", "snacks", "tea/coffee" -> Icons.Default.Restaurant
            "travel", "bus", "train", "auto", "cab", "fuel", "parking" -> Icons.Default.DirectionsBus
            "education", "college", "books", "stationery", "course", "exam", "project" -> Icons.Default.School
            "shopping", "clothes", "shoes", "electronics", "accessories" -> Icons.Default.ShoppingBag
            "personal", "grooming", "entertainment", "hobbies" -> Icons.Default.Person
            "bills", "mobile", "internet", "electricity", "subscription" -> Icons.Default.ReceiptLong
            else -> Icons.Default.Category
        }
    }

    fun getAccountIcon(accountName: String, type: String = ""): ImageVector {
        return when {
            accountName.equals("Cash", ignoreCase = true) || type.equals("Cash", ignoreCase = true) -> Icons.Default.Payments
            accountName.contains("Wallet", ignoreCase = true) || type.contains("Wallet", ignoreCase = true) -> Icons.Default.AccountBalanceWallet
            else -> Icons.Default.AccountBalance
        }
    }

    fun getPaymentMethodIcon(methodName: String): ImageVector {
        return when (methodName.lowercase()) {
            "upi" -> Icons.Default.QrCode
            "cash" -> Icons.Default.Payments
            "debit card", "credit card", "card" -> Icons.Default.CreditCard
            "bank transfer" -> Icons.Default.AccountBalance
            else -> Icons.Default.AccountBalanceWallet
        }
    }
}
