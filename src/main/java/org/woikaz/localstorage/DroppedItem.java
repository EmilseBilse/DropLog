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
}
