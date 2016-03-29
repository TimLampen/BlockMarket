package me.timlampen.blockmarket;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

/**
 * Created by Primary on 3/5/2016.
 */
public class Main extends JavaPlugin{

    InventoryGenerator generator = null;
    LinkedHashMap<ItemStack, ShopItem> shopItems = new LinkedHashMap<ItemStack, ShopItem>();//all of the items in the shop (unsorted by value)
    HashMap<UUID, Integer> playerShopPoints = new HashMap<UUID, Integer>();//amount of points each player has while in shop
    HashMap<ItemStack, Integer> blockPoints = null;//the points that each block is worth
    HashMap<UUID, ArrayList<ItemStack>> playerCache = new HashMap<UUID, ArrayList<ItemStack>>();
    HashMap<UUID, Integer> origPoints = new HashMap<>();
    @Override
    public void onEnable(){
        getCommand("bmk").setExecutor(new CommandHandler(this));
        Bukkit.getPluginManager().registerEvents(new InteractListener(this), this);
        generator = new InventoryGenerator(this);
        if(!new File(getDataFolder() + "/config.yml").exists()){
            saveDefaultConfig();
        }
        HashMap<ItemStack, Integer> unsortedMap = new HashMap<>();
        for(String s : getConfig().getConfigurationSection("blockpoints").getKeys(false)) {
            short durability = 0;
            Material mat = null;
            if(getConfig().getString("blockpoints." + s + ".id").contains(":")){
                durability = Short.parseShort(getConfig().getString("blockpoints." + s + ".id").split(":")[1]);
                mat = Material.getMaterial(Integer.parseInt(getConfig().getString("blockpoints." + s + ".id").split(":")[0]));
            }else{
                mat = Material.getMaterial(Integer.parseInt(getConfig().getString("blockpoints." + s + ".id")));
            }
            String name = getConfig().getString("blockpoints." + s + ".name");
            int buypoints = Integer.parseInt(getConfig().getString("blockpoints." + s + ".blockpoints").split(":")[0]);
            int sellpoints = Integer.parseInt(getConfig().getString("blockpoints." + s + ".blockpoints").split(":")[1]);
            int restock = getConfig().getInt("blockpoints." + s + ".restock");
            int maxcap = getConfig().getInt("blockpoints." + s + ".maxcap");
            boolean reset = getConfig().getBoolean("blockpoints." + s + ".reset");
            shopItems.put(new ItemStack(mat, 1, durability), new ShopItem(mat, name, buypoints, restock, maxcap, reset, durability));
            unsortedMap.put(new ItemStack(mat, 1, durability), sellpoints);
        }
        blockPoints = sortByComparator(unsortedMap, false);

        new BukkitRunnable(){
            @Override
            public void run(){
                for(ShopItem shopItem : shopItems.values()){
                    if(shopItem.getReset()){
                        shopItem.setAmountLeft(shopItem.getRestock());
                    }
                    else{
                        if(shopItem.getAmountLeft() + shopItem.getRestock() > shopItem.getMaxcap()){
                            shopItem.setAmountLeft(shopItem.getMaxcap());
                        }else{
                            shopItem.setAmountLeft(shopItem.getAmountLeft() + shopItem.getRestock());
                        }
                    }
                }
            }
        }.runTaskTimer(this, 20*60*60, 20*60*60);
    }

    @Override
    public void onDisable(){
        saveDefaultConfig();
    }

    public ArrayList<ItemStack> calculateRemainder(UUID uuid, int total){
        ArrayList<ItemStack> returnList = new ArrayList<ItemStack>();
        HashMap<ItemStack, Integer> returnMap = new HashMap<ItemStack, Integer>();
        for(ItemStack is : playerCache.get(uuid)){
            ItemStack compare = new ItemStack(is.getType(), 1, is.getDurability());
            int points = blockPoints.get(compare);
            compare.setAmount(is.getAmount());
            if(total<=0){
                if(compare.getAmount()>0){
                    returnList.add(compare);
                }
            }
            while(total>0 && compare.getAmount()>0){
                total -= points;
                compare.setAmount(compare.getAmount() - 1);
                if(total<=0){
                    if(compare.getAmount()>0){
                        returnList.add(compare);
                    }
                    break;
                }
            }
        }

        if(total<0){
            for(ItemStack is : blockPoints.keySet()){
                int amt = blockPoints.get(is);
                while(total+amt<=0){
                    total += amt;
                    returnList.add(is);
                }
            }
        }

        playerCache.get(uuid).removeAll(returnList);
        for(ItemStack is : playerCache.get(uuid)) {
            if(is != null && is.getType() != Material.AIR){
                int isAmount = is.getAmount();
                is.setAmount(1);
                ShopItem shopItem = shopItems.get(is);
                if(shopItem.getAmountLeft() + isAmount > shopItem.getMaxcap()){
                    shopItem.setAmountLeft(shopItem.getMaxcap());
                }else{
                    shopItem.setAmountLeft(shopItem.getAmountLeft() + isAmount);
                }
            }
        }
        return returnList;
    }

   /* public ArrayList<ItemStack> calculateRemainder(UUID uuid, int total, int removed){
        ArrayList<ItemStack> returnList = new ArrayList<ItemStack>();
        HashMap<ItemStack, Integer> returnMap = new HashMap<ItemStack, Integer>();
        int remainder = total - removed;
        for(ItemStack is : playerCache.get(uuid)) {
            int points = blockPoints.get(is);
            ShopItem shopItem = shopItems.get(is.getType());
            if(shopItem.getAmountLeft()<=0){
                continue;
            }
            while(shopItem.getAmountLeft()>0 && (remainder - points)>=0){
                if(returnMap.containsKey(is)){
                    returnMap.put(is, returnMap.get(is) + 1);
                }else{
                    returnMap.put(is, 1);
                }
                remainder -= points;
                shopItem.setAmountLeft(shopItem.getAmountLeft()-1);
            }
        }
        for(ItemStack is : returnMap.keySet()) {
            returnList.add(new ItemStack(is.getType(), returnMap.get(is), is.getDurability()));
        }
        return returnList;
    }*/

    public int calculatePoints(Inventory inv){
        int amt = 0;
        for(ItemStack is : inv.getContents()) {
            if(is != null){
                int isAmt = is.getAmount();
                is.setAmount(1);
                if(blockPoints.containsKey(is)){
                    amt += blockPoints.get(is) * isAmt;
                }
                is.setAmount(isAmt);
            }
        }
        return amt;
    }

    public static HashMap<ItemStack, Integer> sortByComparator(HashMap<ItemStack, Integer> unsortMap, final boolean order){

        List<Map.Entry<ItemStack, Integer>> list = new LinkedList<Map.Entry<ItemStack, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<ItemStack, Integer>>(){
            public int compare(HashMap.Entry<ItemStack, Integer> o1,
                               HashMap.Entry<ItemStack, Integer> o2){
                if(order){
                    return o1.getValue().compareTo(o2.getValue());
                }else{
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        HashMap<ItemStack, Integer> sortedMap = new LinkedHashMap<ItemStack, Integer>();
        for(HashMap.Entry<ItemStack, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}