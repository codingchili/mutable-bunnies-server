package com.codingchili.instance.model.items;

/**
 * @author Robin Duda
 */
public class ItemType {
    protected Slot slot = Slot.none;
    protected ArmorType armorType = null;
    protected WeaponType weaponType = null;

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    public void setArmorType(ArmorType armorType) {
        this.armorType = armorType;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public void setWeaponType(WeaponType weaponType) {
        this.weaponType = weaponType;
    }
}
