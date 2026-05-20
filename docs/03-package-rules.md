# 后端包结构规则

## 1. 标准包结构

每个业务模块推荐结构：

```text
com.synapse.admin.<module>
├── interfaces
│   ├── controller
│   ├── request
│   └── response
├── application
│   ├── service
│   ├── command
│   ├── query
│   └── result
├── domain
│   ├── model
│   ├── repository
│   ├── service
│   └── event
└── infrastructure
    └── persistence
        ├── entity
        ├── mapper
        ├── repository
        └── converter
```

## 2. interfaces 层

职责：

- REST Controller
- 请求 DTO
- 响应 VO
- 参数校验

禁止：

- 直接调用 Mapper
- 写业务规则
- 写事务逻辑
- 返回 Entity
- 返回 Domain Model

## 3. application 层

职责：

- 用例编排
- 事务边界
- 权限上下文使用
- 调用 domain/repository
- 调用 audit/cache/message port

允许：

- `@Transactional`
- 调用多个 Repository Port
- 组装返回结果

禁止：

- 直接使用 Mapper
- 拼接 SQL
- 持有 HTTP Request

## 4. domain 层

职责：

- 领域模型
- 领域规则
- Repository Port
- Domain Service
- Domain Event

禁止依赖：

- Spring MVC
- MyBatis-Plus
- RedisTemplate
- Mapper
- Entity

## 5. infrastructure 层

职责：

- MyBatis-Plus Entity
- Mapper
- Repository Adapter
- 外部系统适配
- Redis/MQ/File 实现

允许依赖：

- MyBatis-Plus
- Redis
- Feign/WebClient
- OSS SDK

## 6. DTO 命名规范

| 类型 | 命名 | 所在层 |
|---|---|---|
| 新增请求 | `CreateXxxRequest` | interfaces/request |
| 修改请求 | `UpdateXxxRequest` | interfaces/request |
| 查询请求 | `XxxPageRequest` | interfaces/request |
| 响应对象 | `XxxResponse` | interfaces/response |
| 应用命令 | `CreateXxxCommand` | application/command |
| 应用查询 | `XxxQuery` | application/query |
| 应用结果 | `XxxResult` | application/result |
| 持久化对象 | `XxxEntity` | infrastructure/persistence/entity |
| 领域模型 | `Xxx` | domain/model |

## 7. Converter 规则

Converter 用于模型转换：

```text
Request -> Command
Entity -> Domain
Domain -> Entity
Result -> Response
```

禁止：

- Controller 手写大量字段转换。
- Entity 直接返回前端。
- Domain 直接 JSON 序列化给前端。

## 8. Lombok 规则

允许：

- `@Getter`
- `@Setter`
- `@Builder`
- `@NoArgsConstructor`
- `@AllArgsConstructor`
- `@RequiredArgsConstructor`

谨慎：

- `@Data`：Entity/DTO 可用，Domain Model 谨慎。
- `@EqualsAndHashCode`：必须明确字段。

禁止：

- 在复杂领域模型上滥用 `@Data`。

## 9. Controller 示例

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
class UserController {
    private final UserApplicationService userApplicationService;

    @PostMapping
    public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        CreateUserCommand command = UserRequestConverter.toCommand(request);
        UserResult result = userApplicationService.create(command);
        return ApiResponse.ok(UserResponse.from(result));
    }
}
```

## 10. Repository 示例

```java
public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    User save(User user);
}
```

```java
@Repository
@RequiredArgsConstructor
class MybatisPlusUserRepository implements UserRepository {
    private final UserMapper userMapper;
    private final UserPersistenceConverter converter;
}
```
