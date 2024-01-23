package com.kinshuk.budgetmanaget.dataClasses

data class Transaction(
    val Date:String,
    val Amount:Int,
    val Type:Boolean,  // 1-> debited , 2->credited
    val Note:String = ""
    )
