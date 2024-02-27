package com.example.womensafety.model

data class Users(
    val name : String ?= null,
    val email : String ?= null,
    val phone : String ?= null,
    val address : String ?= null,
    val uid : String ?= null,
    val userImg : String ?= null,
    val location : String ?= null,
    val contactsList : HashMap<String, Contacts> ?= null
)