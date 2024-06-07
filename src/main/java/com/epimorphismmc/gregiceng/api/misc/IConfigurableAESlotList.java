package com.epimorphismmc.gregiceng.api.misc;

import appeng.api.stacks.AEKey;
import org.jetbrains.annotations.Nullable;

public interface IConfigurableAESlotList<K extends AEKey> {
    IConfigurableAESlot<K> getAESlot(int index);
    int getSlots();
    boolean hasConfig(@Nullable K key);
    void clearConfig();
}
