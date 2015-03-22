import com.aaomidi.cowclash.util.ColorEnum;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemBuilder {
    @Getter
    private String type;
    @Getter
    private int count = 1;
    @Getter
    private int durablity = 0;
    @Getter
    private Map<Enchantment, Integer> enchants = null;
    @Getter
    private Color color;
    @Getter
    private String name;
    @Getter
    private List<String> lore;
    // Internal Use Only
    private ItemStack builtItemStack = null;

    /**
     * Will construct an ItemBuilder from a map from a config file.
     * Format:
     * - Type: String
     * Durability: Integer/int. SubType of an item.
     * Enchants: List<String>. Enchantment and it's level will be seperated by ':'
     * Color: String. Will be used if item is of type Leather.
     * Name: String.
     * Lore: List<String>
     *
     * @param map
     */
    public static ItemBuilder getFromMap(Map<?, ?> map) {
        ItemBuilder itemBuilder = new ItemBuilder();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if ((entry.getKey() == null) || (entry.getValue() == null) || (!(entry.getKey() instanceof String))) {
                throw new RuntimeException("Error reading the map file when creating an ItemBuilder.");
            }

            String key = (String) entry.getKey();
            Object value = entry.getValue();

            switch (key.toLowerCase()) {
                case "type": {
                    if (!(value instanceof String)) {
                        throw new RuntimeException("Error reading the map file when creating an ItemBuilder. The value of Type was not of type String");
                    }

                    itemBuilder.setType((String) value);
                    break;
                }
                case "name": {
                    if (!(value instanceof String)) {
                        throw new RuntimeException("Error reading the map file when creating an ItemBuilder. The value of Name was not of type String");
                    }

                    itemBuilder.setName((String) value);
                    break;
                }
                case "color": {
                    if (!(value instanceof String)) {
                        throw new RuntimeException("Error reading the map file when creating an ItemBuilder. The value of Color was not of type String");
                    }

                    itemBuilder.setColor((String) value);
                    break;
                }
                case "lore": {
                    if (!(value instanceof List)) {
                        throw new RuntimeException("Error reading the map file when creating an ItemBuilder. The value of Lore was not of type List");
                    }

                    for (Object o : (List<?>) value) {
                        if (!(o instanceof String)) {
                            throw new RuntimeException("Error reading the map file when creating an ItemBuilder. The value of Lore was not of type List<String>");
                        }
                    }

                    itemBuilder.setLore((List<String>) value);
                    break;
                }
                case "durability": {
                    if (!(value instanceof Integer)) {
                        throw new RuntimeException("Error reading the map file when creating an ItemBuilder. The value of Durability was not of type Integer");
                    }

                    itemBuilder.setDurablity((Integer) value);
                    break;
                }
                case "enchants": {
                    if (!(value instanceof List)) {
                        throw new RuntimeException("Error reading the map file when creating an ItemBuilder. The value of Enchants was not of type List");
                    }

                    for (Object o : (List<?>) value) {
                        if (!(o instanceof String)) {
                            throw new RuntimeException("Error reading the map file when creating an ItemBuilder. The value of Enchants was not of type List<String>");
                        }
                    }

                    Map<Enchantment, Integer> enchantmentMap = new HashMap<>();

                    for (String s : (List<String>) value) {
                        String info[] = s.split(":");

                        if (info.length != 2) {
                            throw new RuntimeException("Error reading the map file when creating an ItemBuilder. The value of Enchants was not separable using ':'.");
                        }

                        String e = info[0];
                        String l = info[1];

                        Enchantment enchantment = Enchantment.getByName(e);
                        Integer level = Integer.getInteger(l);

                        if (enchantment == null || level == null) {
                            throw new RuntimeException("Error reading the map file when creating an ItemBuilder. The value of Enchants did not return an Enchantment or the level was not parsable.");

                        }

                        enchantmentMap.put(enchantment, level);
                    }

                    itemBuilder.setEnchants(enchantmentMap);
                    break;
                }
            }
        }

        return itemBuilder;
    }

    public ItemBuilder setType(String type) {
        this.builtItemStack = null;
        this.type = type;
        return this;
    }

    public ItemBuilder setCount(int count) {
        this.builtItemStack = null;
        this.count = count;
        return this;
    }

    public ItemBuilder setDurablity(int durablity) {
        this.builtItemStack = null;
        this.durablity = durablity;
        return this;
    }

    public ItemBuilder setEnchants(Map<Enchantment, Integer> enchants) {
        this.builtItemStack = null;
        this.enchants = enchants;
        return this;
    }

    public ItemBuilder setColor(String color) {
        this.builtItemStack = null;
        ColorEnum colorEnum = ColorEnum.valueOf(color);
        if (colorEnum != null) {
            this.color = colorEnum.getColor();
        }
        return this;
    }

    public ItemBuilder setName(String name) {
        this.builtItemStack = null;
        this.name = ChatColor.translateAlternateColorCodes('&', name);
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        this.builtItemStack = null;
        // Colorize the lore on entry.
        this.lore = lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList());
        return this;
    }

    /**
     * Builds an ItemStack using information in this ItemBuilder instance.
     *
     * @return
     */
    public ItemStack build() {
        if (this.builtItemStack != null) {
            return this.builtItemStack;
        }

        Material material = Material.getMaterial(type);

        if (material == null) return null;

        ItemStack itemStack = new ItemStack(material, count);
        ItemMeta itemMeta = itemStack.getItemMeta();

        // Set the subtype of the itemstack.
        itemStack.setDurability((short) durablity);

        // Add enchants to the itemstack.
        if (enchants != null) {
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                itemStack.addUnsafeEnchantment(entry.getKey(), entry.getValue());
            }
        }

        // Add name to the itemstack.
        if (name != null) {
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        }

        // Add lore to the itemstack.
        if (lore != null) {
            itemMeta.setLore(lore);
        }

        // Colorize the itemstack.
        if (color != null) {
            if (itemMeta instanceof LeatherArmorMeta) {
                ((LeatherArmorMeta) itemMeta).setColor(color);
            }
        }

        itemStack.setItemMeta(itemMeta);
        this.builtItemStack = itemStack;
        return itemStack;
    }
}
