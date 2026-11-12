package javawzgdyx.game;

import javawzgdyx.model.Role;
import javawzgdyx.model.Skill;
import javawzgdyx.model.SkillResult;

import javax.swing.Timer;
import java.util.List;

/**
 * 战斗流程控制器。
 *
 * <p>与原版 Test.java 的顺序一致：</p>
 * <ul>
 *   <li>模式 1：玩家行动 → 敌人是否死亡 → 玩家回合是否耗尽 → 敌人行动 → 玩家是否死亡</li>
 *   <li>模式 2：自由选择出手者 → 结算 → 防守方是否死亡（原版不检查回合耗尽）</li>
 * </ul>
 *
 * <p>所有流程都跑在 Swing 事件调度线程上，等待画面节奏使用一次性 Swing Timer。</p>
 */
public final class BattleController {

    private final BattleView view;
    private final GameMode mode;
    private final Role left;
    private final Role right;
    private final int pacePercent;

    private Timer timer;
    private boolean active;

    public BattleController(BattleView view, GameMode mode, Role left, Role right) {
        this(view, mode, left, right, 100);
    }

    /** pacePercent < 100 可以缩短动画间隔，仅用于自动化测试。 */
    BattleController(BattleView view, GameMode mode, Role left, Role right, int pacePercent) {
        this.view = view;
        this.mode = mode;
        this.left = left;
        this.right = right;
        this.pacePercent = Math.max(1, pacePercent);
    }

    public void start() {
        if (active) {
            return;
        }
        active = true;
        view.beginBattle(left, right, mode);

        if (mode == GameMode.SOLO) {
            view.setPhase("敌情侦察");
            view.log("=== 敌人出现 ===", LogTone.SYSTEM);
            view.log("杂鱼「" + right.getName() + "」现身。生命值 " + right.getLife()
                    + "，性别：" + right.getSex() + "。", LogTone.ENEMY);
            delay(650, this::beginSoloPlayerTurn);
        } else {
            view.setPhase("准备对战");
            view.log("=== 双人对战开始 ===", LogTone.SYSTEM);
            view.log(left.getName() + " 对阵 " + right.getName() + "，轮流出手。", LogTone.SYSTEM);
            delay(650, this::askAttacker);
        }
    }

    /** 退出本局时调用；之后所有未执行的定时器都会失效。 */
    public void cancel() {
        active = false;
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private void delay(int milliseconds, Runnable action) {
        if (!active) {
            return;
        }
        if (timer != null) {
            timer.stop();
        }
        int scaled = Math.max(1, milliseconds * pacePercent / 100);
        timer = new Timer(scaled, event -> {
            if (active) {
                action.run();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    /* ------------------------------------------------------------------ */
    /* 模式 1：狂扁 deepseek                                               */
    /* ------------------------------------------------------------------ */

    private void beginSoloPlayerTurn() {
        if (!active) {
            return;
        }
        view.setActiveRole(left);
        view.setPhase(left.getName() + " · 选择技能");
        view.log("—— " + left.getName() + " 的回合 ——", LogTone.TURN);
        view.requestSkillChoice(left, List.copyOf(left.getSkills()), this::onSoloSkill);
    }

    private void onSoloSkill(Skill skill) {
        if (!active) {
            return;
        }
        SkillResult result = left.useSkill(skill, right);
        showEffects(left, right, result);
        logTurnResult(left, right, result);

        delay(600, () -> {
            if (!active) {
                return;
            }
            if (right.isDead()) {
                finishSoloWin();
                return;
            }
            if (left.getCount() <= 0) {
                finishSoloLoseByRounds();
                return;
            }
            beginSoloEnemyTurn();
        });
    }

    private void beginSoloEnemyTurn() {
        if (!active) {
            return;
        }
        view.setActiveRole(right);
        view.setPhase(right.getName() + " · 行动中");
        view.log(right.getName() + " 正在行动……", LogTone.MUTED);

        delay(700, () -> {
            int damage = right.basicAttack(left);
            view.refreshRoles();
            if (damage > 0) {
                view.animateDamage(left, damage);
            }
            view.log(right.getName() + " 对你造成了 " + damage + " 点伤害，你还剩 "
                    + Math.max(0, left.getLife()) + " 滴血。", LogTone.ENEMY);

            delay(500, () -> {
                if (!active) {
                    return;
                }
                if (left.isDead()) {
                    finishSoloLose();
                    return;
                }
                beginSoloPlayerTurn();
            });
        });
    }

    /* ------------------------------------------------------------------ */
    /* 模式 2：双人对打                                                    */
    /* ------------------------------------------------------------------ */

    private void askAttacker() {
        if (!active) {
            return;
        }
        view.setActiveRole(null);
        view.setPhase("双人对战 · 选择行动");
        view.requestAttackerChoice(left, right, attacker -> {
            if (!active) {
                return;
            }
            Role defender = attacker == left ? right : left;
            beginDuelTurn(attacker, defender);
        });
    }

    private void beginDuelTurn(Role attacker, Role defender) {
        if (!active) {
            return;
        }
        view.setActiveRole(attacker);
        view.setPhase(attacker.getName() + " · 选择技能");
        view.log("—— " + attacker.getName() + " 的回合 ——", LogTone.TURN);
        view.requestSkillChoice(attacker, List.copyOf(attacker.getSkills()), skill -> onDuelSkill(attacker, defender, skill));
    }

    private void onDuelSkill(Role attacker, Role defender, Skill skill) {
        if (!active) {
            return;
        }
        SkillResult result = attacker.useSkill(skill, defender);
        showEffects(attacker, defender, result);
        logTurnResult(attacker, defender, result);

        delay(600, () -> {
            if (!active) {
                return;
            }
            if (defender.isDead()) {
                finishDuel(attacker, defender);
            } else {
                askAttacker();
            }
        });
    }

    /* ------------------------------------------------------------------ */
    /* 视觉与日志                                                          */
    /* ------------------------------------------------------------------ */

    private void showEffects(Role actor, Role target, SkillResult result) {
        view.refreshRoles();
        if (result.damage() > 0) {
            view.animateDamage(target, result.damage());
        } else if (result.damage() < 0) {
            view.animateHeal(target, -result.damage());
        }
        if (result.selfHeal() > 0) {
            view.animateHeal(actor, result.selfHeal());
        }
    }

    private void logTurnResult(Role actor, Role target, SkillResult result) {
        LogTone noteTone;
        if (result.damage() < 0) {
            noteTone = LogTone.WEIRD;
        } else if (result.selfHeal() > 0 || result.revived()) {
            noteTone = LogTone.HEAL;
        } else {
            noteTone = LogTone.SKILL;
        }
        for (String note : result.notes()) {
            view.log(note, noteTone);
        }

        if (target.isDead()) {
            view.log(actor.getName() + " 击败了 " + target.getName() + "，胜利！", LogTone.GOOD);
        } else {
            if (result.damage() > 0) {
                view.log(actor.getName() + " 造成了 " + result.damage() + " 点伤害。",
                        actor == left ? LogTone.PLAYER : LogTone.ENEMY);
            }
            view.log(target.getName() + " 还有 " + Math.max(0, target.getLife()) + " 滴血。", LogTone.MUTED);
        }
        view.log("还剩下 " + actor.getCount() + " 个回合。",
                actor.getCount() <= 0 ? LogTone.WARN : LogTone.MUTED);
    }

    /* ------------------------------------------------------------------ */
    /* 结算                                                                */
    /* ------------------------------------------------------------------ */

    private void finishSoloWin() {
        stop();
        view.log(right.getName() + " 倒下了，你赢得了这场对决！", LogTone.GOOD);
        view.endBattle();
        view.showResult(true, "你击败了 " + right.getName() + "，赢下了这场对决。");
    }

    private void finishSoloLoseByRounds() {
        stop();
        view.log("回合数耗尽，" + left.getName() + " 失败了……", LogTone.WARN);
        view.endBattle();
        view.showResult(false, "回合数耗尽，" + right.getName() + " 拖垮了你。");
    }

    private void finishSoloLose() {
        stop();
        view.log(left.getName() + " 倒下了，被 " + right.getName() + " 击败。", LogTone.WARN);
        view.endBattle();
        view.showResult(false, "你被 " + right.getName() + " 击败了。");
    }

    private void finishDuel(Role winner, Role loser) {
        stop();
        view.log(loser.getName() + " 倒下了！", LogTone.WARN);
        view.log(winner.getName() + " 获得了最终胜利！", LogTone.GOOD);
        view.endBattle();
        view.showResult(true, loser.getName() + " 倒下了，" + winner.getName() + " 获得了最终胜利。");
    }

    private void stop() {
        active = false;
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }
}
