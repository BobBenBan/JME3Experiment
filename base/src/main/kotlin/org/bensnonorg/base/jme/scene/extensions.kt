package org.bensnonorg.base.jme.scene

import com.jme3.scene.Node
import com.jme3.scene.Spatial

@Suppress("UNCHECKED_CAST")
fun <T : Spatial> Node.getChild(clazz: Class<T>): T? {
	for (c in children) if (clazz.isInstance(c)) return c as T
	return null
}

inline fun <reified T : Spatial> Node.getChild(): T? {
	for (c in children) if (c is T) return c
	return null
}