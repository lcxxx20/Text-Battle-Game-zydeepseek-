package javawzgdyx.model;

import java.util.List;

/**
 * 可选角色职业。技能原型保存在这里，创建角色时会复制一份，
 * 这样「复活」被移除时不会影响其他对局。
 */
public enum CharacterClass {

    BUSIZHE(
            "不死者", "不",
            "从死亡里回来的人",
            "掌握禁忌之力的不死战士：拖住时间、打出爆发，还留着一张满血复活的底牌。",
            List.of(
                    new Skill("watch", "兔子的怀表", "拨动怀表拖住时间：本回合不造成伤害，回合数 +2。", "辅助"),
                    new Skill("dark", "黑之力", "引动黑之力，造成 0~49 点伤害，上下限差距极大。", "爆发"),
                    new Skill("anchor", "大工的长锚", "挥出长锚，稳定造成 20~29 点伤害。", "重击"),
                    new Skill("revive", "复活", "生命值完全回满。每局只能使用一次。", "禁忌", true)
            )
    ),

    LIJIACHENG(
            "家成", "家",
            "自带节目效果的男人",
            "不讲武德的乐子人：技能伤害忽高忽低，还能靠食油饼给自己续上一口。",
            List.of(
                    new Skill("pants", "脱裤子放屁", "离谱的一击：造成 -40~69 点伤害，负数会给敌人回血。", "混沌"),
                    new Skill("oilcake", "食油饼", "啃一口食油饼，回复 5~14 点生命值（不超过生命上限）。", "回复"),
                    new Skill("involution", "内卷", "开始内卷，稳定造成 40~54 点伤害。", "重击"),
                    new Skill("bark", "大狗叫", "汪汪大叫：造成 -20~59 点伤害，负数可能给敌人回血。", "混沌")
            )
    ),

    ZAYU(
            "杂鱼", "D",
            "只会基础攻击",
            "deepseek 在文字格斗世界里的化身，只会朴实无华地打出一拳。",
            List.of()
    );

    private final String displayName;
    private final String sigil;
    private final String tagline;
    private final String intro;
    private final List<Skill> skills;

    CharacterClass(String displayName, String sigil, String tagline, String intro, List<Skill> skills) {
        this.displayName = displayName;
        this.sigil = sigil;
        this.tagline = tagline;
        this.intro = intro;
        this.skills = skills;
    }

    /** 玩家可以选择的职业（deepseek 不可选）。 */
    public static List<CharacterClass> playable() {
        return List.of(BUSIZHE, LIJIACHENG);
    }

    public Role createRole(String name, char sex) {
        return switch (this) {
            case BUSIZHE -> new Rolebusizhe(name, sex);
            case LIJIACHENG -> new Rolelijiacheng(name, sex);
            case ZAYU -> new Zayu(name, sex);
        };
    }

    public String displayName() {
        return displayName;
    }

    public String sigil() {
        return sigil;
    }

    public String tagline() {
        return tagline;
    }

    public String intro() {
        return intro;
    }

    public List<Skill> skills() {
        return skills;
    }
}
