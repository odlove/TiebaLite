# TiebaLite 重构纪要

**时间**：2025-10-13 ~ 2025-10-15
**范围**：Repository 层依赖注入改造、ViewModel 可测试性改造

---

## 1. 本轮完成项

### 1.1 仓库层抽象

- **改造内容**：10 个核心 Repository 从 `object` 单例改为接口 + 实现类，通过 Hilt 注入
- **改造的 Repository**：PbPageRepository、FrsPageRepository、PersonalizedRepository、AddPostRepository、UserInteractionRepository、ForumOperationRepository、NotificationRepository、ContentRecommendRepository、SearchRepository、ThreadOperationRepository
- **ViewModel 改造**：37 个 ViewModel 改为构造函数注入，移除 `TiebaApi.getInstance()` 静态调用
- **新增模块**：ApiModule、RepositoryModule（使用 @Binds 绑定）

**对比**：
```kotlin
// 重构前
object PbPageRepository {
    fun pbPage(...) = TiebaApi.getInstance().pbPageFlow(...)
}

// 重构后
interface PbPageRepository { ... }
class PbPageRepositoryImpl @Inject constructor(private val api: ITiebaApi) : PbPageRepository { ... }
```

### 1.2 协程调度抽象

- **新增**：`DispatcherProvider` 接口，替代 BaseViewModel 中硬编码的 `Dispatchers.IO`
- **效果**：测试时可注入 `StandardTestDispatcher`，使用 `advanceUntilIdle()` 控制协程执行

### 1.3 QuickPreview 注入

- **改造**：QuickPreviewUtil 通过 Hilt EntryPoint 提供，ClipBoardLinkDetector 通过 EntryPoint 获取实例

### 1.4 测试基础设施

- **测试文件**：从 1 个增加到 11 个（新增 9 个：7 个 Repository 测试 + 2 个 ViewModel 测试）
- **测试覆盖率**：从 < 0.3% 提升到约 5%
- **新增工具**：BaseViewModelTest（测试基类）、TestFixtures（简化版）

### 变更统计

| 类别 | 修改 | 新增 | 代码量 |
|-----|------|------|-------|
| Repository | 4 | 16 | ~800 行 |
| ViewModel | 37 | 0 | ~500 行 |
| DI 模块 | 2 | 3 | ~150 行 |
| 测试 | 2 | 9 | ~1200 行 |
| **总计** | **50** | **29** | **~2750 行** |

---

## 2. 后续待办

### 2.1 剩余模块改造（P0）

仍使用 `TiebaApi.getInstance()` 的 ViewModel（7 个）：
- SubPostsViewModel - 楼中楼
- ThreadStoreViewModel - 帖子收藏
- HistoryListViewModel - 历史记录
- UserProfileViewModel - 用户资料
- EditProfileViewModel - 编辑资料
- UserLikeForumViewModel - 关注列表
- UserPostViewModel - 用户发帖

需创建对应的 Repository：SubPostsRepository、ThreadStoreRepository、UserProfileRepository

### 2.2 大文件拆分（P1）

- ThreadPage.kt (2268 行) → 拆分为 PostCard、SubPostItem、ThreadMenu 等组件
- ForumPage.kt (1052 行) → 拆分为 ForumHeader、ThreadListItem 等
- ITiebaApi.kt (1527 行) → 按功能拆分为 ThreadApi、ForumApi、UserApi 等

### 2.3 测试覆盖率提升（P1）

- 短期目标（1 个月）：Repository 40%，ViewModel 20%，总覆盖率 15%
- 中期目标（3 个月）：Repository 60%，ViewModel 40%，总覆盖率 40%

### 2.4 CI 集成

- 在 CI 中加入 `./gradlew testDebugUnitTest`
- PR 合并前必须通过全部测试

---

## 3. 风险 / 注意事项

### 3.1 未覆盖模块

- **问题**：7 个 ViewModel 仍使用静态调用，无法测试
- **风险**：新功能可能延续旧模式，技术债累积
- **措施**：Code Review 严格检查，禁止新增 object Repository

### 3.2 DispatcherProvider 一致性

- **风险场景**：
  - 新增 ViewModel 忘记接受 `dispatcherProvider` 参数
  - 测试中忘记注入测试调度器，导致 `advanceUntilIdle()` 失效
- **措施**：
  - 创建 IDE 代码模板
  - 所有 ViewModel 测试必须继承 BaseViewModelTest

### 3.3 CI 集成要求

- 每次 PR 运行全部单元测试
- 测试失败时 PR 无法合并
- 测试通过率保持 100%

### 3.4 代码审查重点

- 新增 Repository 必须有接口定义
- 新增 ViewModel 必须使用构造函数注入
- 禁止使用 `TiebaApi.getInstance()`（除非在 DI 模块中）
- 每个新增 Repository 必须有测试文件

---

**重构完成**：2025-10-15
