package com.github.horizontal.timeview

import java.io.Serializable
import java.time.LocalDateTime

class Schedule : Serializable {
    var classTitle = ""
    var classPlace = ""
    var professorName = ""
    var day = 0
    var startTime: LocalDateTime
    var endTime: LocalDateTime

    init {
        startTime = LocalDateTime.now()
        endTime = LocalDateTime.now()
    }
}