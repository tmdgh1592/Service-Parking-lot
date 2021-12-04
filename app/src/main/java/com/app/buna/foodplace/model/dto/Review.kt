package com.app.buna.foodplace.model.dto

import com.google.gson.annotations.SerializedName

data class Review(
    @SerializedName("id") var id: Int,
    @SerializedName("nickname") var name: String,
    @SerializedName("rate") var rate: Short,
    @SerializedName("created") var created: Long,
    @SerializedName("review") var review: String,
    @SerializedName("parkcode") var parkcode: String
)
