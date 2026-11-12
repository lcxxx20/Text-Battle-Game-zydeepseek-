package javawzgdyx.model;

/**
 * 一个技能在界面上的静态描述。
 * 真正的数值逻辑在 Rolebusizhe / Rolelijiacheng 的 useSkill 中。
 */
public record Skill(String id, String name, String description, String tag, boolean once) {

    public Skill(String id, String name, String description, String tag) {
        this(id, name, description, tag, false);
    }
}
