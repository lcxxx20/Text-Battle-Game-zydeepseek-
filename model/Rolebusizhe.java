package javawzgdyx.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 不死者：兔子的怀表 / 黑之力 / 大工的长锚 / 复活。
 * 数值区间严格对应原始控制台版本。
 */
public class Rolebusizhe extends Role {

    public Rolebusizhe(String name, char sex) {
        super(name, sex, CharacterClass.BUSIZHE);
    }

    @Override
    public SkillResult useSkill(Skill skill, Role target) {
        int damage = 0;
        int selfHeal = 0;
        int roundsAdded = 0;
        boolean revived = false;
        List<String> notes = new ArrayList<>();

        switch (skill.id()) {
            case "watch" -> {
                addRounds(2);
                roundsAdded = 2;
                notes.add(getName() + " 拨动「兔子的怀表」，时间的指针轻轻一颤。");
            }
            case "dark" -> {
                damage = randomInt(0, 50);
                notes.add(getName() + " 低喝一声，黑之力在掌心炸开。");
            }
            case "anchor" -> {
                damage = randomInt(20, 30);
                notes.add(getName() + " 抡起「大工的长锚」，重重砸下。");
            }
            case "revive" -> {
                if (hasSkill("revive")) {
                    int before = getLife();
                    restoreFullLife();
                    selfHeal = getLife() - before;
                    removeSkill("revive");
                    revived = true;
                    notes.add(getName() + " 触发「复活」，伤痕在金光中完全愈合。");
                } else {
                    // 正常流程中不会出现；保留与原版一致的一次性保护。
                    notes.add("杂鱼，复活已经用过了哦~");
                }
            }
            default -> notes.add("杂鱼，没有这个技能哦~");
        }

        spendRound();
        dealDamage(target, damage);
        return new SkillResult(skill, damage, selfHeal, roundsAdded, revived, notes);
    }
}
