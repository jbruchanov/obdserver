package com.jibru.obdserver

import org.apache.commons.lang.StringEscapeUtils
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun ffs() {
        val test = "41545a0d0d0d454c4d3332372076312e350d0d3e"
        val bytes = test.windowed(2, 2)
            .joinToString("") { it.hexToStr() }
            .let { StringEscapeUtils.escapeJava(it) }

        println(bytes)
    }

    @Test
    fun convert() {
        val text = """
            2023-11-25 12:37:49.408 > 41545a0d str:'ATZ'
            2023-11-25 12:37:50.410 < 41545a0d0d0d454c4d3332372076312e350d0d3e, str:'ATZELM327 v1.5>'
            2023-11-25 12:37:56.316 > 41545350300d str:'ATSP0'
            2023-11-25 12:37:56.435 < 41545350300d4f4b0d0d3e, str:'ATSP0OK>'
            2023-11-25 12:38:02.825 > 41544c300d str:'ATL0'
            2023-11-25 12:38:02.961 < 41544c300d4f4b0d0d3e, str:'ATL0OK>'
            2023-11-25 12:38:14.010 > 303134360d str:'0146'
            2023-11-25 12:38:14.302 < 303134360d534541524348494e472e2e2e0d, str:'0146SEARCHING...'
            2023-11-25 12:38:21.832 < 554e41424c4520544f20434f4e4e4543540d0d3e, str:'UNABLE TO CONNECT>'
            2023-11-25 12:38:23.634 > 415445300d str:'ATE0'
            2023-11-25 12:38:23.750 < 415445300d4f4b0d0d3e, str:'ATE0OK>'
            2023-11-25 12:38:26.368 > 303134360d str:'0146'
            2023-11-25 12:38:26.494 < 53, str:'S'
            2023-11-25 12:38:26.583 < 4541524348494e472e2e2e0d, str:'EARCHING...'
            2023-11-25 12:38:27.534 < 3431203436203238200d, str:'41 46 28 '
            2023-11-25 12:38:27.758 < 0d3e, str:'>'
            2023-11-25 12:38:31.764 > 41544c300d str:'ATL0'
            2023-11-25 12:38:31.895 < 4f4b0d0d3e, str:'OK>'
            2023-11-25 12:38:33.084 > 415453300d str:'ATS0'
            2023-11-25 12:38:33.290 < 4f4b0d0d3e, str:'OK>'
            2023-11-25 12:38:33.855 > 303134360d str:'0146'
            2023-11-25 12:38:34.010 < 3431343633330d, str:'414633'
            2023-11-25 12:38:34.099 < 0d3e, str:'>'
            2023-11-25 12:38:38.841 > 303130300d str:'0100'
            2023-11-25 12:38:38.914 < 3431303039383342413031330d, str:'4100983BA013'
            2023-11-25 12:38:39.004 < 0d3e, str:'>'
            2023-11-25 12:38:40.212 > 303930310d str:'0901'
            2023-11-25 12:38:40.759 < 4e4f20444154410d0d3e, str:'NO DATA>'
            2023-11-25 12:38:42.074 > 303930320d str:'0902'
            2023-11-25 12:38:42.247 < 3031340d303a203439303230313537343434340d, str:'0140: 490201574444'
            2023-11-25 12:38:42.251 < 313a2033323330333733333330333133320d323a, str:'1: 323037333031322:'
            2023-11-25 12:38:42.288 < 2034363333333533323337333233300d0d3e, str:' 46333532373230>'
            2023-11-25 12:39:21.466 > 41545a0d str:'ATZ'
            2023-11-25 12:39:22.295 < 0d0d454c4d, str:'ELM'
            2023-11-25 12:39:22.340 < 3332372076312e350d0d3e, str:'327 v1.5>'
            2023-11-25 12:39:35.407 > 415453300d str:'ATS0'
            2023-11-25 12:39:35.526 < 415453300d4f4b0d0d3e, str:'ATS0OK>'
            2023-11-25 12:39:36.776 > 41544c300d str:'ATL0'
            2023-11-25 12:39:36.923 < 41544c300d4f4b0d0d3e, str:'ATL0OK>'
            2023-11-25 12:39:37.919 > 4154535436320d str:'ATST62'
            2023-11-25 12:39:38.135 < 4154535436320d4f4b0d0d3e, str:'ATST62OK>'
            2023-11-25 12:39:40.099 > 41545350300d str:'ATSP0'
            2023-11-25 12:39:40.295 < 41545350300d4f4b0d0d3e, str:'ATSP0OK>'
            2023-11-25 12:39:41.757 > 303130430d str:'010C'
            2023-11-25 12:39:41.871 < 30, str:'0'
            2023-11-25 12:39:41.963 < 3130430d534541524348494e472e2e2e0d, str:'10CSEARCHING...'
            2023-11-25 12:39:49.572 < 554e41424c4520544f20434f4e4e4543540d0d3e, str:'UNABLE TO CONNECT>'
            2023-11-25 12:39:50.439 > 303130430d str:'010C'
            2023-11-25 12:39:50.540 < 30, str:'0'
            2023-11-25 12:39:50.603 < 3130430d534541524348494e472e2e2e0d, str:'10CSEARCHING...'
            2023-11-25 12:39:51.640 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:39:51.725 < 0d3e, str:'>'
            2023-11-25 12:39:52.285 > 303130430d str:'010C'
            2023-11-25 12:39:52.627 < 303130430d34313043303030300d0d3e, str:'010C410C0000>'
            2023-11-25 12:40:00.605 > 415445300d str:'ATE0'
            2023-11-25 12:40:00.864 < 415445300d4f4b0d0d3e, str:'ATE0OK>'
            2023-11-25 12:40:01.606 > 303130430d str:'010C'
            2023-11-25 12:40:02.123 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:40:02.165 < 0d3e, str:'>'
            2023-11-25 12:40:03.531 > 303130430d str:'010C'
            2023-11-25 12:40:03.698 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:40:03.785 < 0d3e, str:'>'
            2023-11-25 12:40:08.680 > 303130430d str:'010C'
            2023-11-25 12:40:08.783 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:40:08.870 < 0d3e, str:'>'
            2023-11-25 12:40:10.454 > 303130430d str:'010C'
            2023-11-25 12:40:10.671 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:40:10.762 < 0d3e, str:'>'
            2023-11-25 12:40:15.545 > 303130430d str:'010C'
            2023-11-25 12:40:15.801 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:40:15.846 < 0d3e, str:'>'
            2023-11-25 12:40:18.419 > 303134360d str:'0146'
            2023-11-25 12:40:18.595 < 3431343633330d, str:'414633'
            2023-11-25 12:40:18.774 < 0d3e, str:'>'
            2023-11-25 12:40:20.197 > 303130430d str:'010C'
            2023-11-25 12:40:20.434 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:40:20.479 < 0d3e, str:'>'
            2023-11-25 12:40:21.960 > 41544c300d str:'ATL0'
            2023-11-25 12:40:22.283 < 4f4b0d0d3e, str:'OK>'
            2023-11-25 12:40:23.414 > 303130430d str:'010C'
            2023-11-25 12:40:23.494 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:40:23.585 < 0d3e, str:'>'
            2023-11-25 12:40:24.820 > 303130430d str:'010C'
            2023-11-25 12:40:24.982 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:40:25.115 < 0d3e, str:'>'
            2023-11-25 12:40:25.684 > 303134360d str:'0146'
            2023-11-25 12:40:26.015 < 3431343633330d, str:'414633'
            2023-11-25 12:40:26.062 < 0d3e, str:'>'
            2023-11-25 12:40:27.440 > 41544c300d str:'ATL0'
            2023-11-25 12:40:27.639 < 4f4b0d0d3e, str:'OK>'
            2023-11-25 12:40:27.947 > 415453300d str:'ATS0'
            2023-11-25 12:40:28.042 < 4f4b0d0d3e, str:'OK>'
            2023-11-25 12:40:30.846 > 41545350300d str:'ATSP0'
            2023-11-25 12:40:31.010 < 4f4b0d0d3e, str:'OK>'
            2023-11-25 12:40:31.549 > 303130430d str:'010C'
            2023-11-25 12:40:31.687 < 53, str:'S'
            2023-11-25 12:40:31.820 < 4541524348494e472e2e2e0d, str:'EARCHING...'
            2023-11-25 12:40:32.768 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:40:32.856 < 0d3e, str:'>'
            2023-11-25 12:40:33.728 > 303130430d str:'010C'
            2023-11-25 12:40:33.982 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:40:34.162 < 0d3e, str:'>'
            2023-11-25 12:40:34.897 > 303130430d str:'010C'
            2023-11-25 12:40:34.972 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:40:35.060 < 0d3e, str:'>'
            2023-11-25 12:40:35.620 > 303134360d str:'0146'
            2023-11-25 12:40:35.739 < 3431343633330d, str:'414633'
            2023-11-25 12:40:35.826 < 0d3e, str:'>'
            2023-11-25 12:41:47.567 > 41545a0d str:'ATZ'
            2023-11-25 12:41:48.998 < 0d0d454c4d3332372076312e350d0d3e, str:'ELM327 v1.5>'
            2023-11-25 12:41:50.180 > 41545a0d str:'ATZ'
            2023-11-25 12:41:51.432 < 41545a0d0d0d454c4d3332372076312e350d0d3e, str:'ATZELM327 v1.5>'
            2023-11-25 12:41:57.673 > 415445300d str:'ATE0'
            2023-11-25 12:41:57.773 < 415445300d4f4b0d0d3e, str:'ATE0OK>'
            2023-11-25 12:41:59.034 > 41544c300d str:'ATL0'
            2023-11-25 12:41:59.212 < 4f4b0d0d3e, str:'OK>'
            2023-11-25 12:42:00.900 > 415453300d str:'ATS0'
            2023-11-25 12:42:01.056 < 4f4b0d0d3e, str:'OK>'
            2023-11-25 12:42:02.376 > 4154535436320d str:'ATST62'
            2023-11-25 12:42:02.543 < 4f4b0d0d3e, str:'OK>'
            2023-11-25 12:42:03.642 > 41545350300d str:'ATSP0'
            2023-11-25 12:42:03.757 < 4f4b0d0d3e, str:'OK>'
            2023-11-25 12:42:06.250 > 303130430d str:'010C'
            2023-11-25 12:42:06.323 < 53, str:'S'
            2023-11-25 12:42:06.413 < 4541524348494e472e2e2e0d, str:'EARCHING...'
            2023-11-25 12:42:13.935 < 554e41424c4520544f20434f4e4e4543540d0d3e, str:'UNABLE TO CONNECT>'
            2023-11-25 12:42:14.769 > 303130430d str:'010C'
            2023-11-25 12:42:15.052 < 53, str:'S'
            2023-11-25 12:42:15.188 < 4541524348494e472e2e2e0d, str:'EARCHING...'
            2023-11-25 12:42:16.237 < 34313043303030300d0d3e, str:'410C0000>'
            2023-11-25 12:42:17.846 > 303130430d str:'010C'
            2023-11-25 12:42:17.979 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:42:18.066 < 0d3e, str:'>'
            2023-11-25 12:42:18.894 > 303130430d str:'010C'
            2023-11-25 12:42:19.056 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:42:19.146 < 0d3e, str:'>'
            2023-11-25 12:42:20.622 > 303130430d str:'010C'
            2023-11-25 12:42:20.766 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:42:20.858 < 0d3e, str:'>'
            2023-11-25 12:42:27.304 > 303134360d str:'0146'
            2023-11-25 12:42:27.876 < 3431343633330d, str:'414633'
            2023-11-25 12:42:27.922 < 0d3e, str:'>'
            2023-11-25 12:42:29.411 > 303134360d str:'0146'
            2023-11-25 12:42:29.542 < 3431343633330d, str:'414633'
            2023-11-25 12:42:29.634 < 0d3e, str:'>'
            2023-11-25 12:42:31.071 > 41544c300d str:'ATL0'
            2023-11-25 12:42:31.252 < 4f4b0d0d3e, str:'OK>'
            2023-11-25 12:42:31.821 > 303134360d str:'0146'
            2023-11-25 12:42:31.927 < 3431343633330d, str:'414633'
            2023-11-25 12:42:32.018 < 0d3e, str:'>'
            2023-11-25 12:42:34.112 > 415453300d str:'ATS0'
            2023-11-25 12:42:34.584 < 4f4b0d0d3e, str:'OK>'
            2023-11-25 12:42:34.679 > 303134360d str:'0146'
            2023-11-25 12:42:34.762 < 3431343633330d, str:'414633'
            2023-11-25 12:42:34.943 < 0d3e, str:'>'
            2023-11-25 12:42:36.024 > 303134360d str:'0146'
            2023-11-25 12:42:36.203 < 3431343633330d, str:'414633'
            2023-11-25 12:42:36.294 < 0d3e, str:'>'
            2023-11-25 12:42:37.979 > 41545a0d str:'ATZ'
            2023-11-25 12:42:39.001 < 0d0d454c4d3332372076312e350d0d3e, str:'ELM327 v1.5>'
            2023-11-25 12:42:39.084 > 303134360d str:'0146'
            2023-11-25 12:42:39.175 < 30, str:'0'
            2023-11-25 12:42:39.263 < 3134360d534541524348494e472e2e2e0d, str:'146SEARCHING...'
            2023-11-25 12:42:40.032 < 34, str:'4'
            2023-11-25 12:42:40.073 < 31203436203333200d, str:'1 46 33 '
            2023-11-25 12:42:40.258 < 0d3e, str:'>'
            2023-11-25 12:42:40.951 > 303134360d str:'0146'
            2023-11-25 12:42:41.065 < 303134360d3431203436203333200d, str:'014641 46 33 '
            2023-11-25 12:42:41.152 < 0d3e, str:'>'
            2023-11-25 12:42:47.527 > 303130430d str:'010C'
            2023-11-25 12:42:47.634 < 303130430d, str:'010C'
            2023-11-25 12:42:47.816 < 3431203043203030203030200d, str:'41 0C 00 00 '
            2023-11-25 12:42:47.903 < 0d3e, str:'>'
            2023-11-25 12:42:48.952 > 41545350300d str:'ATSP0'
            2023-11-25 12:42:49.162 < 41545350300d4f4b0d0d3e, str:'ATSP0OK>'
            2023-11-25 12:42:52.445 > 4154535436320d str:'ATST62'
            2023-11-25 12:42:52.628 < 4154535436320d4f4b0d0d3e, str:'ATST62OK>'
            2023-11-25 12:42:53.636 > 415445300d str:'ATE0'
            2023-11-25 12:42:53.709 < 415445300d4f4b0d0d3e, str:'ATE0OK>'
            2023-11-25 12:42:54.016 > 41544c300d str:'ATL0'
            2023-11-25 12:42:54.257 > 415453300d str:'ATS0'
            2023-11-25 12:42:54.295 < 4f4b0d0d3e, str:'OK>'
            2023-11-25 12:42:54.336 < 4f4b0d0d3e, str:'OK>'
            2023-11-25 12:42:55.283 > 303130430d str:'010C'
            2023-11-25 12:42:55.420 < 53, str:'S'
            2023-11-25 12:42:55.510 < 4541524348494e472e2e2e0d, str:'EARCHING...'
            2023-11-25 12:42:56.014 > 303134360d str:'0146'
            2023-11-25 12:42:56.545 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:42:56.587 < 0d3e, str:'>'
            2023-11-25 12:42:58.413 > 303134360d str:'0146'
            2023-11-25 12:42:58.477 < 3431343633330d, str:'414633'
            2023-11-25 12:42:58.569 < 0d3e, str:'>'
            2023-11-25 12:42:58.848 > 303134360d str:'0146'
            2023-11-25 12:42:58.975 < 3431343633330d, str:'414633'
            2023-11-25 12:42:59.065 < 0d3e, str:'>'
            2023-11-25 12:42:59.344 > 303130430d str:'010C'
            2023-11-25 12:42:59.514 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:42:59.557 < 0d3e, str:'>'
            2023-11-25 12:42:59.809 > 303134360d str:'0146'
            2023-11-25 12:42:59.965 < 3431343633330d, str:'414633'
            2023-11-25 12:43:00.055 < 0d3e, str:'>'
            2023-11-25 12:43:00.103 > 303130430d str:'010C'
            2023-11-25 12:43:00.187 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:43:00.278 < 0d3e, str:'>'
            2023-11-25 12:43:00.384 > 303134360d str:'0146'
            2023-11-25 12:43:00.547 < 3431343633330d, str:'414633'
            2023-11-25 12:43:00.594 < 0d3e, str:'>'
            2023-11-25 12:43:00.618 > 303130430d str:'010C'
            2023-11-25 12:43:00.682 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:43:00.773 < 0d3e, str:'>'
            2023-11-25 12:43:02.184 > 303134360d str:'0146'
            2023-11-25 12:43:02.351 < 3431343633330d, str:'414633'
            2023-11-25 12:43:02.392 < 0d3e, str:'>'
            2023-11-25 12:43:02.940 > 303130430d str:'010C'
            2023-11-25 12:43:03.067 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:43:03.157 < 0d3e, str:'>'
            2023-11-25 12:43:04.550 > 303134360d str:'0146'
            2023-11-25 12:43:04.735 < 3431343633330d, str:'414633'
            2023-11-25 12:43:04.825 < 0d3e, str:'>'
            2023-11-25 12:43:05.963 > 303130430d str:'010C'
            2023-11-25 12:43:06.083 < 34313043303030300d, str:'410C0000'
            2023-11-25 12:43:06.221 < 0d3e, str:'>'
        """.trimIndent()

        text.split("\n")
            .map {
                val items = it.split(" ").toMutableList()
                items[3] = items[3].trim(',')
                val escaped = items[3].windowed(2, 2).joinToString("") { it.hexToStr() }.let { StringEscapeUtils.escapeJava(it) }
                    .let { "'$it'" }
                (items.take(4) + escaped).joinToString(" ")
            }
            .joinToString("\n")
            .let(::println)

    }

    private fun String.hexToStr() = toByte(16).toInt().toChar().toString()
}
