package com.druto.loan.cash.firebase

class FirebaseInstanceIdEntity(val id: String?, val error: String?) {
    override fun toString(): String {
        return "FirebaseInstanceIdEntity{" +
                "id='" + id + '\'' +
                ", error='" + error + '\'' +
                '}'
    }
}
