package me.timlampen.blockmarket;

import org.bukkit.Material;

/**
 * Created by Primary on 3/13/2016.
 */
public class ShopItem{

    Material mat = null;
    String name = "";
    int blockpoints = 0;
    int restock = 0;
    int maxcap = 0;
    boolean reset = false;
    short durability;
    int amountLeft = 0;

    public ShopItem(Material mat, String name, int blockpoints, int restock, int maxcap, boolean reset, short durability){
        this.mat = mat;
        this.durability = durability;
        this.name = name;
        this.blockpoints = blockpoints;
        this.restock = restock;
        this.maxcap = maxcap;
        this.reset = reset;
        amountLeft = restock;
    }

    public Material getMaterial(){
        return mat;
    }

    public String getName(){
        return name;
    }

    public int getBlockpoints(){
        return blockpoints;
    }

    public int getRestock(){
        return restock;
    }

    public int getMaxcap(){
        return maxcap;
    }

    public boolean getReset(){
        return reset;
    }

    public short getDurability(){
        return durability;
    }

    public void setAmountLeft(int amt){
        this.amountLeft = amt;
    }

    public int getAmountLeft(){
        return amountLeft;
    }

}
