"""企业微信群机器人消息类型定义。

支持的消息类型:
- 文本 (text)
- Markdown (markdown)
- 图片 (image)
- 图文 (news)
- 文件 (file)
- 模板卡片 (template_card)

详见: https://developer.work.weixin.qq.com/document/path/91770
"""

from __future__ import annotations

import base64
import hashlib
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any


@dataclass
class TextMessage:
    """文本消息。

    Args:
        content: 文本内容，最长 2048 字节。
        mentioned_list: 需要 @ 的成员 userid 列表，@all 表示所有人。
        mentioned_mobile_list: 需要 @ 的成员手机号列表，@all 表示所有人。
    """

    content: str
    mentioned_list: list[str] = field(default_factory=list)
    mentioned_mobile_list: list[str] = field(default_factory=list)

    def to_dict(self) -> dict[str, Any]:
        payload: dict[str, Any] = {
            "msgtype": "text",
            "text": {
                "content": self.content,
            },
        }
        if self.mentioned_list:
            payload["text"]["mentioned_list"] = self.mentioned_list
        if self.mentioned_mobile_list:
            payload["text"]["mentioned_mobile_list"] = self.mentioned_mobile_list
        return payload


@dataclass
class MarkdownMessage:
    """Markdown 消息。

    Args:
        content: Markdown 内容，最长 4096 字节。
                 支持的语法子集见企业微信官方文档。
    """

    content: str

    def to_dict(self) -> dict[str, Any]:
        return {
            "msgtype": "markdown",
            "markdown": {
                "content": self.content,
            },
        }


@dataclass
class ImageMessage:
    """图片消息。支持从文件路径或 bytes 构造。

    使用 from_file / from_bytes 类方法创建实例。
    """

    base64_data: str
    md5: str

    @classmethod
    def from_file(cls, path: str | Path) -> ImageMessage:
        data = Path(path).read_bytes()
        return cls.from_bytes(data)

    @classmethod
    def from_bytes(cls, data: bytes) -> ImageMessage:
        return cls(
            base64_data=base64.b64encode(data).decode(),
            md5=hashlib.md5(data).hexdigest(),
        )

    def to_dict(self) -> dict[str, Any]:
        return {
            "msgtype": "image",
            "image": {
                "base64": self.base64_data,
                "md5": self.md5,
            },
        }


@dataclass
class NewsArticle:
    """图文消息中的单篇文章。"""

    title: str
    url: str
    description: str = ""
    picurl: str = ""

    def to_dict(self) -> dict[str, Any]:
        d: dict[str, Any] = {"title": self.title, "url": self.url}
        if self.description:
            d["description"] = self.description
        if self.picurl:
            d["picurl"] = self.picurl
        return d


@dataclass
class NewsMessage:
    """图文消息，最多支持 8 条图文。"""

    articles: list[NewsArticle]

    def __post_init__(self) -> None:
        if len(self.articles) > 8:
            raise ValueError("图文消息最多支持 8 条")

    def to_dict(self) -> dict[str, Any]:
        return {
            "msgtype": "news",
            "news": {
                "articles": [a.to_dict() for a in self.articles],
            },
        }


@dataclass
class FileMessage:
    """文件消息。需要先上传文件获取 media_id。

    Args:
        media_id: 通过文件上传接口获取的 media_id。
    """

    media_id: str

    def to_dict(self) -> dict[str, Any]:
        return {
            "msgtype": "file",
            "file": {
                "media_id": self.media_id,
            },
        }


@dataclass
class TemplateCardMessage:
    """文本通知型模板卡片消息。

    Args:
        card_type: 卡片类型，目前仅支持 "text_notice"。
        source_icon_url: 来源图片 URL。
        source_desc: 来源描述。
        main_title: 主标题。
        sub_title_text: 副标题。
        emphasis_content_title: 关键数据标题。
        emphasis_content_desc: 关键数据描述。
        horizontal_content_list: 二级标题+文本列表。
        jump_list: 跳转列表。
        card_action_url: 整体卡片的点击跳转 URL。
    """

    main_title: str
    card_action_url: str
    card_type: str = "text_notice"
    source_icon_url: str = ""
    source_desc: str = ""
    sub_title_text: str = ""
    emphasis_content_title: str = ""
    emphasis_content_desc: str = ""
    horizontal_content_list: list[dict[str, str]] = field(default_factory=list)
    jump_list: list[dict[str, str]] = field(default_factory=list)

    def to_dict(self) -> dict[str, Any]:
        card: dict[str, Any] = {
            "card_type": self.card_type,
            "main_title": {"title": self.main_title},
            "card_action": {"type": 1, "url": self.card_action_url},
        }
        if self.source_icon_url or self.source_desc:
            card["source"] = {}
            if self.source_icon_url:
                card["source"]["icon_url"] = self.source_icon_url
            if self.source_desc:
                card["source"]["desc"] = self.source_desc
        if self.sub_title_text:
            card["sub_title_text"] = self.sub_title_text
        if self.emphasis_content_title:
            card["emphasis_content"] = {
                "title": self.emphasis_content_title,
                "desc": self.emphasis_content_desc,
            }
        if self.horizontal_content_list:
            card["horizontal_content_list"] = self.horizontal_content_list
        if self.jump_list:
            card["jump_list"] = self.jump_list

        return {
            "msgtype": "template_card",
            "template_card": card,
        }
