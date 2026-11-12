package javawzgdyx.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 家成：脱裤子放屁 / 食油饼 / 内卷 / 大狗叫。
 * 负数伤害会给敌人回血，这是原版保留的混沌设定。
 */
public class Rolelijiacheng extends Role {

    public Rolelijiacheng(String name, char sex) {
        super(name, sex, CharacterClass.LIJIACHENG);
    }

    @Override
    public SkillResult useSkill(Skill skill, Role target) {
        int damage = 0;
        int selfHeal = 0;
        List<String> notes = new ArrayList<>();

        switch (skill.id()) {
            case "pants" -> {
                damage = randomInt(-40, 70);
                notes.add(getName() + " 使出「脱裤子放屁」，场面一度非常混乱。");
                if (damage < 0) {
                    notes.add("臭气反而给 " + target.getName() + " 回了 " + (-damage) + " 点血……");
                }
            }
            case "oilcake" -> {
                int before = getLife();
                int heal = randomInt(5, 15);
                selfHeal = healSelf(heal);
                if (selfHeal > 0) {
                    notes.add(getName() + " 啃下「食油饼」，回复了 " + selfHeal + " 点生命值。");
                } else if (before > getMaxLife()) {
                    // 正常对局里只有家成之间互相用负伤害技能时才会出现；
                    // 原版的 Math.min 会把溢出的生命压回上限。
                    notes.add(getName() + " 啃下「食油饼」，但生命值早已超过上限，被压回 " + getLife() + "。");
                } else {
                    notes.add(getName() + " 啃下「食油饼」，但生命值已经满了。");
                }
            }
            case "involution" -> {
                damage = randomInt(40, 55);
                notes.add(getName() + " 开始疯狂「内卷」，卷出了一记重拳。");
            }
            case "bark" -> {
                damage = randomInt(-20, 60);
                notes.add(getName() + " 发出「大狗叫」，声浪席卷全场。");
                if (damage < 0) {
                    notes.add("叫声反而给 " + target.getName() + " 回了 " + (-damage) + " 点血……");
                }
            }
            default -> notes.add("杂鱼，没有这个技能哦~");
        }

        spendRound();
        dealDamage(target, damage);
        return new SkillResult(skill, damage, selfHeal, 0, false, notes);
    }
}
