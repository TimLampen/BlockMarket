package me.timlampen.blockmarket;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

/**
 * Created by Primary on 3/13/2016.
 */
public class CommandHandler implements CommandExecutor{
    Main p;

    public CommandHandler(Main p){
        this.p = p;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(sender.hasPermission("blockmarket.open")){
            if(args.length == 0){
                if(sender instanceof Player){
                    Player player = (Player) sender;
                    Inventory inv = Bukkit.createInventory(player, 36, ChatColor.AQUA + "Put Items for Trade");
                    ItemStack is = new ItemStack(Material.EXP_BOTTLE);
                    ItemMeta im = is.getItemMeta();
                    im.setDisplayName(ChatColor.GREEN + "Continue?");
                    is.setItemMeta(im);
                    inv.setItem(35, is);

                    ItemStack is1 = new ItemStack(Material.IRON_FENCE);
                    ItemMeta im1 = is1.getItemMeta();
                    im1.setDisplayName(ChatColor.GOLD + "You have " + ChatColor.GRAY + 0 + ChatColor.GOLD + " points to spend (click to update)");
                    is1.setItemMeta(im1);

                    inv.setItem(34, is1);

                    player.openInventory(inv);
                }
            }else if(args.length == 1 && args[0].equalsIgnoreCase("reload")){
                if(sender.isOp()){
                    for(Player player : Bukkit.getOnlinePlayers()) {
                        player.closeInventory();
                    }
                    p.reloadConfig();
                    p.saveConfig();
                    p.shopItems.clear();
                    HashMap<ItemStack, Integer> unsortedMap = new HashMap<>();
                    for(String s : p.getConfig().getConfigurationSection("blockpoints").getKeys(false)) {
                        short durability = 0;
                        Material mat = null;
                        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + p.getConfig().getString("blockpoints." + s + ".id"));
                        if(p.getConfig().getString("blockpoints." + s + ".id").contains(":")){
                            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "yes");
                            durability = Short.parseShort(p.getConfig().getString("blockpoints." + s + ".id").split(":")[1]);
                            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "" + durability);
                            mat = Material.getMaterial(Integer.parseInt(p.getConfig().getString("blockpoints." + s + ".id").split(":")[0]));
                            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + mat.toString());
                        }else{
                            mat = Material.getMaterial(Integer.parseInt(p.getConfig().getString("blockpoints." + s + ".id")));
                        }
                        String name = p.getConfig().getString("blockpoints." + s + ".name");
                        int blockpoints = p.getConfig().getInt("blockpoints." + s + ".blockpoints");
                        int restock = p.getConfig().getInt("blockpoints." + s + ".restock");
                        int maxcap = p.getConfig().getInt("blockpoints." + s + ".maxcap");
                        boolean reset = p.getConfig().getBoolean("blockpoints." + s + ".reset");
                        p.shopItems.put(new ItemStack(mat, 1, durability), new ShopItem(mat, name, blockpoints, restock, maxcap, reset, durability));
                        unsortedMap.put(new ItemStack(mat, 1, durability), blockpoints);
                    }
                    p.blockPoints = p.sortByComparator(unsortedMap, false);
                    sender.sendMessage(ChatColor.GREEN + "Config reloaded");
                }else{
                    sender.sendMessage(ChatColor.RED + "You do not have permission to perform this command!");
                }
            }
        }
        else{
            sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
        }
        return false;
    }
}
