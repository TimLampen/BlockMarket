package me.timlampen.blockmarket;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Primary on 3/5/2016.
 */
public class InventoryGenerator{
    HashMap<UUID, Integer> currentPage = new HashMap<UUID, Integer>();
    Main p;

    public InventoryGenerator(Main p){
        this.p = p;
    }

    public void createGUI(Player player, int points, boolean inShop, boolean alreadyHasPoints){
        ArrayList<ItemStack> itemList = new ArrayList<ItemStack>();
        itemList.addAll(p.shopItems.keySet());
        if(!currentPage.containsKey(player.getUniqueId())){
            currentPage.put(player.getUniqueId(), 1);
        }
        if(!p.playerShopPoints.containsKey(player.getUniqueId())){
            p.playerShopPoints.put(player.getUniqueId(), 0);
        }
        int page = currentPage.get(player.getUniqueId());
        int startIndex = page  * 53 - 53;
        int endIndex = startIndex >= itemList.size() ? itemList.size() - 1 : startIndex + 53;

        Inventory inv = null;
        if(!inShop){
            inv = Bukkit.createInventory(player, 54, ChatColor.AQUA + "Avaliable Items");
        }
        else{
            inv = player.getOpenInventory().getTopInventory();
            inv.clear();
        }
        if(alreadyHasPoints){
            inv.setItem(51, getPointsDisplay(p.playerShopPoints.get(player.getUniqueId())));
        }
        else{
            inv.setItem(51, getPointsDisplay(points));
        }
        inv.setItem(53, getPageSelector(page + 1));
        inv.setItem(50, getBackItem());
        if(page > 1){
            inv.setItem(52, getPageSelector(page - 1));
        }
        for(; startIndex < endIndex; startIndex++) {
            if(itemList.size() - 1 < startIndex){
                break;
            }
            if(startIndex <= 53 && startIndex >= 50){
                continue;
            }
            ArrayList<String> lore = new ArrayList<String>();
            ItemStack is = new ItemStack(itemList.get(startIndex).getType(), 1, itemList.get(startIndex).getDurability());
            ItemMeta im = is.getItemMeta();
            String name = is.getType().toString().toLowerCase().substring(0, 1).toUpperCase() + is.getType().toString().toLowerCase().substring(1);
            im.setDisplayName(ChatColor.AQUA + name);
            lore.add(ChatColor.GOLD + "Buy: " + ChatColor.GRAY + p.shopItems.get(is).getBlockpoints());
            lore.add(ChatColor.GOLD + "Sell: " + ChatColor.GRAY + p.blockPoints.get(is));
            lore.add(ChatColor.GOLD + "Amount Left: " + ChatColor.GRAY + p.shopItems.get(is).getAmountLeft());
            im.setLore(lore);
            is.setItemMeta(im);
            inv.addItem(is);
        }
        if(inShop){
            player.updateInventory();
        }
        else{
            player.openInventory(inv);
        }
    }

    public ItemStack getPageSelector(int newPage){
        ItemStack is = new ItemStack(Material.SHEARS);
        ItemMeta im = is.getItemMeta();

        im.setDisplayName(ChatColor.RED + "To page: " + ChatColor.DARK_GRAY + (newPage));
        is.setItemMeta(im);


        return is;
    }
    
    public ItemStack getPointsDisplay(int points){
        ItemStack is = new ItemStack(Material.IRON_FENCE);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatColor.GOLD + "You have " + ChatColor.GRAY + points + ChatColor.GOLD + " points to spend");
        is.setItemMeta(im);

        return is;
    }

    public ItemStack getBackItem(){
        ItemStack is = new ItemStack(Material.FISHING_ROD);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatColor.RED + "Go back to the insert screen.");
        is.setItemMeta(im);
        return is;
    }

}
