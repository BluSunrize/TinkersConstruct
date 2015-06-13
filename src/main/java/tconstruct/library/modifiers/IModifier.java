package tconstruct.library.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IModifier {

  String getIdentifier();

  /**
   * Called with a set of itemstacks and returns a match which contains the items that match
   * and how often the modifier can be applied with them
   */
  RecipeMatch.Match matches(ItemStack[] stacks);

  /** Returns true if the modifier can be applied to the given itemstack */
  boolean canApply(ItemStack stack);

  /** Apply the modifier to that itemstack. The complete procedure */
  void apply(ItemStack stack);

  /**
   * In this function the modifier saves its own data into the given tag.
   * Take a look at the ModifierNBT class for easy handling.
   *
   * @param modifierTag This tag shall be filled with data. It will be saved into the tool as the modifiers identifier.
   */
  void updateNBT(NBTTagCompound modifierTag);

  /**
   * This is the actual bread and butter of the modifier. This function applies the actual effect like adding a trait,
   * increasing miningspeed, etc. It is important that the application is DETERMINISTIC! The result has to solely depend
   * on the NBT and has to give the same result every time. This is needed so the tool can be rebuilt and modifiers
   * reapplied.
   *
   * @param rootCompound The main compound of the item to be modified.
   * @param modifierTag  The same tag as for updateNBT.
   */
  void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag);

  /**
   * Returns the tooltip to display for the given tag of this specific modifier.
   * Color tags are not necessarry.
   */
  String getTooltip(NBTTagCompound modifierTag);

  /** Used for specific modifiers that need a texture variant for each material */
  @SideOnly(Side.CLIENT)
  boolean hasTexturePerMaterial();

}