package com.kinshuk.budgetmanaget.dataClasses

data class User(
    val Name:String ,
    val Mail:String,
    var lastIn:Long,
    var Budget:Int = 0,
    var spent:Int = 0,
    var totalTransactions: MutableList<Transaction> = mutableListOf()
    )
