package org.woikaz.ui;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import org.woikaz.DropLogPlugin;
import org.woikaz.localstorage.DropDataStorage;
import org.woikaz.localstorage.DroppedItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static net.runelite.client.RuneLite.getInjector;

public class DropLogPanel  extends PluginPanel {
    private final DropLogPlugin plugin;

    private static final Color ODD_ROW = new Color(44, 44, 44);

    private final JPanel listContainer = new JPanel();

    private DropLogHeader countHeader;
    private DropLogHeader valueHeader;
    private DropLogHeader nameHeader;

    private SortOrder orderIndex = SortOrder.VALUE;
    private boolean ascendingOrder = false;

    private ArrayList<DropLogTableRow> rows = new ArrayList<>();
    private List<DropLogTableRow> allRows = new ArrayList<>(); // For all items loaded from the file
    private List<DropLogTableRow> sessionRows = new ArrayList<>(); // For items dropped in the current session
    private boolean showingAllItems = true;

    private DropDataStorage dropDataStorage;

    public DropLogPanel(DropLogPlugin plugin) {
        this.plugin = plugin;
        dropDataStorage = new DropDataStorage();
        getInjector().injectMembers(dropDataStorage);

        setBorder(null);
        setLayout(new DynamicGridLayout(0, 1));

        JPanel headerContainer = buildHeader();

        listContainer.setLayout(new GridLayout(0, 1));

        JPanel controlPanel = new JPanel(new BorderLayout());
        JButton toggleButton = new JButton("Show Current Session");
        // toggleButton.setMargin(new Insets(10, 20, 10, 20));
        toggleButton.addActionListener(e -> {
            toggleViewMode();
            if (showingAllItems) {
                toggleButton.setText("Show Current Session");
            } else {
                toggleButton.setText("Show All Time");
            }
        });
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(toggleButton, BorderLayout.CENTER);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        controlPanel.add(buttonPanel, BorderLayout.CENTER);

        add(controlPanel, BorderLayout.NORTH);

        add(headerContainer);
        add(listContainer);
    }

    private JPanel buildHeader()
    {
        JPanel header = new JPanel(new BorderLayout());
        JPanel leftSide = new JPanel(new BorderLayout());
        JPanel rightSide = new JPanel(new BorderLayout());

        nameHeader = new DropLogHeader("Name", orderIndex == SortOrder.NAME, ascendingOrder);
        nameHeader.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent mouseEvent)
            {
                if (SwingUtilities.isRightMouseButton(mouseEvent))
                {
                    return;
                }
                ascendingOrder = orderIndex != SortOrder.NAME || !ascendingOrder;
                orderBy(SortOrder.NAME);
            }
        });

        countHeader = new DropLogHeader("#", orderIndex == SortOrder.COUNT, ascendingOrder);
        countHeader.setPreferredSize(new Dimension(DropLogTableRow.ITEM_COUNT_COLUMN_WIDTH, 0));
        countHeader.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent mouseEvent)
            {
                if (SwingUtilities.isRightMouseButton(mouseEvent))
                {
                    return;
                }
                ascendingOrder = orderIndex != SortOrder.COUNT || !ascendingOrder;
                orderBy(SortOrder.COUNT);
            }
        });

        valueHeader = new DropLogHeader("$", orderIndex == SortOrder.VALUE, ascendingOrder);
        valueHeader.setPreferredSize(new Dimension(DropLogTableRow. ITEM_VALUE_COLUMN_WIDTH, 0));
        valueHeader.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent mouseEvent)
            {
                if (SwingUtilities.isRightMouseButton(mouseEvent))
                {
                    return;
                }
                ascendingOrder = orderIndex != SortOrder.VALUE || !ascendingOrder;
                orderBy(SortOrder.VALUE);
            }
        });


        leftSide.add(nameHeader, BorderLayout.CENTER);
        leftSide.add(countHeader, BorderLayout.EAST);
        rightSide.add(valueHeader, BorderLayout.CENTER);

        header.add(leftSide, BorderLayout.CENTER);
        header.add(rightSide, BorderLayout.EAST);

        return header;
    }

    private void orderBy(SortOrder order)
    {
        nameHeader.highlight(false, ascendingOrder);
        countHeader.highlight(false, ascendingOrder);
        valueHeader.highlight(false, ascendingOrder);

        switch (order)
        {
            case NAME:
                nameHeader.highlight(true, ascendingOrder);
                break;
            case COUNT:
                countHeader.highlight(true, ascendingOrder);
                break;
            case VALUE:
                valueHeader.highlight(true, ascendingOrder);
                break;
        }

        orderIndex = order;
        updateList();
    }

    void updateList()
    {

        List<DropLogTableRow> filteredRows = rows.stream()
                .filter(row -> row.getItemCount() > 0)
                .collect(Collectors.toList());

        filteredRows.sort((r1, r2) ->
        {
            switch (orderIndex)
            {
                case NAME:
                    return r1.getItemName().compareTo(r2.getItemName()) * (ascendingOrder ? 1 : -1);
                case COUNT:
                    return Integer.compare(r1.getItemCount(), r2.getItemCount()) * (ascendingOrder ? 1 : -1);
                case VALUE:
                    return Integer.compare(r1.getPrice(), r2.getPrice()) * (ascendingOrder ? 1 : -1);
                default:
                    return 0;
            }
        });

        listContainer.removeAll();

        for (int i = 0; i < filteredRows.size(); i++)
        {
            DropLogTableRow row = filteredRows.get(i);
            row.setBackground(i % 2 == 0 ? ODD_ROW : ColorScheme.DARK_GRAY_COLOR);
            listContainer.add(row);
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    public void droppedItem(DroppedItem item) {
        DroppedItem itemClone1 = item.clone();
        updateListWithItem(sessionRows, itemClone1);

        DroppedItem itemClone2 = item.clone();
        updateListWithItem(allRows, itemClone2);

        rows = showingAllItems ? new ArrayList<>(allRows) : new ArrayList<>(sessionRows);
        updateList();
    }

    private boolean updateListWithItem(List<DropLogTableRow> listToUpdate, DroppedItem item) {
        for (DropLogTableRow row : listToUpdate) {
            if (row.getItemName().equals(item.getName().trim())) {
                row.setQuantity(row.getItemCount() + item.getQuantity());
                return true; // Item was found and updated
            }
        }
        DropLogTableRow newRow = buildRow(item, listToUpdate.size() % 2 == 0);
        listToUpdate.add(newRow);
        return false; // A new item was added
    }

    public void removeDroppedItem(DroppedItem item) {
        SwingUtilities.invokeLater(() -> {
            updateListWithItemForRemoval(sessionRows, item);
            updateListWithItemForRemoval(allRows, item);
            rows = showingAllItems ? new ArrayList<>(allRows) : new ArrayList<>(sessionRows);
            updateList();
        });
    }

    private boolean updateListWithItemForRemoval(List<DropLogTableRow> listToUpdate, DroppedItem item) {
        Iterator<DropLogTableRow> iterator = listToUpdate.iterator();
        while (iterator.hasNext()) {
            DropLogTableRow row = iterator.next();
            if (row.getItemName().equals(item.getName().trim())) {
                int newQuantity = row.getItemCount() - item.getQuantity();
                if (newQuantity > 0) {
                    row.setQuantity(newQuantity);
                } else {
                    iterator.remove();
                }
                return true;
            }
        }
        return false;
    }

    public void removeRow(DropLogTableRow row) {
        rows.remove(row);

        listContainer.remove(row);

        listContainer.revalidate();
        listContainer.repaint();

        String itemName = row.getItemName();
        if (showingAllItems) {
            dropDataStorage.removeItem(itemName); // Remove the item from the JSON file
        }
    }

    public void removeQuantity(String itemName, Integer newQuantity) {
        if (showingAllItems) {
            dropDataStorage.setItemQuantity(itemName, newQuantity);
        }
    }


    private DropLogTableRow buildRow(DroppedItem item, boolean stripe)
    {
        DropLogTableRow row = new DropLogTableRow(this, item);
        row.setBackground(stripe ? ODD_ROW : ColorScheme.DARK_GRAY_COLOR);
        return row;
    }

    private enum SortOrder
    {
        COUNT,
        VALUE,
        NAME
    }

    private void toggleViewMode() {
        showingAllItems = !showingAllItems;
        if (showingAllItems) {
            rows = new ArrayList<>(allRows);
        } else {
            rows = new ArrayList<>(sessionRows);
        }
        updateList();
    }

    public void populateAllRows(List<DroppedItem> loadedItems) {
        allRows.clear();
        for (DroppedItem item : loadedItems) {
            allRows.add(buildRow(item, allRows.size() % 2 == 0));
        }

        // Update the display based on the current view mode
        rows = showingAllItems ? new ArrayList<>(allRows) : new ArrayList<>(sessionRows);
        updateList();
    }



}
