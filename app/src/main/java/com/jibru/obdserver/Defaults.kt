package com.jibru.obdserver

import androidx.compose.runtime.mutableStateMapOf

object Defaults {
    val defaults = mutableStateMapOf(
        //@formatter:off
        //\r added in rawData
        "ATZ"   to mutableListOf("ELM327 v1.5\r\r>"),
        "ATE0"  to mutableListOf("OK\r\r>"),
        "ATL0"  to mutableListOf("OK\r\r>"),
        "ATS0"  to mutableListOf("OK\r\r>"),
        "ATH0"  to mutableListOf("OK\r\r>"),
        "ATSP0" to mutableListOf("OK\r\r>"),
        "ATST62" to mutableListOf("OK\r\r>"),

        "010C" to mutableListOf("410C0000\r\r>"),
        "0146" to mutableListOf("414633\r\r>"),
        "0146" to mutableListOf("414633\r\r>"),
        "0901" to mutableListOf("NO DATA\r\r>"),
        "0902" to mutableListOf("014\r0: 490201574444\r", "1: 32303733303132\r2:", " 46333532373230\r\r>"),
        //@formatter:on
    )

    val rawData = defaults.map {
        (it.key + "\r") to it.value.map { it.encodeToByteArray() }
    }.toMap()

    val counters = mutableMapOf<String, Int>()

    var echoOn: Boolean = true; private set
    var spacesOn: Boolean = true; private set
    var headersOn: Boolean = true; private set

    fun getResponse(request: ByteArray): List<ByteArray> {
        val requestStr = request.decodeToString()
        when {
            requestStr.startsWith("ATZ") -> {
                echoOn = true
                spacesOn = true
                headersOn = true
            }

            requestStr.startsWith("ATE") -> requestStr.getOrNull(3)?.digitToIntOrNull()?.let { echoOn = it == 1 }
            requestStr.startsWith("ATS") -> requestStr.getOrNull(3)?.digitToIntOrNull()?.let { spacesOn = it == 1 }
            requestStr.startsWith("ATH0") -> requestStr.getOrNull(3)?.digitToIntOrNull()?.let { headersOn = it == 1 }
            else -> Unit
        }
        val counter = counters.getOrPut(requestStr) { 0 } + 1
        counters[requestStr] = counter
        val bytes = rawData[requestStr]?.mapIndexed { index, bytes -> bytes.updateBasedOnFlags(index, requestStr) }
        return bytes ?: listOf("RE:${request.decodeToString()}".encodeToByteArray())
    }

    private fun ByteArray.updateBasedOnFlags(index:Int, requestStr: String): ByteArray {
        var msg = decodeToString()
        if (echoOn) {
            msg = requestStr + msg
        }
//        if (!headersOn) {
//            msg = msg.substringAfter("\r")
//        }
        if (spacesOn) {
            msg = msg.addSpaces()
        }
        return msg.encodeToByteArray()
    }

    private fun String.addSpaces(): String {
        return if (startsWith("41")) {
            partition { Character.isDigit(it) }
                .let { (digits, rest) ->
                    digits.windowed(2, 2).joinToString(" ", postfix = " ") { it } + rest
                }
        } else {
            this
        }
    }
}
