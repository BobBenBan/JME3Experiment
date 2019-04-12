package org.bensnonorg.musicmachine.core.util

import java.util.logging.Level
import java.util.logging.Logger

inline fun Logger.logIf(level: Level?, message: (() -> String?)) {
    if (isLoggable(level)) {
        log(level, message())
    }
}