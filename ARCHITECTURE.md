# 架构说明

本文说明 `javawzgdyx` 的分层、关键类、战斗流程，以及它与原控制台版本的对应关系。

---

## 一、总体结构

```text
src/javawzgdyx/
├─ Main.java
├─ model/
│  ├─ CharacterClass.java
│  ├─ Role.java
│  ├─ Rolebusizhe.java
│  ├─ Rolelijiacheng.java
│  ├─ Zayu.java
│  ├─ Skill.java
│  └─ SkillResult.java
├─ game/
│  ├─ BattleController.java
│  ├─ BattleView.java
│  ├─ GameMode.java
│  ├─ LogTone.java
│  └─ PlayerConfig.java
└─ ui/
   ├─ GameFrame.java
   ├─ MainMenuPanel.java
   ├─ SetupPanel.java
   ├─ BattlePanel.java
   ├─ Theme.java
   └─ components/
      ├─ BackgroundPanel.java
      ├─ BrandBar.java
      ├─ TitleArt.java
      ├─ RoundedButton.java
      ├─ RoundedPanel.java
      ├─ TextLinkButton.java
      ├─ PillLabel.java
      ├─ ModeCard.java
      ├─ ClassCard.java
      ├─ PreviewPanel.java
      ├─ StepPips.java
      ├─ DarkTextField.java
      ├─ SegmentedToggle.java
      ├─ SkillCardButton.java
      ├─ FighterCard.java
      ├─ LogPane.java
      └─ ResultOverlay.java
```

依赖关系：

```text
┌──────────────┐
│      ui      │   页面、事件、自绘、动画
└──────┬───────┘
       │ 使用
       ▼
┌──────────────┐
│     game     │   流程、回合、计时、胜负
└──────┬───────┘
       │ 使用
       ▼
┌──────────────┐
│    model     │   角色、技能、数值规则
└──────────────┘
```

- `ui` 会同时引用 `game` 和 `model`（例如选角界面直接读取 `CharacterClass`）。
- `game` 只依赖 `model`，并依赖自己定义的 `BattleView` 接口。
- `model` 不依赖 Swing，可脱离界面单独测试。

---

## 二、入口与页面切换

### `Main`

```java
SwingUtilities.invokeLater(() -> {
    Theme.install();
    new GameFrame().setVisible(true);
});
```

- 所有 Swing 组件都在事件调度线程（EDT）创建。
- `Theme.install()` 设置系统外观、默认字体和选项面板颜色。

### `GameFrame`

主窗口使用 `BorderLayout`：

```text
┌──────────────────────────────────────────┐
│ BrandBar（固定顶部品牌栏）                │
├──────────────────────────────────────────┤
│ CardLayout screens                       │
│   ├─ menu     MainMenuPanel              │
│   ├─ setup    SetupPanel                 │
│   └─ battle   BattlePanel（每局新建）     │
└──────────────────────────────────────────┘
```

- `screens` 是 `CardLayout` 容器，负责菜单/选角/战斗切换。
- 结算界面是 `JFrame` 的 **glass pane**（`ResultOverlay`），覆盖整个窗口，而不是第三个 Card。
- `GameFrame` 持有当前 `BattleController`；退出、重开或返回菜单时调用 `controller.cancel()`。

### 页面流转

```text
主菜单
 ├─ 模式 01 ──► 选角（单人） ──► 战斗 ──► 结算
 │                                      ├─ 再战一局
 │                                      └─ 返回主菜单
 └─ 模式 02 ──► 选角 A ──► 选角 B ──► 战斗 ──► 结算
```

- 双人模式选角 A 后，`drafts` 保存 A 的配置，进入 B。
- 在选角 B 界面点「返回」时，会返回并恢复 A 的配置。
- 结算「再战一局」会基于 `lastMode` / `lastConfigs` 新建角色实例，因此生命值与出生回合都会重新随机。

---

## 三、模型层

### `CharacterClass`

枚举定义三个职业：

| 枚举 | 显示名 | 可选 | 技能 |
| --- | --- | --- | --- |
| `BUSIZHE` | 不死者 | 是 | 兔子的怀表、黑之力、大工的长锚、复活 |
| `LIJIACHENG` | 家成 | 是 | 脱裤子放屁、食油饼、内卷、大狗叫 |
| `ZAYU` | 杂鱼 | 否 | 无 |

每个职业包含：

- 显示名、徽记字符、定位短语、介绍文本；
- 技能原型列表（`List<Skill>`）；
- `createRole(name, sex)` 工厂方法。

技能原型是不可变列表。`Role` 构造时复制到每个角色自己的 `ArrayList`，所以不死者使用复活后，不会影响其他角色或下一局。

### `Role`

公共数值与行为：

- 生命：`100 + RNG.nextInt(100)`，即 100~199；
- 生命上限 `maxLife`：等于初始生命，之后不改变；
- 初始回合数 `count = 20`；
- `basicAttack(target)`：`randomInt(0, 40)` 即 0~39；
- `healSelf(amount)`：封顶治疗，返回实际回复量；
- `restoreFullLife()`：复活专用；
- `randomInt(minInclusive, maxExclusive)`：对 `Random.nextInt(origin, bound)` 的薄封装。

`getSkills()` 返回只读视图；复活移除通过内部 `removeSkill` 完成。

### 技能执行

`Rolebusizhe.useSkill` / `Rolelijiacheng.useSkill` 都返回 `SkillResult`：

```java
record SkillResult(
    Skill skill,
    int damage,
    int selfHeal,
    int roundsAdded,
    boolean revived,
    List<String> notes
)
```

- `damage` 可以为负；负数表示对方回血。
- `selfHeal` 是实际回复量（已封顶）。
- `notes` 是给日志面板的气氛文本。
- 两个 `useSkill` 最后都统一执行 `spendRound()` 和 `dealDamage(target, damage)`，与原始 `attack(...)` 的结尾一致。

### 关键数值

| 技能 | 实现 | 实际范围 |
| --- | --- | --- |
| 基础攻击 | `randomInt(0, 40)` | 0~39 |
| 兔子的怀表 | `addRounds(2)` 后统一 `spendRound()` | 回合净 +1 |
| 黑之力 | `randomInt(0, 50)` | 0~49 |
| 大工的长锚 | `randomInt(20, 30)` | 20~29 |
| 复活 | 一次，`restoreFullLife()` | 回满 |
| 脱裤子放屁 | `randomInt(-50, 50)` | -50~49 |
| 食油饼 | `healSelf(randomInt(5, 15))` | 回 5~14，封顶 |
| 内卷 | `randomInt(30, 45)` | 30~44 |
| 大狗叫 | `randomInt(-20, 50)` | -20~49 |

`randomInt` 的 `maxExclusive` 语义必须保持，否则最后一档边界会变大 1 点。

---

## 四、战斗控制层

### `BattleView` 契约

`BattleController` 不直接操作 Swing 组件，而是通过 `BattleView` 通知界面：

```java
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
```

- `SkillHandler` / `AttackerHandler` 是选择回调。
- 真正的 Swing 实现是 `BattlePanel`；测试时可以使用记录调用的假界面。
- 这个边界让回合逻辑不依赖窗口是否可见。

### `BattleController` 状态与计时

- 所有流程运行在 EDT。
- 节奏使用一个一次性 `javax.swing.Timer`：
  - 每次 `delay(...)` 先停掉旧 timer，再启动新 timer；
  - 回调执行前再次检查 `active`；
  - `cancel()` / `stop()` 会停止 timer 并令 `active = false`，防止退出后旧回调继续执行。
- 控制器没有独立线程，不需要 `synchronized`。

### 模式 01 流程

```text
start()
  ├─ beginBattle()
  ├─ 打印敌人信息
  └─ delay 650ms
       └─ beginSoloPlayerTurn()
            ├─ requestSkillChoice(玩家)
            └─ onSoloSkill()
                 ├─ useSkill
                 ├─ refreshRoles / 动画 / 日志
                 └─ delay 600ms
                      ├─ 敌人死亡？ → finishSoloWin()
                      ├─ 玩家 count ≤ 0？ → finishSoloLoseByRounds()
                      └─ beginSoloEnemyTurn()
                           ├─ 敌人 basicAttack
                           ├─ 日志 / 受伤动画
                           └─ delay 500ms
                                ├─ 玩家死亡？ → finishSoloLose()
                                └─ beginSoloPlayerTurn()
```

关键细节：

- 先判敌人死亡，再判玩家回合耗尽。
- 敌人回合结束才判玩家死亡。
- 兔子的怀表会让 `count` 净增加，因此「20 回合」是起始值，不是绝对上限。

### 模式 02 流程

```text
start()
  ├─ beginBattle()
  ├─ 双人介绍日志
  └─ delay 650ms
       └─ askAttacker()
            ├─ requestAttackerChoice(左, 右)
            └─ beginDuelTurn(attacker, defender)
                 ├─ requestSkillChoice(attacker)
                 └─ onDuelSkill()
                      ├─ useSkill / 动画 / 日志
                      └─ delay 600ms
                           ├─ 防守方死亡？ → finishDuel(attacker, defender)
                           └─ askAttacker()
```

关键细节：

- 没有回合数耗尽检查。
- `count` 依然会随技能减少并显示在角色卡上，但不参与胜负判定。
- 出手者与防守方由玩家当轮选择，没有强制轮换。

### 结算

- 胜/负文案 + `view.endBattle()` + `view.showResult(...)`。
- `BattlePanel.showResult` 把结果交给 `GameFrame`，后者显示 glass pane 上的 `ResultOverlay`。
- 战斗结束后按钮清空，`requestId` 自增，避免残留点击触发旧回调。

### 防止过期点击

`BattlePanel` 有两层保护：

- `requestId`：每次新的选择请求都自增；按钮回调携带创建时的 token。
- `choosing`：一旦选择生效就置为 `false`，后续点击直接忽略。

这能避免快速双击、动画期间重复点击导致一回合执行两次技能。

---

## 五、界面层

### 主题 `Theme`

集中管理：

- 深色底色、暖金主色、红/绿/青/紫等状态色；
- 字体：正文「微软雅黑」，标题「楷体」，等宽英文「Consolas」；
- 圆角渐变填充、胶囊标签、文本换行、应用图标；
- 文本换行时会避免标点单独出现在行首。

> 注意：中文文本必须使用 `Theme.UI` 系列字体；`Consolas` 不含中文字形，混用会出现方框。当前代码中所有含中文的标签已改用中文字体，`MONO` 仅保留给英文标签。

### 自绘组件

- `BackgroundPanel`：深色渐变 + 径向光晕 + 网格 + 暗角。
- `BrandBar`：印章、品牌名、英文副标、渐变分割线。
- `ModeCard`：主页两个模式卡；悬停高亮，整卡可点击。
- `ClassCard`：选角职业卡；整卡是 `JToggleButton`，含勾选态、技能 chip 和低透明度职业徽记水印。
- `SkillCardButton`：战斗技能卡/出手选择卡；含标题、标签、两行说明。
- `FighterCard`：生命条平滑动画、受击抖动、伤害/回血飘字、当前行动者金色辉光、状态与回合胶囊。
- `LogPane`：`JTextPane` 富文本日志，按 `LogTone` 上色，带「战」字水印和呼吸绿点。
- `ResultOverlay`：铺满窗口的结算遮罩，包含「胜/败」徽记、标题、换行说明和两个操作按钮。

### `BattlePanel`

实现 `BattleView`，同时组织战斗页面：

```text
header：退出本局 / 阶段标题 / 模式胶囊
arena：左角色卡  VS  右角色卡
log：战斗记录
action panel：
  ├─ 标题 + 操作提示
  └─ 技能卡行 / 出手选择行
```

- 左右角色卡分别对应 `left` / `right`。
- 右侧卡固定使用敌方血条颜色（双人模式中也以红色区分 P2）。
- 选择技能时按技能数量创建横向技能卡；选择出手者时创建两张「先手」卡。
- ESC 通过 `WHEN_IN_FOCUSED_WINDOW` 绑定到退出本局。

### 模型到界面的动画

- 控制器先调用 `refreshRoles()` 更新目标生命值，再调用 `animateDamage` / `animateHeal`。
- `FighterCard` 内部用 16ms Timer 让显示生命值向目标值插值，形成平滑血条。
- 伤害/回血飘字和抖动由另一个 33ms Timer 驱动，结束后自动停止。
- 组件从窗口移除时（`removeNotify`）会停止这些 Timer，避免返回菜单后继续重绘。
- `LogPane` 的呼吸绿点同理，在 `addNotify` / `removeNotify` 中启停。

---

## 六、与原控制台版的类映射

| 原文件/方法 | 新实现 | 说明 |
| --- | --- | --- |
| `Role.java` | `model/Role.java` | 生命、上限、回合、基础攻击、随机工具 |
| `Rolebusizhe.java` | `model/Rolebusizhe.java` + `CharacterClass.BUSIZHE` | 技能逻辑与技能原型 |
| `Rolelijiacheng.java` | `model/Rolelijiacheng.java` + `CharacterClass.LIJIACHENG` | 同上 |
| `Zayu.java` | `model/Zayu.java` | 随机性别、无技能杂鱼 |
| `Test.main` 菜单循环 | `ui/MainMenuPanel` + `ui/GameFrame` | 图形化模式选择 |
| `Test.chooseRole` | `ui/SetupPanel` + `PlayerConfig` | 职业、姓名、性别表单 |
| `Test.mode1` 循环 | `game/BattleController` 的 SOLO 分支 | 回合顺序、胜负判定 |
| `Test.mode2` 循环 | `game/BattleController` 的 DUEL 分支 | 自由出手、无回合上限 |
| `Role.attack` 里的控制台输出 | `LogPane` + `BattleView.log` | 彩色日志替代 `System.out` |
| `Role.SC.nextInt()` / `Scanner.next()` | 鼠标点击、技能卡、文本框 | 事件驱动输入 |
| 控制台 `print()` | `FighterCard` / `PreviewPanel` | 角色信息可视化 |
| 控制台胜负文本 | `ResultOverlay` | 胜/败结算遮罩 |

原版 `Test.java` 中的输入循环、`Scanner`、`System.out` 不会进入新项目；新项目只保留规则结果与数值。

---

## 七、扩展方式

### 新增一个技能

1. 在 `CharacterClass` 对应枚举中加入 `new Skill(...)`。
2. 在对应 `Rolexxx.useSkill` 的 `switch` 中增加 `case`。
3. 更新 `SkillCardButton.toneFor`（如果需要新标签颜色）。
4. 如有新日志类型，可扩展 `LogTone` 与 `LogPane.colorFor`。

### 新增一个职业

1. 在 `CharacterClass` 增加枚举项及技能列表。
2. 新建 `Role` 子类，实现 `useSkill`。
3. 在 `CharacterClass.createRole` 的 `switch` 中返回新角色。
4. 在 `Theme.accentFor` 中配置主题色。
5. 如果是可选职业，加入 `CharacterClass.playable()`。
6. 选角界面会自动读取 `playable()` 和职业卡数据，无需改布局。

### 调整节奏

`BattleController` 中的 650 / 700 / 600 / 500 毫秒是表现节奏，不影响规则。修改这些延迟即可；`cancel()` 会保证退出后不会继续执行。

### 新增模式

1. 扩展 `GameMode`（模式标题、标签、玩家数量）。
2. 在 `GameFrame` / `SetupPanel` 中处理人数与页面流程。
3. 在 `BattleController.start()` 中增加分支。
4. 主页增加 `ModeCard`，或在现有模式上增加设置项。

---

## 八、验证

开发期使用的外部测试程序（不包含在交付目录中）覆盖了：

- 模型随机边界与数值精确性；
- 复活的一次性、实例隔离与技能原型不变性；
- 负伤害回血的封顶行为；
- 模式 01 的三种结束方式；
- 模式 02 的自由出手与死亡结算；
- 两档窗口尺寸及真实内容区尺寸（扣除标题栏/边框）的离屏渲染；
- 真实窗口下的单人、双人端到端操作。

最终项目本身不依赖任何测试框架或第三方库，只使用 JDK 17 自带的 Swing / AWT。
