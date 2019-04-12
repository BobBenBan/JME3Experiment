package org.bensnonorg.musicmachine.app

import com.jme3.app.SimpleApplication

fun SimpleApplication.init() {
    rootNode.setUserData("isRoot", true)
    guiNode.setUserData("isRoot", true)
}