package javawzgdyx.model;

import java.util.List;

/**
 * 一次技能结算的结果，供界面层播放动画和书写战斗日志。
 *
 * @param skill       使用的技能
 * @param damage      对目标造成的伤害（可能为负，负数表示给目标回血）
 * @param selfHeal    自己实际回复的生命值
 * @param roundsAdded 自己额外获得的回合数
 * @param revived     本次是否触发了复活
 * @param notes       技能相关的叙事文本
 */
public record SkillResult(
        Skill skill,
        int damage,
        int selfHeal,
        int roundsAdded,
        boolean revived,
        List<String> notes
) {
    public SkillResult {
        notes = List.copyOf(notes);
    }

    public boolean healsEnemy() {
        return damage < 0;
    }
}
