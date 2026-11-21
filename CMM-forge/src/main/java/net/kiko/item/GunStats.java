package net.kiko.item;

/** Numbers for a working firearm. Ammo is iron nuggets (sneak + right-click reloads). */
public record GunStats(int capacity, float damage, int cooldown, int pellets, float spread,
                       boolean auto, double range, boolean rocket) {

    //                                                    cap  dmg  cd  pel  spread auto   range rocket
    public static final GunStats PISTOL   = new GunStats(12,  6.0F,  8, 1, 0.0F,  false,  64, false);
    public static final GunStats REVOLVER = new GunStats(6,   8.0F, 10, 1, 0.0F,  false,  64, false);
    public static final GunStats MAGNUM   = new GunStats(7,  11.0F, 14, 1, 0.0F,  false,  80, false);
    public static final GunStats MUSKET   = new GunStats(1,  12.0F, 40, 1, 0.01F, false,  64, false);
    public static final GunStats RIFLE    = new GunStats(8,   9.0F, 16, 1, 0.0F,  false, 100, false);
    public static final GunStats SNIPER   = new GunStats(5,  20.0F, 30, 1, 0.0F,  false, 150, false);
    public static final GunStats SHOTGUN  = new GunStats(4,   3.0F, 22, 7, 0.09F, false,  32, false);
    public static final GunStats ASSAULT  = new GunStats(30,  5.0F,  3, 1, 0.02F, true,   80, false);
    public static final GunStats SMG      = new GunStats(30,  3.5F,  2, 1, 0.03F, true,   60, false);
    public static final GunStats LMG      = new GunStats(60,  5.0F,  3, 1, 0.04F, true,   80, false);
    public static final GunStats ROCKET   = new GunStats(1,   0.0F, 60, 1, 0.0F,  false, 100, true);
}
