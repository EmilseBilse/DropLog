package org.woikaz.ui;

import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import org.woikaz.ExamplePlugin;
import org.woikaz.localstorage.CachedItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class DropLogPanel  extends PluginPanel {
    private final ExamplePlugin plugin;

    private static final Color ODD_ROW = new Color(44, 44, 44);

    private final JPanel listContainer = new JPanel();

    private DropLogHeader countHeader;
    private DropLogHeader valueHeader;
    private DropLogHeader nameHeader;

    private SortOrder orderIndex = SortOrder.VALUE;
    private boolean ascendingOrder = false;

    private ArrayList<DropLogTableRow> rows = new ArrayList<>();

    public DropLogPanel(ExamplePlugin plugin) {
        this.plugin = plugin;

        setBorder(null);
        setLayout(new DynamicGridLayout(0, 1));

        JPanel headerContainer = buildHeader();

        listContainer.setLayout(new GridLayout(0, 1));

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
        rows.sort((r1, r2) ->
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

        for (int i = 0; i < rows.size(); i++)
        {
            DropLogTableRow row = rows.get(i);
            row.setBackground(i % 2 == 0 ? ODD_ROW : ColorScheme.DARK_GRAY_COLOR);
            listContainer.add(row);
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    void populate(List<CachedItem> items)
    {
        rows.clear();

        for (int i = 0; i < items.size(); i++)
        {
            CachedItem item = items.get(i);

            rows.add(buildRow(item, i % 2 == 0));
        }

        updateList();
    }

    public void droppedItem(CachedItem item) {
        boolean itemExists = false;
        for (DropLogTableRow row : rows) {
            if (row.getItemName().equals(item.getName())) {
                // Update quantity
                row.setQuantity(row.getItemCount() + item.getQuantity());
                itemExists = true;
                break;
            }
        }

        if (!itemExists) {
            // Add new row
            DropLogTableRow newRow = new DropLogTableRow(item);
            rows.add(newRow);
        }

        updateList();
    }


    private DropLogTableRow buildRow(CachedItem item, boolean stripe)
    {
        DropLogTableRow row = new DropLogTableRow(item);
        row.setBackground(stripe ? ODD_ROW : ColorScheme.DARK_GRAY_COLOR);
        return row;
    }

    private enum SortOrder
    {
        COUNT,
        VALUE,
        NAME
    }
}
