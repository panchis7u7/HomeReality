package com.example.homereality.Models

data class Furniture(var category: String? = "",
                     var color: String? = "",
                     var details: Map<String?, String?>,
                     var images: List<String?>,
                     var model: String? = "hola",
                     var price: Long?,
                     var rendable: String? = "",
                     var sizes: List<Double?>,
                     var source: String? = "") {
}