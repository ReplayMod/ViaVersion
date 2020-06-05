package us.myles.ViaVersion.api.minecraft;

public enum Environment {

    NORMAL(0),
    NETHER(-1),
    END(1),
    CUSTOM(Integer.MAX_VALUE);

    private final int id;

    Environment(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Environment getEnvironmentById(int id) {
        switch (id) {
            default:
            case -1:
                return NETHER;
            case 0:
                return NORMAL;
            case 1:
                return END;
        }
    }

    public static Environment getEnvironmentById(String id) {
        switch (id) {
            case "minecraft:the_nether":
                return NETHER;
            case "minecraft:overworld":
                return NORMAL;
            case "minecraft:the_end":
                return END;
            default:
                return CUSTOM;
        }
    }
}
