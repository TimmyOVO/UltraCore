package com.github.skystardust.ultracore.nukkit.models.item;

import cn.nukkit.item.Item;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemStack {
    private int id;

    public static ItemStack valueOf(Item item) {
        return new ItemStack(item.getId());
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public Item toItem() {
        return new Item(id);
    }
}
