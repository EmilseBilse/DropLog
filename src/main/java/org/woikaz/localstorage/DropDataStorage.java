package org.woikaz.localstorage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

public class DropDataStorage {
    private static final String FILE_PATH = "dropLogData.json";
    @Inject
    private Gson gson;

    public void saveItem(DroppedItem item) {
        List<DroppedItem> items = loadAllItems();

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

    public void removeItem(String itemName) {
        List<DroppedItem> items = loadAllItems();

        items.removeIf(item -> item.getName().equals(itemName));

        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(items, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setItemQuantity(String itemName, int newQuantity) {
        List<DroppedItem> items = loadAllItems();

        if (newQuantity > 0) {
            items.forEach(item -> {
                if (item.getName().equals(itemName)) {
                    item.setQuantity(newQuantity);
                }
            });
        } else {
            items.removeIf(item -> item.getName().equals(itemName));
        }

        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(items, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void decreaseItemQuantity(String itemName, int decreaseAmount) {
        List<DroppedItem> items = loadAllItems();

        ListIterator<DroppedItem> iterator = items.listIterator();
        while (iterator.hasNext()) {
            DroppedItem item = iterator.next();
            if (item.getName().equals(itemName)) {
                int newQuantity = item.getQuantity() - decreaseAmount;
                if (newQuantity > 0) {
                    item.setQuantity(newQuantity);
                } else {
                    // Remove the item if the new quantity is 0 or less
                    iterator.remove();
                }
                break; // Assuming item names are unique, break after finding the match
            }
        }

        // Save the updated list back to the JSON file
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(items, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public List<DroppedItem> loadAllItems() {
        List<DroppedItem> items = new ArrayList<>();
        File file = new File(FILE_PATH);

        // Check if the file exists, and if not, create it
        if (!file.exists()) {
            try {
                System.out.println("Create new file");
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("New file failed");
            }
        }

        // Proceed with loading items from the file
        try (Reader reader = new FileReader(file)) {
            List<DroppedItem> result = gson.fromJson(reader, new TypeToken<List<DroppedItem>>(){}.getType());
            if (result != null) {
                items = result;
            }
        } catch (FileNotFoundException e) {
            // This exception should not occur since we've just checked/created the file
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }
}
