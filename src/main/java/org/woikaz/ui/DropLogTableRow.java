package org.woikaz.ui;

import lombok.Getter;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.QuantityFormatter;
import org.woikaz.localstorage.DroppedItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DropLogTableRow extends JPanel {
    static final int ITEM_NAME_COLUMN_WIDTH = 60;
    static final int ITEM_COUNT_COLUMN_WIDTH = 45;
    static final int ITEM_VALUE_COLUMN_WIDTH = 45;

    private JLabel itemName;
    private JLabel itemCount;
    private JLabel value;

    @Getter
    private final DroppedItem item;

    private DropLogPanel parentPanel;

    private Color lastBackground;

    DropLogTableRow(DropLogPanel parentPanel, DroppedItem item)
    {
        this.parentPanel = parentPanel;
        this.item = item;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(2, 0, 2, 0));

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");
        JMenuItem removeXItem = new JMenuItem("Remove X");

        deleteItem.addActionListener(e -> {
            parentPanel.removeRow(DropLogTableRow.this);
        });

        removeXItem.addActionListener(e -> {
            // Prompt the user to enter the amount to remove
            String amountStr = JOptionPane.showInputDialog(this, "Enter amount to remove:", "Remove Amount", JOptionPane.PLAIN_MESSAGE);
            if (amountStr != null && !amountStr.isEmpty()) {
                try {
                    int amountToRemove = Integer.parseInt(amountStr);
                    if (amountToRemove > 0 && amountToRemove <= this.item.getQuantity()) {
                        int newQuantity = this.item.getQuantity() - amountToRemove;
                        this.item.setQuantity(newQuantity);
                        setQuantity(newQuantity);

                        parentPanel.updateList();

                    } else {
                        // Handle invalid input (e.g., number too large or negative)
                        JOptionPane.showMessageDialog(this, "Invalid amount entered.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        // Add items to the popup menu
        popupMenu.add(deleteItem);
        popupMenu.add(removeXItem);

        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent)
            {
                DropLogTableRow.this.lastBackground = getBackground();
                setBackground(getBackground().brighter());
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent)
            {
                setBackground(lastBackground);
            }
        });

        JPanel leftSide = new JPanel(new BorderLayout());
        JPanel rightSide = new JPanel(new BorderLayout());
        leftSide.setOpaque(false);
        rightSide.setOpaque(false);

        JPanel itemNameField = buildItemNameField();
        itemNameField.setOpaque(false);

        JPanel itemCountField = buildItemCountField();
        itemCountField.setPreferredSize(new Dimension(ITEM_COUNT_COLUMN_WIDTH, 0));
        itemCountField.setOpaque(false);

        JPanel valueField = buildValueField();
        valueField.setPreferredSize(new Dimension(ITEM_VALUE_COLUMN_WIDTH, 0));
        valueField.setOpaque(false);

        leftSide.add(itemNameField, BorderLayout.CENTER);
        leftSide.add(itemCountField, BorderLayout.EAST);
        rightSide.add(valueField, BorderLayout.CENTER);

        add(leftSide, BorderLayout.CENTER);
        add(rightSide, BorderLayout.EAST);
    }

    private JPanel buildValueField()
    {
        JPanel column = new JPanel(new BorderLayout());
        column.setBorder(new EmptyBorder(0, 5, 0, 5));

        value = new JLabel(QuantityFormatter.quantityToStackSize(getPrice()));
        value.setFont(FontManager.getRunescapeSmallFont());

        column.add(value, BorderLayout.EAST);

        return column;
    }

    int getPrice() {
        return item.getValue() * item.getQuantity();
    }

    private JPanel buildItemCountField()
    {
        JPanel column = new JPanel(new BorderLayout());
        column.setBorder(new EmptyBorder(0, 5, 0, 5));

        itemCount = new JLabel(QuantityFormatter.quantityToStackSize(getItemCount()));
        itemCount.setFont(FontManager.getRunescapeSmallFont());

        column.add(itemCount, BorderLayout.WEST);

        return column;
    }

    int getItemCount() {
        return item.getQuantity();
    }

    void setQuantity(int newQuantity) {
        item.setQuantity(newQuantity);

        itemCount.setText(QuantityFormatter.quantityToStackSize(newQuantity));

        int newPrice = getPrice();
        value.setText(QuantityFormatter.quantityToStackSize(newPrice));
    }

    private JPanel buildItemNameField()
    {
        JPanel column = new JPanel(new BorderLayout());
        column.setBorder(new EmptyBorder(0, 5, 0, 5));

        itemName = new JLabel(getItemName());
        itemName.setFont(FontManager.getRunescapeSmallFont());

        column.add(itemName, BorderLayout.WEST);

        return column;
    }

    String getItemName() {
        return item.getName();
    }
}
