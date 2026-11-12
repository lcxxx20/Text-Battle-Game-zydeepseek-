package javawzgdyx.model;

/**
 * 与原始控制台版本一致的杂鱼敌人 deepseek。
 * 不传性别时随机为男或女。
 */
public class Zayu extends Role {

    public Zayu(String name) {
        this(name, randomInt(0, 2) == 0 ? '女' : '男');
    }

    public Zayu(String name, char sex) {
        super(name, sex, CharacterClass.ZAYU);
    }
}
