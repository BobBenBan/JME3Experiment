@file:Suppress("UNCHECKED_CAST")

package org.bensnonorg.base.jme

import com.jme3.scene.Spatial
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class LazyCloning<R, T : Spatial>(private val cloneMaterials:Boolean = false, initializer: () -> T) : ReadOnlyProperty<R, T> {
	private val value by lazy(initializer)
	override fun getValue(thisRef: R, property: KProperty<*>): T = value.clone(cloneMaterials) as T
}
