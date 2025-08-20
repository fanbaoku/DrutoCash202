package com.cash.ease.money.cashease.entity

class ImageResult {
    var pursuit: String? = null
    var interview: String? = null
    var torch: String? = null
    var ambiguous: String? = null
    var image: String? = null
    var wreath: Int = 0

    override fun toString(): String {
        return "ImageResult{" +
                "pursuit='" + pursuit + '\'' +
                ", interview='" + interview + '\'' +
                ", torch='" + torch + '\'' +
                ", ambiguous='" + ambiguous + '\'' +
                ", image='" + image + '\'' +
                ", wreath=" + wreath +
                '}'
    }
}
