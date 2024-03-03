package org.woikaz.localstorage;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DroppedItem implements Cloneable
{
    int id;
    int quantity;
    String name;
    int value;

    public DroppedItem(int id, int quantity, String name) {
        this.id = id;
        this.quantity = quantity;
        this.name = name;
        this.value = 0;
    }

    @Override
    public DroppedItem clone() {
        try {
            return (DroppedItem) super.clone();
        } catch (CloneNotSupportedException e) {
            // This shouldn't happen since we're Cloneable
            throw new AssertionError();
        }
    }
}
