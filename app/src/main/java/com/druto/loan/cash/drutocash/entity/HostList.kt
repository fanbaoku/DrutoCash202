package com.druto.loan.cash.drutocash.entity

import com.druto.loan.cash.drutocash.DrutoCashApplication
import java.util.Arrays
import java.util.stream.Collectors

class HostList {
    var drutocash: String? = null // 正式环境
    var drutocashbd: String? = null // 正式环境
    var drutocashtz: String? = null // 正式环境
    var drutocashbdtest: String? = null // 测试环境
    var drutocashtztest: String? = null // 测试环境
    var drutocashtest: String? = null // 测试环境
    var whatsapp: String? = null
    var email: String? = null

    val cashEaseHostList: List<String>
        get() {
            if (DrutoCashApplication.country==1){
            val listString = if (DrutoCashApplication.sDebug) {
                drutocashtest
            } else {
                drutocash
            }
            return getHostList(listString)
            }else if (DrutoCashApplication.country==2){
                val listString = if (DrutoCashApplication.sDebug) {
                    drutocashbdtest
                } else {
                    drutocashbd
                }
                return getHostList(listString)
            }
            else{
                val listString = if (DrutoCashApplication.sDebug) {
                    drutocashtztest
                } else {
                    drutocashtz
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
                "drutocash='" + drutocash + '\'' +
                "drutocashbd='" + drutocash + '\'' +
                ", drutocashtest='" + drutocashtest + '\'' +
                "drutocashtz='" + drutocashtz + '\'' +
                ", drutocashtztest='" + drutocashtztest + '\'' +
                ", drutocashbdtest='" + drutocashtest + '\'' +
                ", whatsapp='" + whatsapp + '\'' +
                ", email='" + email + '\'' +
                '}'
    }
}
