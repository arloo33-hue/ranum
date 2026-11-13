package com.randomgundrops;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

/**
 * Talks to TACZ only through reflection, so this mod never needs a specific TACZ version to compile or load.
 * Everything TACZ has loaded is visible here: the base game and every addon mod or gunpack.
 */
public final class TaczCompat {
    private static final Set<String> WARNED = new HashSet<>();

    private TaczCompat() {
    }

    private static Class<?> api() throws ClassNotFoundException {
        return Class.forName("com.tacz.guns.api.TimelessAPI");
    }

    private static void warn(String what, Throwable t) {
        if (WARNED.add(what)) {
            System.err.println("[Random Gun Drops] TACZ lookup '" + what + "' failed: " + t);
        }
    }

    private static List<ResourceLocation> ids(String method) {
        List<ResourceLocation> out = new ArrayList<>();
        try {
            Object result = api().getMethod(method).invoke(null);
            if (result instanceof Iterable<?> it) {
                for (Object o : it) {
                    if (o instanceof Map.Entry<?, ?> e && e.getKey() instanceof ResourceLocation rl) {
                        out.add(rl);
                    }
                }
            }
        } catch (Throwable t) {
            warn(method, t);
        }
        out.sort(Comparator.comparing(ResourceLocation::toString));
        return out;
    }

    public static List<ResourceLocation> gunIds() {
        return ids("getAllCommonGunIndex");
    }

    public static List<ResourceLocation> ammoIds() {
        return ids("getAllCommonAmmoIndex");
    }

    public static List<ResourceLocation> attachmentIds() {
        return ids("getAllCommonAttachmentIndex");
    }

    private static Object gunData(ResourceLocation gunId) {
        try {
            Object opt = api().getMethod("getCommonGunIndex", ResourceLocation.class).invoke(null, gunId);
            if (opt instanceof Optional<?> o && o.isPresent()) {
                Object index = o.get();
                return index.getClass().getMethod("getGunData").invoke(index);
            }
        } catch (Throwable t) {
            warn("getCommonGunIndex", t);
        }
        return null;
    }

    /** The ammo type this gun uses, or null if it can't be read. */
    public static ResourceLocation ammoFor(ResourceLocation gunId) {
        Object data = gunData(gunId);
        if (data == null) return null;
        try {
            Object r = data.getClass().getMethod("getAmmoId").invoke(data);
            if (r instanceof ResourceLocation rl) return rl;
        } catch (Throwable t) {
            warn("getAmmoId", t);
        }
        return null;
    }

    /** The first fire mode the gun supports (for example SEMI or AUTO), or null if it can't be read. */
    public static String firstFireMode(ResourceLocation gunId) {
        Object data = gunData(gunId);
        if (data == null) return null;
        try {
            Object set = data.getClass().getMethod("getFireModeSet").invoke(data);
            if (set instanceof Iterable<?> it) {
                for (Object m : it) {
                    return m instanceof Enum<?> en ? en.name() : String.valueOf(m);
                }
            }
        } catch (Throwable t) {
            warn("getFireModeSet", t);
        }
        return null;
    }
}
