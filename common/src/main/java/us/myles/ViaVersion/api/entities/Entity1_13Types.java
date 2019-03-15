package us.myles.ViaVersion.api.entities;

import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import us.myles.ViaVersion.api.Via;


// TODO auto generate 18w11a with PAaaS
public class Entity1_13Types {
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
        // Auto generated

        ENTITY(-1), // abm

        AREA_EFFECT_CLOUD(0, ENTITY), // abk
        ENDER_CRYSTAL(16, ENTITY), // aho
        EVOCATION_FANGS(20, ENTITY), // ala
        XP_ORB(22, ENTITY), // abs
        EYE_OF_ENDER_SIGNAL(23, ENTITY), // alb
        FALLING_BLOCK(24, ENTITY), // aix
        FIREWORKS_ROCKET(25, ENTITY), // alc
        ITEM(32, ENTITY), // aiy
        LLAMA_SPIT(37, ENTITY), // ale
        TNT(55, ENTITY), // aiz
        SHULKER_BULLET(60, ENTITY), // alh
        FISHING_BOBBER(93, ENTITY), // ais

        LIVINGENTITY(-1, ENTITY), // abv
        ARMOR_STAND(1, LIVINGENTITY), // ail
        PLAYER(92, LIVINGENTITY), // aks

        ABSTRACT_INSENTIENT(-1, LIVINGENTITY), // abw
        ENDER_DRAGON(17, ABSTRACT_INSENTIENT), // ahp

        ABSTRACT_CREATURE(-1, ABSTRACT_INSENTIENT), // acd

        ABSTRACT_AGEABLE(-1, ABSTRACT_CREATURE), // abj
        VILLAGER(79, ABSTRACT_AGEABLE), // akn

        // Animals
        ABSTRACT_ANIMAL(-1, ABSTRACT_AGEABLE), // agd
        CHICKEN(7, ABSTRACT_ANIMAL), // age
        COW(9, ABSTRACT_ANIMAL), // agg
        MOOSHROOM(47, COW), // agi
        PIG(51, ABSTRACT_ANIMAL), // agl
        POLAR_BEAR(54, ABSTRACT_ANIMAL), // agm
        RABBIT(56, ABSTRACT_ANIMAL), // ago
        SHEEP(58, ABSTRACT_ANIMAL), // agq
        TURTLE(73, ABSTRACT_ANIMAL), // agv

        ABSTRACT_TAMEABLE_ANIMAL(-1, ABSTRACT_ANIMAL), // acg
        OCELOT(48, ABSTRACT_TAMEABLE_ANIMAL), // agj
        WOLF(86, ABSTRACT_TAMEABLE_ANIMAL), // agy

        ABSTRACT_PARROT(-1, ABSTRACT_TAMEABLE_ANIMAL), // agr
        PARROT(50, ABSTRACT_PARROT), // agk


        // Horses
        ABSTRACT_HORSE(-1, ABSTRACT_ANIMAL), // aha
        CHESTED_HORSE(-1, ABSTRACT_HORSE), // agz
        DONKEY(11, CHESTED_HORSE), // ahb
        MULE(46, CHESTED_HORSE), // ahf
        LLAMA(36, CHESTED_HORSE), // ahe
        HORSE(29, ABSTRACT_HORSE), // ahc
        SKELETON_HORSE(63, ABSTRACT_HORSE), // ahg
        ZOMBIE_HORSE(88, ABSTRACT_HORSE), // ahi

        // Golem
        ABSTRACT_GOLEM(-1, ABSTRACT_CREATURE), // agc
        SNOWMAN(66, ABSTRACT_GOLEM), // ags
        VILLAGER_GOLEM(80, ABSTRACT_GOLEM), // agw
        SHULKER(59, ABSTRACT_GOLEM), // ajx

        // Fish
        ABSTRACT_FISHES(-1, ABSTRACT_CREATURE), // agb
        COD_MOB(8, ABSTRACT_FISHES), // agf
        PUFFER_FISH(52, ABSTRACT_FISHES), // agn
        SALMON_MOB(57, ABSTRACT_FISHES), // agp
        TROPICAL_FISH(72, ABSTRACT_FISHES), // agu


        // Monsters
        ABSTRACT_MONSTER(-1, ABSTRACT_CREATURE), // ajs
        BLAZE(4, ABSTRACT_MONSTER), // ajd
        CREEPER(10, ABSTRACT_MONSTER), // ajf
        ENDERMITE(19, ABSTRACT_MONSTER), // ajj
        ENDERMAN(18, ABSTRACT_MONSTER), // aji
        GIANT(27, ABSTRACT_MONSTER), // ajn
        SILVERFISH(61, ABSTRACT_MONSTER), // ajy
        VEX(78, ABSTRACT_MONSTER), // ake
        WITCH(82, ABSTRACT_MONSTER), // akg
        WITHER(83, ABSTRACT_MONSTER), // aij

        // Illagers
        ABSTRACT_ILLAGER_BASE(-1, ABSTRACT_MONSTER), // ajb
        ABSTRACT_EVO_ILLU_ILLAGER(-1, ABSTRACT_ILLAGER_BASE), // akb
        EVOCATION_ILLAGER(21, ABSTRACT_EVO_ILLU_ILLAGER), // ajl
        ILLUSION_ILLAGER(31, ABSTRACT_EVO_ILLU_ILLAGER), // ajq
        VINDICATION_ILLAGER(81, ABSTRACT_ILLAGER_BASE), // akf

        // Skeletons
        ABSTRACT_SKELETON(-1, ABSTRACT_MONSTER), // ajc
        SKELETON(62, ABSTRACT_SKELETON), // ajz
        STRAY(71, ABSTRACT_SKELETON), // akd
        WITHER_SKELETON(84, ABSTRACT_SKELETON), // akh

        // Guardians
        GUARDIAN(28, ABSTRACT_MONSTER), // ajo
        ELDER_GUARDIAN(15, GUARDIAN), // ajh

        // Spiders
        SPIDER(69, ABSTRACT_MONSTER), // akc
        CAVE_SPIDER(6, SPIDER), // aje

        // Zombies - META CHECKED
        ZOMBIE(87, ABSTRACT_MONSTER), // aki
        DROWNED(14, ZOMBIE), // ajg
        HUSK(30, ZOMBIE), // ajp
        ZOMBIE_PIGMAN(53, ZOMBIE), // aju
        ZOMBIE_VILLAGER(89, ZOMBIE), // akj

        // Flying entities
        ABSTRACT_FLYING(-1, ABSTRACT_INSENTIENT), // abt
        GHAST(26, ABSTRACT_FLYING), // ajm
        PHANTOM(90, ABSTRACT_FLYING), // ajt

        ABSTRACT_AMBIENT(-1, ABSTRACT_INSENTIENT), // afy
        BAT(3, ABSTRACT_AMBIENT), // afz

        ABSTRACT_WATERMOB(-1, ABSTRACT_INSENTIENT), // agx
        SQUID(70, ABSTRACT_WATERMOB), // agt

        // Slimes
        SLIME(64, ABSTRACT_INSENTIENT), // aka
        MAGMA_CUBE(38, SLIME), // ajr

        // Hangable objects
        ABSTRACT_HANGING(-1, ENTITY), // aim
        LEASH_KNOT(35, ABSTRACT_HANGING), // aio
        ITEM_FRAME(33, ABSTRACT_HANGING), // ain
        PAINTING(49, ABSTRACT_HANGING), // aiq

        ABSTRACT_LIGHTNING(-1, ENTITY), // aiu
        LIGHTNING_BOLT(91, ABSTRACT_LIGHTNING), // aiv

        // Arrows
        ABSTRACT_ARROW(-1, ENTITY), // akw
        ARROW(2, ABSTRACT_ARROW), // aky
        SPECTRAL_ARROW(68, ABSTRACT_ARROW), // alk
        TRIDENT(94, ABSTRACT_ARROW), // alq

        // Fireballs
        ABSTRACT_FIREBALL(-1, ENTITY), // akx
        DRAGON_FIREBALL(13, ABSTRACT_FIREBALL), // akz
        FIREBALL(34, ABSTRACT_FIREBALL), // ald
        SMALL_FIREBALL(65, ABSTRACT_FIREBALL), // ali
        WITHER_SKULL(85, ABSTRACT_FIREBALL), // alr

        // Projectiles
        PROJECTILE_ABSTRACT(-1, ENTITY), // all
        SNOWBALL(67, PROJECTILE_ABSTRACT), // alj
        ENDER_PEARL(75, PROJECTILE_ABSTRACT), // aln
        EGG(74, PROJECTILE_ABSTRACT), // alm
        POTION(77, PROJECTILE_ABSTRACT), // alp
        XP_BOTTLE(76, PROJECTILE_ABSTRACT), // alo

        // Vehicles
        MINECART_ABSTRACT(-1, ENTITY), // alt
        CHESTED_MINECART_ABSTRACT(-1, MINECART_ABSTRACT), // alu
        CHEST_MINECART(40, CHESTED_MINECART_ABSTRACT), // alx
        HOPPER_MINECART(43, CHESTED_MINECART_ABSTRACT), // ama
        MINECART(39, MINECART_ABSTRACT), // alw
        FURNACE_MINECART(42, MINECART_ABSTRACT), // alz
        COMMANDBLOCK_MINECART(41, MINECART_ABSTRACT), // aly
        TNT_MINECART(45, MINECART_ABSTRACT), // amc
        SPAWNER_MINECART(44, MINECART_ABSTRACT), // amb
        BOAT(5, ENTITY); // alv


        private final int id;
        private final EntityType parent;

        EntityType(int id) {
            this.id = id;
            this.parent = null;
        }

        public static Optional<EntityType> findById(int id) {
            if (id == -1)  // Check if this is called
                return Optional.absent();

            for (EntityType ent : EntityType.values())
                if (ent.getId() == id)
                    return Optional.of(ent);

            return Optional.absent();
        }

        public boolean is(EntityType... types) {
            for (EntityType type : types)
                if (is(type))
                    return true;
            return false;
        }

        public boolean is(EntityType type) {
            return this == type;
        }

        public boolean isOrHasParent(EntityType type) {
            EntityType parent = this;

            do {
                if (parent.equals(type))
                    return true;

                parent = parent.getParent();
            } while (parent != null);

            return false;
        }
    }

    @AllArgsConstructor
    @Getter
    public enum ObjectTypes {
        BOAT(1, EntityType.BOAT),
        ITEM(2, EntityType.ITEM),
        AREA_EFFECT_CLOUD(3, EntityType.AREA_EFFECT_CLOUD),
        MINECART(10, EntityType.MINECART_ABSTRACT),
        TNT_PRIMED(50, EntityType.TNT),
        ENDER_CRYSTAL(51, EntityType.ENDER_CRYSTAL),
        TIPPED_ARROW(60, EntityType.ARROW),
        SNOWBALL(61, EntityType.SNOWBALL),
        EGG(62, EntityType.EGG),
        FIREBALL(63, EntityType.FIREBALL),
        SMALL_FIREBALL(64, EntityType.SMALL_FIREBALL),
        ENDER_PEARL(65, EntityType.ENDER_PEARL),
        WITHER_SKULL(66, EntityType.WITHER_SKULL),
        SHULKER_BULLET(67, EntityType.SHULKER_BULLET),
        LIAMA_SPIT(68, EntityType.LLAMA_SPIT),
        FALLING_BLOCK(70, EntityType.FALLING_BLOCK),
        ITEM_FRAME(71, EntityType.ITEM_FRAME),
        ENDER_SIGNAL(72, EntityType.EYE_OF_ENDER_SIGNAL),
        POTION(73, EntityType.POTION),
        THROWN_EXP_BOTTLE(75, EntityType.XP_BOTTLE),
        FIREWORK(76, EntityType.FIREWORKS_ROCKET),
        LEASH(77, EntityType.LEASH_KNOT),
        ARMOR_STAND(78, EntityType.ARMOR_STAND),
        EVOCATION_FANGS(79, EntityType.EVOCATION_FANGS),
        FISHIHNG_HOOK(90, EntityType.FISHING_BOBBER),
        SPECTRAL_ARROW(91, EntityType.SPECTRAL_ARROW),
        DRAGON_FIREBALL(93, EntityType.DRAGON_FIREBALL);

        private final int id;
        private final EntityType type;

        public static Optional<ObjectTypes> findById(int id) {
            if (id == -1)
                return Optional.absent();

            for (ObjectTypes ent : ObjectTypes.values())
                if (ent.getId() == id)
                    return Optional.of(ent);

            return Optional.absent();
        }

        public static Optional<EntityType> getPCEntity(int id) {
            Optional<ObjectTypes> output = findById(id);

            if (!output.isPresent())
                return Optional.absent();
            return Optional.of(output.get().getType());
        }
    }
}
