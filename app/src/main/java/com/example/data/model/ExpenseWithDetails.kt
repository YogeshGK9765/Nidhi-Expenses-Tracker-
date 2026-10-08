package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.PaymentMethodEntity
import com.example.data.local.entity.SubcategoryEntity

data class ExpenseWithDetails(
    @Embedded
    val expense: ExpenseEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "categoryId"
    )
    val category: CategoryEntity?,

    @Relation(
        parentColumn = "subcategoryId",
        entityColumn = "subcategoryId"
    )
    val subcategory: SubcategoryEntity?,

    @Relation(
        parentColumn = "accountId",
        entityColumn = "accountId"
    )
    val account: AccountEntity?,

    @Relation(
        parentColumn = "paymentMethodId",
        entityColumn = "paymentMethodId"
    )
    val paymentMethod: PaymentMethodEntity?
) {
    val displayTitle: String
        get() = when {
            subcategory != null && subcategory.name.isNotBlank() -> subcategory.name
            expense.title.isNotBlank() -> expense.title
            category != null -> category.name
            else -> "Expense"
        }

    val categoryName: String
        get() = category?.name ?: "Other"

    val accountName: String
        get() = account?.name ?: "SBI"

    val paymentMethodName: String
        get() = paymentMethod?.name ?: "UPI"
}
