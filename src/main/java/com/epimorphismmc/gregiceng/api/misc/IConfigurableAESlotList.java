package com.epimorphismmc.gregiceng.api.misc;

import appeng.api.stacks.AEKey;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public interface IConfigurableAESlotList<K extends AEKey> {
    IConfigurableAESlot<K> getAESlot(int index);
    int getSlots();
    boolean hasConfig(@Nullable K key);
    void clearConfig();
    default Set<K> getAEKeySet() {
        Set<K> keys = new HashSet<>();
        for (int i = 0; i < getSlots(); i++) {
            var key = getAESlot(i).getConfig();
            if (key != null) {
                keys.add(key);
            }
        }
        return keys;
    }
}
