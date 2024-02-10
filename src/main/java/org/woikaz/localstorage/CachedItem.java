package org.woikaz.localstorage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

@Data
@AllArgsConstructor
public class CachedItem
{
    int id;
    int quantity;
    String name;
    int value;
}
