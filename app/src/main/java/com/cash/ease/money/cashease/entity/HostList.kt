package com.cash.ease.money.cashease.entity

import com.cash.ease.money.cashease.CashEaseApplication
import java.util.Arrays
import java.util.stream.Collectors

class HostList {
    var cashease: String? = null // 正式环境
    var casheasebda: String? = null // 正式环境
    var casheasetz: String? = null // 正式环境
    var casheasebdatest: String? = null // 测试环境
    var casheasetztest: String? = null // 测试环境
    var casheasetest: String? = null // 测试环境
    var whatsapp: String? = null
    var email: String? = null

    val cashEaseHostList: List<String>
        get() {
            if (CashEaseApplication.country==1){
            val listString = if (CashEaseApplication.sDebug) {
                casheasetest
            } else {
                cashease
            }
            return getHostList(listString)
            }else if (CashEaseApplication.country==2){
                val listString = if (CashEaseApplication.sDebug) {
                    casheasebdatest
                } else {
                    casheasebda
                }
                return getHostList(listString)
            }
            else{
                val listString = if (CashEaseApplication.sDebug) {
                    casheasetztest
                } else {
                    casheasetz
                }
                return getHostList(listString)
            }
        }

    private fun getHostList(hostListString: String?): List<String> {
        val list: List<String> = ArrayList()
        if (hostListString == null) {
            return list
        }
        val hostArray =
            hostListString.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return Arrays.stream(hostArray).collect(Collectors.toUnmodifiableList())
    }

    override fun toString(): String {
        return "HostList{" +
                "cashease='" + cashease + '\'' +
                "casheasebda='" + cashease + '\'' +
                ", casheasetest='" + casheasetest + '\'' +
                "casheasetz='" + casheasetz + '\'' +
                ", casheasetztest='" + casheasetztest + '\'' +
                ", casheasebdatest='" + casheasetest + '\'' +
                ", whatsapp='" + whatsapp + '\'' +
                ", email='" + email + '\'' +
                '}'
    }
}
