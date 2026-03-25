"""企业微信群机器人客户端。

通过 Webhook 向企业微信外部群发送消息。
"""

from __future__ import annotations

import logging
from pathlib import Path
from typing import Any

import requests

from .message import (
    FileMessage,
    ImageMessage,
    MarkdownMessage,
    NewsMessage,
    TemplateCardMessage,
    TextMessage,
)

logger = logging.getLogger(__name__)

WEBHOOK_BASE_URL = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send"
UPLOAD_BASE_URL = "https://qyapi.weixin.qq.com/cgi-bin/webhook/upload_media"

MessageType = (
    TextMessage
    | MarkdownMessage
    | ImageMessage
    | NewsMessage
    | FileMessage
    | TemplateCardMessage
)


class WeComBotError(Exception):
    """企业微信机器人 API 异常。"""

    def __init__(self, errcode: int, errmsg: str) -> None:
        self.errcode = errcode
        self.errmsg = errmsg
        super().__init__(f"[{errcode}] {errmsg}")


class WeComBotClient:
    """企业微信群机器人客户端。

    Args:
        key: Webhook URL 中的 key 参数。
        base_url: Webhook 基础 URL，一般无需修改。
        timeout: 请求超时秒数。
    """

    def __init__(
        self,
        key: str,
        base_url: str = WEBHOOK_BASE_URL,
        timeout: int = 10,
    ) -> None:
        if not key:
            raise ValueError("Webhook key 不能为空")
        self.key = key
        self.base_url = base_url
        self.timeout = timeout
        self._session = requests.Session()

    @property
    def webhook_url(self) -> str:
        return f"{self.base_url}?key={self.key}"

    def _upload_url(self, media_type: str = "file") -> str:
        return f"{UPLOAD_BASE_URL}?key={self.key}&type={media_type}"

    def send(self, message: MessageType) -> dict[str, Any]:
        """发送消息到群。

        Args:
            message: 消息对象（TextMessage / MarkdownMessage 等）。

        Returns:
            API 响应 JSON。

        Raises:
            WeComBotError: API 返回非 0 错误码时抛出。
        """
        payload = message.to_dict()
        logger.debug("Sending payload: %s", payload)

        resp = self._session.post(
            self.webhook_url,
            json=payload,
            timeout=self.timeout,
        )
        resp.raise_for_status()
        result = resp.json()

        errcode = result.get("errcode", 0)
        if errcode != 0:
            raise WeComBotError(errcode, result.get("errmsg", "unknown error"))

        logger.info("Message sent successfully: msgtype=%s", payload.get("msgtype"))
        return result

    def send_text(
        self,
        content: str,
        mentioned_list: list[str] | None = None,
        mentioned_mobile_list: list[str] | None = None,
    ) -> dict[str, Any]:
        """发送文本消息的快捷方法。"""
        msg = TextMessage(
            content=content,
            mentioned_list=mentioned_list or [],
            mentioned_mobile_list=mentioned_mobile_list or [],
        )
        return self.send(msg)

    def send_markdown(self, content: str) -> dict[str, Any]:
        """发送 Markdown 消息的快捷方法。"""
        return self.send(MarkdownMessage(content=content))

    def send_image(self, path: str | Path) -> dict[str, Any]:
        """发送图片消息的快捷方法。"""
        return self.send(ImageMessage.from_file(path))

    def upload_file(self, path: str | Path) -> str:
        """上传文件并返回 media_id。

        Args:
            path: 本地文件路径。

        Returns:
            media_id 字符串，可用于构造 FileMessage。
        """
        file_path = Path(path)
        with open(file_path, "rb") as f:
            resp = self._session.post(
                self._upload_url("file"),
                files={"media": (file_path.name, f)},
                timeout=self.timeout,
            )
        resp.raise_for_status()
        result = resp.json()

        errcode = result.get("errcode", 0)
        if errcode != 0:
            raise WeComBotError(errcode, result.get("errmsg", "unknown error"))

        media_id: str = result["media_id"]
        logger.info("File uploaded: %s -> media_id=%s", file_path.name, media_id)
        return media_id

    def send_file(self, path: str | Path) -> dict[str, Any]:
        """上传并发送文件的快捷方法。"""
        media_id = self.upload_file(path)
        return self.send(FileMessage(media_id=media_id))
