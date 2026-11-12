package javawzgdyx.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * 角色基类。生命值、生命上限、回合数的规则与原始控制台版本一致：
 * 初始生命值 100~199，初始回合数 20。
 */
public class Role {

    private static final Random RNG = new Random();

    private final String name;
    private final char sex;
    private final CharacterClass characterClass;
    private final int maxLife;
    private final List<Skill> skills;

    private int life;
    private int count;

    protected Role(String name, char sex, CharacterClass characterClass) {
        this.name = Objects.requireNonNull(name, "name");
        this.sex = sex;
        this.characterClass = Objects.requireNonNull(characterClass, "characterClass");
        this.life = 100 + RNG.nextInt(100);
        this.maxLife = this.life;
        this.count = 20;
        this.skills = new ArrayList<>(characterClass.skills());
    }

    /** 普通攻击：0~39 点伤害（对应原版 random.nextInt(0, 40)）。 */
    public int basicAttack(Role target) {
        int damage = randomInt(0, 40);
        target.life -= damage;
        return damage;
    }

    /**
     * 使用技能。基类角色（杂鱼）没有技能，子类会覆盖此方法。
     */
    public SkillResult useSkill(Skill skill, Role target) {
        throw new UnsupportedOperationException(characterClass.displayName() + " 没有可用技能");
    }

    public boolean isDead() {
        return life <= 0;
    }

    public String getName() {
        return name;
    }

    public char getSex() {
        return sex;
    }

    public int getLife() {
        return life;
    }

    public int getMaxLife() {
        return maxLife;
    }

    public int getCount() {
        return count;
    }

    public CharacterClass getCharacterClass() {
        return characterClass;
    }

    /** 返回只读视图；需要移除技能时请使用 removeSkill。 */
    public List<Skill> getSkills() {
        return Collections.unmodifiableList(skills);
    }

    protected static int randomInt(int minInclusive, int maxExclusive) {
        return RNG.nextInt(minInclusive, maxExclusive);
    }

    protected final void addRounds(int amount) {
        count += amount;
    }

    protected final void spendRound() {
        count--;
    }

    protected final void dealDamage(Role target, int damage) {
        target.life -= damage;
    }

    /** 回复生命值，但不超过生命上限；返回实际回复量。 */
    protected final int healSelf(int amount) {
        int before = life;
        life = Math.min(life + amount, maxLife);
        return life - before;
    }

    protected final void restoreFullLife() {
        life = maxLife;
    }

    protected final boolean hasSkill(String id) {
        return skills.stream().anyMatch(skill -> skill.id().equals(id));
    }

    protected final void removeSkill(String id) {
        skills.removeIf(skill -> skill.id().equals(id));
    }
}
