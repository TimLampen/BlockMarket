package me.timlampen.blockmarket;

import com.mysql.fabric.xmlrpc.base.Array;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Primary on 3/5/2016.
 */
public class InteractListener implements Listener{
    Main p;
    public InteractListener(Main p){
        this.p = p;
    }
    ArrayList<UUID> goingBack = new ArrayList<UUID>();
    @EventHandler
    public void onClick(InventoryClickEvent event){
        Player player = (Player)event.getWhoClicked();
        if(event.getClickedInventory()!=null){
            Inventory inv = event.getClickedInventory();
            if(event.getView().getTopInventory().getName().contains("Avaliable")){
                event.setCancelled(true);
            }
            if(inv.getName().contains("Avaliable") ){
                if(event.getCurrentItem()==null){
                    return;
                }
                ItemStack is = event.getCurrentItem();
                if(is.getType()==Material.SHEARS){
                    int page = Integer.parseInt(ChatColor.stripColor(is.getItemMeta().getDisplayName()).replace("To page: ", ""));
                    p.generator.currentPage.put(player.getUniqueId(), page);
                    p.generator.createGUI(player, p.playerShopPoints.get(player.getUniqueId()), true, true);
                }
                else if(is.getType()==Material.IRON_FENCE){
                    event.setCancelled(true);
                }
                else if(is.getType()==Material.FISHING_ROD){
                    goingBack.add(player.getUniqueId());
                    Inventory inva = Bukkit.createInventory(player, 36, ChatColor.AQUA + "Put Items for Trade");
                    ItemStack isa = new ItemStack(Material.EXP_BOTTLE);
                    ItemMeta im = isa.getItemMeta();
                    im.setDisplayName(ChatColor.GREEN + "Continue?");//
                    isa.setItemMeta(im);
                    inva.setItem(35, isa);

                    ItemStack is1 = new ItemStack(Material.IRON_FENCE);
                    ItemMeta im1 = is1.getItemMeta();
                    int amt = p.playerShopPoints.containsKey(player.getUniqueId()) ? p.playerShopPoints.get(player.getUniqueId()) : 0;
                    im1.setDisplayName(ChatColor.GOLD + "You have " + ChatColor.GRAY + amt + ChatColor.GOLD + " points to spend (click to update)");
                    is1.setItemMeta(im1);

                    inva.setItem(34, is1);

                    player.openInventory(inva);
                }
                else if(is!=null && is.getType()!=Material.AIR){
                    is.setAmount(1);
                    player.sendMessage(is.getType() + " - type, " + is.getDurability() + " - dura");
                    if(p.shopItems.get(is)==null){
                        player.sendMessage(p.shopItems.size() + "");
                    }
                    if(p.shopItems.get(is).getAmountLeft()<=0){
                        player.sendMessage(ChatColor.RED + "Error: This item is sold out! Wait for it to be restocked.");
                    }
                    else{
                        int cost = p.shopItems.get(is).getBlockpoints();
                        int cpoints = p.playerShopPoints.get(player.getUniqueId());
                        if(cost <= cpoints){
                            if(player.getInventory().firstEmpty() != -1){
                                p.playerShopPoints.put(player.getUniqueId(), p.playerShopPoints.get(player.getUniqueId()) - cost);
                                player.getInventory().addItem(is);
                                player.updateInventory();
                                p.shopItems.get(is).setAmountLeft(p.shopItems.get(is).getAmountLeft()-1);
                                p.generator.createGUI(player, p.playerShopPoints.get(player.getUniqueId()), true, true);
                            }else{
                                player.sendMessage(ChatColor.RED + "Your inventory is full!");
                            }
                        }else{
                            player.sendMessage(ChatColor.RED + "You do not have enough points to purchase this!");
                        }
                    }
                }
            }
            else if(inv.getName().contains("Put Items for Trade")){
                if(event.getCurrentItem().getType()==Material.EXP_BOTTLE){
                    event.setCancelled(true);
                    ArrayList<ItemStack> cache = new ArrayList<ItemStack>();
                    for(ItemStack is : inv.getContents()){
                        if(is!=null && is.getType()!=Material.AIR){
                            int amt = is.getAmount();
                            short durability = is.getDurability();
                            ItemStack compare = new ItemStack(is.getType(), 1, is.getDurability());
                            if(!p.blockPoints.containsKey(compare) && !(compare.getType()==Material.EXP_BOTTLE) && !(compare.getType()==Material.IRON_FENCE)){
                                if(player.getInventory().firstEmpty() == -1){
                                    player.getWorld().dropItem(player.getLocation(), is);
                                }else{
                                    player.getInventory().addItem(is);
                                }
                            }
                            else if(compare.getType()!=Material.EXP_BOTTLE && compare.getType()!=Material.IRON_FENCE){
                                cache.add(new ItemStack(compare.getType(), amt, durability));
                            }
                        }
                    }
                    if(goingBack.contains(player.getUniqueId())){
                        goingBack.remove(player.getUniqueId());
                    }
                    if(p.playerCache.containsKey(player.getUniqueId())){
                        cache.addAll(p.playerCache.get(player.getUniqueId()));
                        p.playerCache.put(player.getUniqueId(), cache);
                        p.playerShopPoints.put(player.getUniqueId(), p.playerShopPoints.get(player.getUniqueId())+p.calculatePoints(inv));
                        p.origPoints.put(player.getUniqueId(), p.origPoints.get(player.getUniqueId()) + p.calculatePoints(inv));
                        p.generator.createGUI(player, p.calculatePoints(inv), false, true);
                    }
                    else{
                        p.playerCache.put(player.getUniqueId(), cache);
                        p.playerShopPoints.put(player.getUniqueId(), p.calculatePoints(inv));
                        p.origPoints.put(player.getUniqueId(), p.calculatePoints(inv));
                        p.generator.createGUI(player, p.calculatePoints(inv), false, false);
                    }
                }
                else if(event.getCurrentItem().getType()==Material.IRON_FENCE){
                    event.setCancelled(true);
                    ItemStack is1 = new ItemStack(Material.IRON_FENCE);
                    ItemMeta im1 = is1.getItemMeta();
                    int i = p.playerShopPoints.containsKey(player.getUniqueId()) ? p.playerShopPoints.get(player.getUniqueId())+p.calculatePoints(inv) : p.calculatePoints(inv);
                    im1.setDisplayName(ChatColor.GOLD + "You have " + ChatColor.GRAY + i + ChatColor.GOLD + " points to spend (click to update)");
                    is1.setItemMeta(im1);
                    inv.setItem(34, is1);
                    player.updateInventory();
                }
            }
        }
    }
    @EventHandler
    public void onOpen(InventoryOpenEvent event){
        Player player = (Player)event.getPlayer();
        if(event.getInventory().getName().contains("Put Items for Trade")){
            if(goingBack.contains(player.getUniqueId())){
                goingBack.remove(player.getUniqueId());
            }
        }
    }
    @EventHandler
    public void onClose(InventoryCloseEvent event){
        Inventory inv = event.getInventory();
        Player player = (Player)event.getPlayer();
        if((inv.getName().contains("Avaliable") && !goingBack.contains(player.getUniqueId())) || (inv.getName().contains("Avaliable") && p.playerShopPoints.containsKey(player.getUniqueId())) && goingBack.contains(player.getUniqueId())){
            int blockpoints = p.origPoints.get(player.getUniqueId()) - p.playerShopPoints.get(player.getUniqueId());
            ArrayList<ItemStack> remainder = p.calculateRemainder(player.getUniqueId(), blockpoints);
            for(ItemStack is : remainder){
                player.getInventory().addItem(is);
            }
            player.sendMessage(ChatColor.GREEN + "Your remaining points have been turned into block form");
            p.playerShopPoints.remove(player.getUniqueId());
            p.origPoints.remove(player.getUniqueId());
            p.playerCache.remove(player.getUniqueId());
            p.generator.currentPage.remove(player.getUniqueId());

        }
    }
}
