package org.woikaz.localstorage;

import lombok.Value;

@Value
public class InventoryItem {
    int id;
    int quantity;
    String name;
}
