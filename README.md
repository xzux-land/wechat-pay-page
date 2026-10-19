# 微信支付收款网页（固定额度 · Native 扫码支付）

基于 **Spring Boot 3 + 微信支付 APIv2（Native 扫码支付）** 的固定额度收款项目：
用户在前端页面选择固定金额 → 后端下单并返回付款二维码 → 用户微信扫码支付 → 支付结果通过回调 + 轮询双通道确认。

- 后端：Java 17 + Spring Boot 3.2，使用微信官方推荐封装 [WxJava（weixin-java-pay）](https://github.com/Wechat-Group/WxJava)
- 前端：单文件静态 HTML（`index.html`），可直接部署到 **GitHub Pages**
- 金额：固定额度白名单（默认 6 / 18 / 66 / 88 / 188 元），**服务端强制校验**，前端无法篡改

## 架构

```
用户浏览器(前端页面/GitHub Pages)
        │ ①选择金额 POST /api/create-order
        ▼
Spring Boot 后端 ──② Native 下单（APIv2 签名）──▶ 微信支付
        │                                              │
        │ ③返回 code_url + 二维码                        │
        │ ④轮询 GET /api/order-status  ◀── ⑤支付结果回调 ┘
        ▼
前端展示二维码 → 用户扫码 → 微信支付 → 回调通知后端 → 前端轮询确认成功
```

## 目录结构

```
wechat-pay-page/
├── index.html                  # 前端收款页面（可单独部署到 GitHub Pages）
├── pom.xml                     # Maven 工程配置
├── src/main/java/com/example/wechatpay/
│   ├── WechatPayApplication.java
│   ├── config/                 # 微信支付配置与 SDK 装配、CORS
│   ├── controller/             # /api/create-order、/api/order-status、/api/notify
│   ├── service/                # 下单 / 查询 / 回调处理、订单内存存储
│   ├── model/ dto/ exception/  # 模型、DTO、统一异常处理
└── src/main/resources/application.yml   # 商户配置（密钥只在这里）
```

## 一、微信商户准备（一次性）

> 没有商户号时无法完成真实支付，本项目的代码结构已就绪，拿到配置后填进 `application.yml` 即可。

1. **申请商户号**：[微信支付商户平台](https://pay.weixin.qq.com/) 注册并完成主体认证（个人可用「小微商户」/「个体工商户」）。
2. **开通 Native 支付**：商户平台 → 产品中心 → Native 支付 → 开通。
3. **关联 AppID**：商户平台 → 产品中心 → AppID 账号管理 → 关联一个已认证的公众号 / 小程序 / APP 的 AppID。
4. **设置 APIv2 密钥**：商户平台 → 账户中心 → API 安全 → 设置 APIv2 密钥（32 位随机字符串，保存好）。

## 二、本地运行

前置：JDK 17+、Maven 3.6+（或直接用 IDEA 打开项目）。

```bash
# 1. 编辑 src/main/resources/application.yml，填入 app-id / mch-id / mch-key / notify-url
# 2. 启动后端
mvn spring-boot:run
# 3. 打开前端页面（两种方式任选）
#    - 直接双击打开 index.html（后端已在 8080 端口运行，CORS 已放行）
#    - 或 python -m http.server 8081 后访问 http://localhost:8081
```

验证接口（后端启动后）：

```bash
# 非法金额 → 400，服务端白名单拦截
curl -X POST http://localhost:8080/api/create-order -H "Content-Type: application/json" -d '{"amount":0.01}'
# 合法金额 → 返回 outTradeNo + 二维码 base64（未配置真实商户时返回微信侧错误提示）
curl -X POST http://localhost:8080/api/create-order -H "Content-Type: application/json" -d '{"amount":66}'
```

## 三、本地联调支付回调（内网穿透）

微信支付回调必须是**公网 HTTPS** 地址。本地调试用内网穿透工具（任选其一）：

```bash
# cpolar 或 ngrok，把本地 8080 端口映射到公网
cpolar http 8080        # 得到 https://xxxx.cpolar.cn
```

把 `application.yml` 的 `notify-url` 改为 `https://xxxx.cpolar.cn/api/notify`，重启后端即可接收真实回调。

## 四、部署上线

### 后端（任选一台云服务器）

1. 购买云服务器（阿里云 / 腾讯云轻量应用服务器即可）+ 域名，域名解析到服务器并**配置 HTTPS 证书**（微信回调只接受 HTTPS）。
2. 服务器上安装 JDK 17，上传项目，构建并运行：

```bash
mvn clean package -DskipTests
nohup java -jar target/wechat-pay-page-1.0.0.jar > app.log 2>&1 &
```

3. Nginx 反向代理 `https://你的域名/api → localhost:8080`，`notify-url` 填 `https://你的域名/api/notify`。
   - 注意保留真实用户 IP：Nginx 需配置 `X-Forwarded-For`（后端已按该头解析 `spbill_create_ip`）。

### 前端（GitHub Pages）

1. 新建 GitHub 仓库，上传本项目（`index.html` 放在仓库根目录）。
2. 打开仓库 → Settings → Pages → Source 选择 `Deploy from a branch` → 分支 `main` → 目录 `/ (root)` → Save。
3. 访问 `https://你的用户名.github.io/你的仓库名/`，页面即上线。
4. **修改 `index.html` 顶部 `CONFIG.apiBase`** 为你的后端地址（如 `https://api.example.com`），否则前端无法调用后端接口。

> 前端页面也可以不部署 GitHub Pages，直接由后端同源托管：把 `index.html` 复制到
> `src/main/resources/static/` 下，后端启动后访问 `http://你的域名/` 即可（无需改 apiBase，无需 CORS）。

## 五、固定额度配置

金额前后端各有一处，需保持一致：

| 位置 | 配置 |
| --- | --- |
| 前端 `index.html` | `CONFIG.amounts = [6, 18, 66, 88, 188]` |
| 后端 `application.yml` | `wechat.pay.allow-amounts: [6, 18, 66, 88, 188]` |

改金额时两处同步修改。后端为最终校验方：不在白名单内的金额一律拒绝下单。

## 六、安全注意事项

- **密钥只存在于服务端** `application.yml`，严禁写进前端或提交到 GitHub（`.gitignore` 已忽略密钥文件）。
- 金额以**服务端白名单**为准，前端传值不可信。
- 支付回调必须**验签 + 核对金额 + 核对订单号**（本代码已实现）。
- 上线必须使用 **HTTPS**。
- 支付成功以**回调为准**，前端轮询仅用于展示。
- 生产环境建议把内存订单存储（`OrderStore`）替换为 MySQL / Redis，并做订单幂等与超时关单。

## 七、常见问题

| 问题 | 原因与处理 |
| --- | --- |
| 下单报「签名错误 / 商户号不存在」 | 商户号、APIv2 密钥填写错误，或 AppID 未与商户号关联 |
| 一直收不到支付回调 | notify-url 非公网 HTTPS；或未使用内网穿透；检查后端日志是否收到请求 |
| 前端提示跨域错误 | 前端与后端不同源时，需把 `apiBase` 指向后端公网地址（后端 CORS 已放行） |
| 提示金额不合法 | 所选金额不在 `allow-amounts` 白名单内，检查前后端配置是否一致 |
| 支付成功但页面没反应 | 以回调与订单查询接口为准；检查轮询接口返回、浏览器控制台报错 |

## 许可说明

本项目为学习 / 演示用途，接入前请确保符合微信支付平台规则与当地法律法规。
