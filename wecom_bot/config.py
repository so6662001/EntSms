"""配置管理，支持从环境变量和 .env 文件加载。"""

from __future__ import annotations

import os

from dotenv import load_dotenv

from .client import WeComBotClient

load_dotenv()


def create_client_from_env(
    key_env: str = "WECOM_BOT_KEY",
    base_url_env: str = "WECOM_WEBHOOK_BASE_URL",
) -> WeComBotClient:
    """从环境变量创建客户端实例。

    Args:
        key_env: 存储 Webhook key 的环境变量名。
        base_url_env: 存储自定义基础 URL 的环境变量名（可选）。

    Returns:
        配置好的 WeComBotClient 实例。

    Raises:
        ValueError: 未设置必需的环境变量时抛出。
    """
    key = os.getenv(key_env)
    if not key:
        raise ValueError(
            f"环境变量 {key_env} 未设置。"
            f"请在 .env 文件或系统环境变量中配置 Webhook key。"
        )

    kwargs: dict = {"key": key}
    base_url = os.getenv(base_url_env)
    if base_url:
        kwargs["base_url"] = base_url

    return WeComBotClient(**kwargs)
