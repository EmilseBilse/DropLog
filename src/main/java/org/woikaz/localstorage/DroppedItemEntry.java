package org.woikaz.localstorage;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DroppedItemEntry {
    public final String name;
    public final int id;
    public int quantity;
    public long price;

    public long getTotal() {
        return this.quantity * this.price;
    }
}
