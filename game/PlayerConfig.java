package javawzgdyx.game;

import javawzgdyx.model.CharacterClass;
import javawzgdyx.model.Role;

import java.util.Objects;

/** 选角界面提交的不可变配置。 */
public record PlayerConfig(CharacterClass characterClass, String name, char sex) {

    public PlayerConfig {
        Objects.requireNonNull(characterClass, "characterClass");
        Objects.requireNonNull(name, "name");
    }

    public Role createRole() {
        return characterClass.createRole(name, sex);
    }
}
