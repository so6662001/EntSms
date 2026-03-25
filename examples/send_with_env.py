"""示例：从 .env 环境变量读取配置并发送消息。

使用前请先在项目根目录创建 .env 文件并设置 WECOM_BOT_KEY。
可参考 .env.example 文件。
"""

from wecom_bot.config import create_client_from_env

client = create_client_from_env()

client.send_text("通过环境变量配置发送的消息 ✅")

client.send_markdown(
    "## 系统状态\n"
    "- CPU: <font color=\"info\">32%</font>\n"
    "- 内存: <font color=\"info\">61%</font>\n"
    "- 磁盘: <font color=\"warning\">85%</font>"
)
