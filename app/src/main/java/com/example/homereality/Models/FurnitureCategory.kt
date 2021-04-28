package com.example.homereality.Models

import android.os.Parcel
import android.os.Parcelable

data class FurnitureCategory (
        var category: String? = "",
        var iconBlack: String? = "",
        var iconWhite: String? = ""): Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(category)
        parcel.writeString(iconBlack)
        parcel.writeString(iconWhite)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FurnitureCategory> {
        override fun createFromParcel(parcel: Parcel): FurnitureCategory {
            return FurnitureCategory(parcel)
        }

        override fun newArray(size: Int): Array<FurnitureCategory?> {
            return arrayOfNulls(size)
        }
    }
}