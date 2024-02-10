package org.woikaz.localstorage;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DroppedItem
{
    int id;
    int quantity;
    String name;
    int value;

    public DroppedItem(int id, int quantity, String name) {
        this.id = id;
        this.quantity = quantity;
        this.name = name;
        this.value = 0; // Default or ignored value
    }
}
