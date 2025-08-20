package com.druto.loan.cash.drutocash.entity

class FirstEntity {
    var genesis: Int = 0 // 紧急联系人选择方式
    var jeans: String? = null // 会话ID
    var argument: Argument? = null
    var powdery: Powdery? = null
    var brothel: Brothel? = null
    var abrasive: Abrasive? = null
    val h5MainUrl: String?
        // response.splice.defection
        get() {
            if (argument == null) {
                return null
            }
            return argument!!.drop
        }

    override fun toString(): String {
        return "FirstEntity{" +
                "providing='" + genesis + '\'' +
                ", reek='" + jeans + '\'' +
                ", sincerity=" + argument +
                ", armament=" + powdery +
                ", boneless=" + brothel +
                ", daddy=" + abrasive +
                '}'
    }
}
