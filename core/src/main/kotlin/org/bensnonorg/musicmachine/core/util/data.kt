package org.bensnonorg.musicmachine.core.util

import com.jme3.util.SafeArrayList

inline fun <T> SafeArrayList<T>.forEach(action: (T) -> Unit) {
    for(element in this.array) action(element)
}