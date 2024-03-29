package tconstruct.tools;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import tconstruct.library.ActiveToolMod;
import tconstruct.library.tools.ToolCore;
import tconstruct.modifiers.tools.ModSneakDetector;

import java.util.*;

/**
 * Created by LolHens on 18.06.2015.
 */
public class SneakListenerMod extends ActiveToolMod {
    @Override
    public void updateTool(ToolCore tool, ItemStack stack, World world, Entity entity) {
        NBTTagCompound modTag = TinkerModification.getModifierTag(stack, ModSneakDetector.class);
        if (modTag == null) return;

        if (modTag.hasKey("current", new NBTTagCompound().getId())
                && checkModifiersChanged(stack, modTag)) disableSneakModListener(stack, modTag);

        swap(stack.getTagCompound().getCompoundTag("InfiTool"), modTag, entity.isSneaking());
    }

    private boolean checkModifiersChanged(ItemStack stack, NBTTagCompound modTag) {
        NBTTagCompound current = modTag.getCompoundTag("current");

        boolean inverted = current.getBoolean("inverted");

        NBTTagCompound modifiers = current.getCompoundTag("modifiers");

        boolean foundModifier = false;

        for (Map.Entry<String, NBTBase> entry : new HashSet<Map.Entry<String, NBTBase>>(TinkerModification.getModifierTags(stack).entrySet())) {
            if (entry.getKey().equals("SneakDetector")) continue;

            if (!modifiers.hasKey(entry.getKey(), entry.getValue().getId())
                    || !modifiers.getTag(entry.getKey()).equals(entry.getValue())) {
                foundModifier = true;
                applyToSneakListener(stack, entry.getKey(), modTag, inverted);
            }
        }

        return foundModifier;
    }

    private void applyToSneakListener(ItemStack stack, String modifierKey, NBTTagCompound modTag, boolean inverted) {
        NBTTagList toggleList = modTag.getTagList(inverted ? "off" : "on", new NBTTagString().getId());

        new ModSneakDetector(new ItemStack[]{}, inverted).addTooltip(stack, modifierKey);

        toggleList.appendTag(new NBTTagString(modifierKey));
    }

    private void disableSneakModListener(ItemStack stack, NBTTagCompound modTag) {
        int newTmpModifierCount = stack.getTagCompound().getCompoundTag("InfiTool").getInteger("Modifiers");
        int oldTmpModifierCount = modTag.getCompoundTag("current").getInteger("modifierCount");
        int extraModifierCount = modTag.getInteger("extraModifiers");

        int modifierUsage = oldTmpModifierCount - newTmpModifierCount;

        extraModifierCount -= modifierUsage;
        modifierUsage = 0;

        if (extraModifierCount < 0) {
            modifierUsage = 0 - extraModifierCount;
            extraModifierCount = 0;
        }

        int modifierCount = newTmpModifierCount - (extraModifierCount + modifierUsage);

        stack.getTagCompound().getCompoundTag("InfiTool").setInteger("Modifiers", modifierCount);
        modTag.setInteger("extraModifiers", extraModifierCount);

        modTag.removeTag("current");
    }

    private void swap(NBTTagCompound tagCompound, NBTTagCompound modTag, boolean toggle) {
        NBTTagList toggleTagList = modTag.getTagList(toggle ? "off" : "on", new NBTTagString().getId());

        List<String> toggleList = new ArrayList<String>();
        for (int i = 0; i < toggleTagList.tagCount(); i++)
            toggleList.add(toggleTagList.getStringTagAt(i));

        NBTTagCompound swap = modTag.getCompoundTag("swap");

        Map<String, NBTBase> swapTmp = new HashMap<String, NBTBase>();

        for (String key : new HashSet<String>((Set<String>) swap.getKeySet()))
            if (!toggleList.contains(key)) {
                swapTmp.put(key, swap.getTag(key));
                swap.removeTag(key);
            }

        for (String key : toggleList)
            if (tagCompound.hasKey(key)) {
                swap.setTag(key, tagCompound.getTag(key));
                tagCompound.removeTag(key);
            }

        for (Map.Entry<String, NBTBase> entry : swapTmp.entrySet())
            tagCompound.setTag(entry.getKey(), entry.getValue());
    }
}
