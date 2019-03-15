package us.myles.ViaVersion.sponge.commands;

import lombok.AllArgsConstructor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Identifiable;
import us.myles.ViaVersion.api.command.ViaCommandSender;

import java.util.UUID;

@AllArgsConstructor
public class SpongeCommandSender implements ViaCommandSender {
    private CommandSource source;

    @Override
    public boolean hasPermission(String permission) {
        return source.hasPermission(permission);
    }

    @Override
    public void sendMessage(String msg) {
        source.sendMessage(
                TextSerializers.JSON.deserialize(
                        ComponentSerializer.toString(
                                TextComponent.fromLegacyText(msg) // Hacky way to fix links
                        )
                )
        );
    }

    @Override
    public UUID getUUID() {
        if (source instanceof Identifiable) {
            return ((Identifiable) source).getUniqueId();
        } else {
            return UUID.fromString(getName());
        }

    }

    @Override
    public String getName() {
        return source.getName();
    }
}
