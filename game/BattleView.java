package javawzgdyx.game;

import javawzgdyx.model.Role;
import javawzgdyx.model.Skill;

import java.util.List;

/**
 * 战斗流程对界面的全部要求。
 * BattleController 只依赖这个接口，便于替换界面或在测试中使用假实现。
 */
public interface BattleView {

    @FunctionalInterface
    interface SkillHandler {
        void onSkillChosen(Skill skill);
    }

    @FunctionalInterface
    interface AttackerHandler {
        void onAttackerChosen(Role attacker);
    }

    void beginBattle(Role left, Role right, GameMode mode);

    void setPhase(String text);

    void setActiveRole(Role role);

    void refreshRoles();

    void log(String text, LogTone tone);

    void requestSkillChoice(Role actor, List<Skill> skills, SkillHandler handler);

    void requestAttackerChoice(Role left, Role right, AttackerHandler handler);

    void animateDamage(Role target, int damage);

    void animateHeal(Role target, int amount);

    void endBattle();

    void showResult(boolean victory, String message);
}
