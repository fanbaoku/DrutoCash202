package com.cash.ease.money.firebase

class FirebaseInstanceIdEntity(val id: String?, val error: String?) {
    override fun toString(): String {
        return "FirebaseInstanceIdEntity{" +
                "id='" + id + '\'' +
                ", error='" + error + '\'' +
                '}'
    }
}
