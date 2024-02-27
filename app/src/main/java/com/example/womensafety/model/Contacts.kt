package com.example.womensafety.model


data class Contacts(
    val contactKey: String ?= null,
    val contactImg: String ?= null,
    val contactName: String ?= null,
    val contactNumber: String ?= null,
    val contactRelation: String ?= null,
    var selected : Boolean ?= null,
    var originalPosition: Int = -1
)

