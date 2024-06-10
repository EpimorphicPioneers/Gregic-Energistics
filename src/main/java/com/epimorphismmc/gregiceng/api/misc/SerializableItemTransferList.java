package com.epimorphismmc.gregiceng.api.misc;

import com.lowdragmc.lowdraglib.misc.ItemTransferList;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class SerializableItemTransferList extends ItemTransferList implements IContentChangeAware {
    @Getter
    @Setter
    protected Runnable onContentsChanged = () -> {};

    public SerializableItemTransferList(IItemTransfer... transfers) {
        super(transfers);
    }

    public SerializableItemTransferList(List<IItemTransfer> transfers) {
        super(transfers);
    }
}
