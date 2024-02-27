package com.example.womensafety.model

import android.net.Uri

data class ContactModel(
    val name : String ?= null,
    val number : String?= null,
    val image : String ?= null,
    var selected : Boolean ?= null
)
