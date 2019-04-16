// HideableKt.java
package org.bensnonorg.musicmachine.scene;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(
		mv = {1, 1, 15},
		bv = {1, 0, 3},
		k = 2,
		d1 = {"\u0000\u001e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\u001a\n\u0010\b\u001a\u00020\t*\u00020\u0001\u001a\n\u0010\n\u001a\u00020\t*\u00020\u0002\u001a\n\u0010\u000b\u001a\u00020\t*\u00020\u0001\"\u0017\u0010\u0000\u001a\u0004\u0018\u00010\u0001*\u00020\u00028F¢\u0006\u0006\u001a\u0004\b\u0003\u0010\u0004\"\u0015\u0010\u0005\u001a\u00020\u0006*\u00020\u00018F¢\u0006\u0006\u001a\u0004\b\u0005\u0010\u0007¨\u0006\f"},
		d2 = {"hideableImpl", "Lorg/bensnonorg/musicmachine/scene/Hideable;", "Lcom/jme3/scene/Spatial;",
				"getHideableImpl", "(Lcom/jme3/scene/Spatial;)Lorg/bensnonorg/musicmachine/scene/Hideable;",
				"isEffectivelyHidden", "", "(Lorg/bensnonorg/musicmachine/scene/Hideable;)Z", "hide", "",
				"refreshParentsHidden", "reveal", "core"
		}
)
public final class HideableKT {
	public static boolean isEffectivelyHidden(@NotNull Hideable $this$isEffectivelyHidden) {
		Intrinsics.checkParameterIsNotNull($this$isEffectivelyHidden, "$this$isEffectivelyHidden");
		return $this$isEffectivelyHidden.isHidden() || $this$isEffectivelyHidden.getParentsHidden();
	}
	
	@Nullable
	public static Hideable getHideableImpl(@NotNull Spatial $this$hideableImpl) {
		Intrinsics.checkParameterIsNotNull($this$hideableImpl, "$this$hideableImpl");
		Object var10000;
		if ($this$hideableImpl instanceof Hideable) {
			var10000 = $this$hideableImpl;
		} else {
			int i$iv$iv = 0;
			int var6 = $this$hideableImpl.getNumControls();
			
			while (true) {
				if (i$iv$iv >= var6) {
					var10000 = null;
					break;
				}
				
				Control var9 = $this$hideableImpl.getControl(i$iv$iv);
				Intrinsics.checkExpressionValueIsNotNull(var9, "getControl(i)");
				if (var9 instanceof Hideable) {
					var10000 = var9;
					break;
				}
				
				++i$iv$iv;
			}
		}
		
		return (Hideable) var10000;
	}
	
	public static void hide(@NotNull Hideable $this$hide) {
		Intrinsics.checkParameterIsNotNull($this$hide, "$this$hide");
		$this$hide.setHidden(true);
	}
	
	public static void reveal(@NotNull Hideable $this$reveal) {
		Intrinsics.checkParameterIsNotNull($this$reveal, "$this$reveal");
		$this$reveal.setHidden(false);
	}
	
	public static void refreshParentsHidden(@NotNull Spatial $this$refreshParentsHidden) throws Throwable {
		Intrinsics.checkParameterIsNotNull($this$refreshParentsHidden, "$this$refreshParentsHidden");
		Hideable var10000 = getHideableImpl($this$refreshParentsHidden);
		if (var10000 == null) {
			throw new IllegalStateException("This spatial is not Hideable");
		} else {
			Hideable hideableImpl;
			boolean var3;
			label16:
			{
				hideableImpl = var10000;
				Node var10001 = $this$refreshParentsHidden.getParent();
				if (var10001 != null) {
					Hideable var2 = getHideableImpl(var10001);
					if (var2 != null) {
						var3 = isEffectivelyHidden(var2);
						break label16;
					}
				}
				
				var3 = false;
			}
			
			hideableImpl.setParentsHidden(var3);
		}
	}
}
