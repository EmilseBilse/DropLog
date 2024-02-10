package org.woikaz.localstorage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DropDataStorage {
    private static final String FILE_PATH = "dropLogData.json";
    private final Gson gson = new Gson();

    public void saveItem(DroppedItem item) {
        List<DroppedItem> items = loadAllItems(); // Load all items

        Optional<DroppedItem> existingItemOpt = items.stream()
                .filter(i -> i.getName().equals(item.getName()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            // If the item exists, update its quantity
            DroppedItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + item.getQuantity();
            existingItem.setQuantity(newQuantity);
        } else {
            items.add(item);
        }

        // Save the updated list of items
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(items, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<DroppedItem> loadAllItems() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            return gson.fromJson(reader, new TypeToken<List<DroppedItem>>(){}.getType());
        } catch (FileNotFoundException e) {
            return new ArrayList<>(); // No file yet, return empty list
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
