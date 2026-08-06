# 仓库指南

## 项目结构与模块组织

Perspective API Compat 是一个使用 Stonecutter 和 Modstitch 构建的客户端 Minecraft 兼容性模组。共享 Java 代码位于 `src/main/java/io/github/leawind/perspectiveapicompat/`。将可选模组集成放在 `internal/compat/` 下；将启动协调逻辑保持在 `internal/logic/` 中。与加载器无关的服务应放在 `platform/api/` 中，而 Fabric、Forge 和 NeoForge 入口点则保留在各自的 `platform/<loader>/` 包中。元数据模板位于 `src/main/templates/`。

`versions/<version>-<loader>/gradle.properties` 定义每个变体。不要编辑 `versions/*/build/` 下的生成文件；请改为更改共享源代码或模板。

切勿提交 `.env`、凭证、日志或 `run/` 目录。

## 编码风格与兼容性规则

使用 Java 现有的两空格缩进、小写包名、`PascalCase` 类型和 `camelCase` 成员。当附近代码这样做时，用简洁的 `///` 注释记录公共意图。在引用的类可以初始化之前，将每个可选模组引用隔离在已加载模组检查之后。

共享兼容性和逻辑代码只能依赖 `platform.api`；具体加载器代码不得跨越该边界泄露。Mixin 切勿使用 `@Redirect` 或 `@ModifyArgs`；
