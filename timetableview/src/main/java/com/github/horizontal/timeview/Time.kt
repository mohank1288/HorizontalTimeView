package com.github.horizontal.timeview

import java.io.Serializable

class Time : Serializable {
    var hour = 0
    var minute = 0

    constructor(hour: Int, minute: Int) {
        this.hour = hour
        this.minute = minute
    }

    constructor() {}
}