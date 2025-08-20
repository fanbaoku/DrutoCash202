package com.cash.ease.money.cashease.entity

class FirstEntity {
    var providing: Int = 0 // 紧急联系人选择方式
    var reek: String? = null // 会话ID
    var sincerity: Sincerity? = null
    var armament: Armament? = null
    var boneless: Boneless? = null
    var daddy: Daddy? = null
    val h5MainUrl: String?
        // response.splice.defection
        get() {
            if (sincerity == null) {
                return null
            }
            return sincerity!!.interview
        }

    override fun toString(): String {
        return "FirstEntity{" +
                "providing='" + providing + '\'' +
                ", reek='" + reek + '\'' +
                ", sincerity=" + sincerity +
                ", armament=" + armament +
                ", boneless=" + boneless +
                ", daddy=" + daddy +
                '}'
    }
}
