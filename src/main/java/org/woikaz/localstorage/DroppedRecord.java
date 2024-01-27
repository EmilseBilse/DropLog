package org.woikaz.localstorage;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.http.api.loottracker.LootRecordType;

import java.util.Collection;
import java.util.Date;

@Data
@AllArgsConstructor
public class DroppedRecord {
    public final String name;
    public final Collection<DroppedItemEntry> drops;
    public Date date;

    public void addDropEntry(DroppedItemEntry itemEntry)
    {
        drops.add(itemEntry);
    }
}
