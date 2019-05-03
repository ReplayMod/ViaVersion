package us.myles.ViaVersion.api.entities;

import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import us.myles.ViaVersion.api.Via;

import java.util.HashMap;
import java.util.Map;

// 1.10 Entity / Object ids
public class Entity1_10Types {

    public static EntityType getTypeFromId(int typeID, boolean isObject) {
        Optional<EntityType> type;

        if (isObject)
            type = ObjectTypes.getPCEntity(typeID);
        else
            type = EntityType.findById(typeID);

        if (!type.isPresent()) {
            Via.getPlatform().getLogger().severe("Could not find type id " + typeID + " isObject=" + isObject);
            return EntityType.ENTITY; // Fall back to the basic ENTITY
        }

        return type.get();
    }

    @AllArgsConstructor
    @Getter
    public enum EntityType {
        ENTITY(-1),
        DROPPED_ITEM(1, EntityType.ENTITY),
        EXPERIENCE_ORB(2, EntityType.ENTITY),
        LEASH_HITCH(8, EntityType.ENTITY), // Actually entity hanging but it doesn't make a lot of difference for metadata
        PAINTING(9, EntityType.ENTITY), // Actually entity hanging but it doesn't make a lot of difference for metadata
        ARROW(10, EntityType.ENTITY),
        SNOWBALL(11, EntityType.ENTITY), // Actually EntityProjectile
        FIREBALL(12, EntityType.ENTITY),
        SMALL_FIREBALL(13, EntityType.ENTITY),
        ENDER_PEARL(14, EntityType.ENTITY), // Actually EntityProjectile
        ENDER_SIGNAL(15, EntityType.ENTITY),
        THROWN_EXP_BOTTLE(17, EntityType.ENTITY),
        ITEM_FRAME(18, EntityType.ENTITY), // Actually EntityHanging
        WITHER_SKULL(19, EntityType.ENTITY),
        PRIMED_TNT(20, EntityType.ENTITY),
        FALLING_BLOCK(21, EntityType.ENTITY),
        FIREWORK(22, EntityType.ENTITY),
        TIPPED_ARROW(23, EntityType.ARROW),
        SPECTRAL_ARROW(24, EntityType.ARROW),
        SHULKER_BULLET(25, EntityType.ENTITY),
        DRAGON_FIREBALL(26, EntityType.FIREBALL),

        ENTITY_LIVING(-1, ENTITY),
        ENTITY_INSENTIENT(-1, ENTITY_LIVING),
        ENTITY_AGEABLE(-1, ENTITY_INSENTIENT),
        ENTITY_TAMEABLE_ANIMAL(-1, ENTITY_AGEABLE),
        ENTITY_HUMAN(-1, ENTITY_LIVING),

        ARMOR_STAND(30, EntityType.ENTITY_LIVING),

        // Vehicles
        MINECART_ABSTRACT(-1, ENTITY),
        MINECART_COMMAND(40, MINECART_ABSTRACT),
        BOAT(41, ENTITY),
        MINECART_RIDEABLE(42, MINECART_ABSTRACT),
        MINECART_CHEST(43, MINECART_ABSTRACT),
        MINECART_FURNACE(44, MINECART_ABSTRACT),
        MINECART_TNT(45, MINECART_ABSTRACT),
        MINECART_HOPPER(46, MINECART_ABSTRACT),
        MINECART_MOB_SPAWNER(47, MINECART_ABSTRACT),

        CREEPER(50, ENTITY_INSENTIENT),
        SKELETON(51, ENTITY_INSENTIENT),
        SPIDER(52, ENTITY_INSENTIENT),
        GIANT(53, ENTITY_INSENTIENT),
        ZOMBIE(54, ENTITY_INSENTIENT),
        SLIME(55, ENTITY_INSENTIENT),
        GHAST(56, ENTITY_INSENTIENT),
        PIG_ZOMBIE(57, ZOMBIE),
        ENDERMAN(58, ENTITY_INSENTIENT),
        CAVE_SPIDER(59, SPIDER),
        SILVERFISH(60, ENTITY_INSENTIENT),
        BLAZE(61, ENTITY_INSENTIENT),
        MAGMA_CUBE(62, SLIME),
        ENDER_DRAGON(63, ENTITY_INSENTIENT),
        WITHER(64, ENTITY_INSENTIENT),
        BAT(65, ENTITY_INSENTIENT),
        WITCH(66, ENTITY_INSENTIENT),
        ENDERMITE(67, ENTITY_INSENTIENT),
        GUARDIAN(68, ENTITY_INSENTIENT),
        IRON_GOLEM(99, ENTITY_INSENTIENT), // moved up to avoid illegal forward references
        SHULKER(69, EntityType.IRON_GOLEM),
        PIG(90, ENTITY_AGEABLE),
        SHEEP(91, ENTITY_AGEABLE),
        COW(92, ENTITY_AGEABLE),
        CHICKEN(93, ENTITY_AGEABLE),
        SQUID(94, ENTITY_INSENTIENT),
        WOLF(95, ENTITY_TAMEABLE_ANIMAL),
        MUSHROOM_COW(96, COW),
        SNOWMAN(97, EntityType.IRON_GOLEM),
        OCELOT(98, ENTITY_TAMEABLE_ANIMAL),
        HORSE(100, ENTITY_AGEABLE),
        RABBIT(101, ENTITY_AGEABLE),
        POLAR_BEAR(102, ENTITY_AGEABLE),
        VILLAGER(120, ENTITY_AGEABLE),
        ENDER_CRYSTAL(200, ENTITY),
        SPLASH_POTION(-1, ENTITY),
        LINGERING_POTION(-1, SPLASH_POTION),
        AREA_EFFECT_CLOUD(-1, ENTITY),
        EGG(-1, ENTITY),
        FISHING_HOOK(-1, ENTITY),
        LIGHTNING(-1, ENTITY),
        WEATHER(-1, ENTITY),
        PLAYER(-1, ENTITY_HUMAN),
        COMPLEX_PART(-1, ENTITY);

        private static final Map<Integer, EntityType> TYPES = new HashMap<>();

        private final int id;
        private final EntityType parent;

        EntityType(int id) {
            this.id = id;
            this.parent = null;
        }

        static {
            for (EntityType type : EntityType.values()) {
                TYPES.put(type.id, type);
            }
        }

        public static Optional<EntityType> findById(int id) {
            if (id == -1)  // Check if this is called
                return Optional.absent();
            return Optional.fromNullable(TYPES.get(id));
        }
    }

    @AllArgsConstructor
    @Getter
    public enum ObjectTypes {
        BOAT(1, EntityType.BOAT),
        ITEM(2, EntityType.DROPPED_ITEM),
        AREA_EFFECT_CLOUD(3, EntityType.AREA_EFFECT_CLOUD),
        MINECART(10, EntityType.MINECART_RIDEABLE),
        TNT_PRIMED(50, EntityType.PRIMED_TNT),
        ENDER_CRYSTAL(51, EntityType.ENDER_CRYSTAL),
        TIPPED_ARROW(60, EntityType.TIPPED_ARROW),
        SNOWBALL(61, EntityType.SNOWBALL),
        EGG(62, EntityType.EGG),
        FIREBALL(63, EntityType.FIREBALL),
        SMALL_FIREBALL(64, EntityType.SMALL_FIREBALL),
        ENDER_PEARL(65, EntityType.ENDER_PEARL),
        WITHER_SKULL(66, EntityType.WITHER_SKULL),
        SHULKER_BULLET(67, EntityType.SHULKER_BULLET),
        FALLING_BLOCK(70, EntityType.FALLING_BLOCK),
        ITEM_FRAME(71, EntityType.ITEM_FRAME),
        ENDER_SIGNAL(72, EntityType.ENDER_SIGNAL),
        POTION(73, EntityType.SPLASH_POTION),
        THROWN_EXP_BOTTLE(75, EntityType.THROWN_EXP_BOTTLE),
        FIREWORK(76, EntityType.FIREWORK),
        LEASH(77, EntityType.LEASH_HITCH),
        ARMOR_STAND(78, EntityType.ARMOR_STAND),
        FISHIHNG_HOOK(90, EntityType.FISHING_HOOK),
        SPECTRAL_ARROW(91, EntityType.SPECTRAL_ARROW),
        DRAGON_FIREBALL(93, EntityType.DRAGON_FIREBALL);

        private static final Map<Integer, ObjectTypes> TYPES = new HashMap<>();

        private final int id;
        private final EntityType type;

        static {
            for (ObjectTypes type : ObjectTypes.values()) {
                TYPES.put(type.id, type);
            }
        }

        public static Optional<ObjectTypes> findById(int id) {
            if (id == -1)
                return Optional.absent();
            return Optional.fromNullable(TYPES.get(id));
        }

        public static Optional<EntityType> getPCEntity(int id) {
            Optional<ObjectTypes> output = findById(id);

            if (!output.isPresent())
                return Optional.absent();
            return Optional.of(output.get().getType());
        }
    }
}
