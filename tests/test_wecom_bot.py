"""企业微信群机器人消息通知单元测试。"""

import hashlib
import json

import pytest
import responses

from wecom_bot import (
    MarkdownMessage,
    NewsArticle,
    NewsMessage,
    TemplateCardMessage,
    TextMessage,
    FileMessage,
    ImageMessage,
)
from wecom_bot.client import WeComBotClient, WeComBotError, WEBHOOK_BASE_URL


TEST_KEY = "test-key-xxxx-xxxx"
WEBHOOK_URL = f"{WEBHOOK_BASE_URL}?key={TEST_KEY}"


# ── 消息序列化测试 ──────────────────────────────────────────────


class TestTextMessage:
    def test_basic(self):
        msg = TextMessage(content="hello")
        d = msg.to_dict()
        assert d["msgtype"] == "text"
        assert d["text"]["content"] == "hello"
        assert "mentioned_list" not in d["text"]

    def test_with_mentions(self):
        msg = TextMessage(
            content="hi",
            mentioned_list=["user1", "@all"],
            mentioned_mobile_list=["13800138000"],
        )
        d = msg.to_dict()
        assert d["text"]["mentioned_list"] == ["user1", "@all"]
        assert d["text"]["mentioned_mobile_list"] == ["13800138000"]


class TestMarkdownMessage:
    def test_basic(self):
        msg = MarkdownMessage(content="# Title\nbody")
        d = msg.to_dict()
        assert d["msgtype"] == "markdown"
        assert "# Title" in d["markdown"]["content"]


class TestImageMessage:
    def test_from_bytes(self):
        data = b"\x89PNG\r\n\x1a\n" + b"\x00" * 100
        msg = ImageMessage.from_bytes(data)
        d = msg.to_dict()
        assert d["msgtype"] == "image"
        assert d["image"]["md5"] == hashlib.md5(data).hexdigest()
        assert len(d["image"]["base64"]) > 0


class TestNewsMessage:
    def test_single_article(self):
        article = NewsArticle(title="Title", url="https://example.com")
        msg = NewsMessage(articles=[article])
        d = msg.to_dict()
        assert d["msgtype"] == "news"
        assert len(d["news"]["articles"]) == 1
        assert d["news"]["articles"][0]["title"] == "Title"

    def test_article_with_optional_fields(self):
        article = NewsArticle(
            title="T",
            url="https://example.com",
            description="Desc",
            picurl="https://example.com/pic.png",
        )
        d = article.to_dict()
        assert d["description"] == "Desc"
        assert d["picurl"] == "https://example.com/pic.png"

    def test_max_articles(self):
        articles = [NewsArticle(title=f"A{i}", url="https://example.com") for i in range(8)]
        msg = NewsMessage(articles=articles)
        assert len(msg.to_dict()["news"]["articles"]) == 8

    def test_too_many_articles_raises(self):
        articles = [NewsArticle(title=f"A{i}", url="https://example.com") for i in range(9)]
        with pytest.raises(ValueError, match="最多支持 8 条"):
            NewsMessage(articles=articles)


class TestFileMessage:
    def test_basic(self):
        msg = FileMessage(media_id="mid_001")
        d = msg.to_dict()
        assert d["msgtype"] == "file"
        assert d["file"]["media_id"] == "mid_001"


class TestTemplateCardMessage:
    def test_minimal(self):
        card = TemplateCardMessage(
            main_title="Alert",
            card_action_url="https://example.com",
        )
        d = card.to_dict()
        assert d["msgtype"] == "template_card"
        tc = d["template_card"]
        assert tc["card_type"] == "text_notice"
        assert tc["main_title"]["title"] == "Alert"

    def test_full(self):
        card = TemplateCardMessage(
            main_title="Alert",
            card_action_url="https://example.com",
            source_desc="Monitor",
            sub_title_text="Sub",
            emphasis_content_title="99",
            emphasis_content_desc="pending",
            horizontal_content_list=[{"keyname": "K", "value": "V"}],
            jump_list=[{"type": 1, "title": "Go", "url": "https://example.com"}],
        )
        d = card.to_dict()
        tc = d["template_card"]
        assert tc["source"]["desc"] == "Monitor"
        assert tc["emphasis_content"]["title"] == "99"
        assert len(tc["horizontal_content_list"]) == 1
        assert len(tc["jump_list"]) == 1


# ── 客户端测试 ──────────────────────────────────────────────────


class TestWeComBotClient:
    def test_empty_key_raises(self):
        with pytest.raises(ValueError, match="不能为空"):
            WeComBotClient(key="")

    def test_webhook_url(self):
        client = WeComBotClient(key=TEST_KEY)
        assert client.webhook_url == WEBHOOK_URL

    @responses.activate
    def test_send_text_success(self):
        responses.add(
            responses.POST,
            WEBHOOK_BASE_URL,
            json={"errcode": 0, "errmsg": "ok"},
            status=200,
        )
        client = WeComBotClient(key=TEST_KEY)
        result = client.send_text("hello")

        assert result["errcode"] == 0
        body = json.loads(responses.calls[0].request.body)
        assert body["msgtype"] == "text"
        assert body["text"]["content"] == "hello"

    @responses.activate
    def test_send_markdown_success(self):
        responses.add(
            responses.POST,
            WEBHOOK_BASE_URL,
            json={"errcode": 0, "errmsg": "ok"},
            status=200,
        )
        client = WeComBotClient(key=TEST_KEY)
        result = client.send_markdown("# Title")
        assert result["errcode"] == 0

    @responses.activate
    def test_send_api_error(self):
        responses.add(
            responses.POST,
            WEBHOOK_BASE_URL,
            json={"errcode": 93000, "errmsg": "invalid webhook url"},
            status=200,
        )
        client = WeComBotClient(key=TEST_KEY)
        with pytest.raises(WeComBotError) as exc_info:
            client.send_text("fail")
        assert exc_info.value.errcode == 93000

    @responses.activate
    def test_send_http_error(self):
        responses.add(
            responses.POST,
            WEBHOOK_BASE_URL,
            body="Server Error",
            status=500,
        )
        client = WeComBotClient(key=TEST_KEY)
        with pytest.raises(Exception):
            client.send_text("fail")

    @responses.activate
    def test_send_news(self):
        responses.add(
            responses.POST,
            WEBHOOK_BASE_URL,
            json={"errcode": 0, "errmsg": "ok"},
            status=200,
        )
        client = WeComBotClient(key=TEST_KEY)
        news = NewsMessage(
            articles=[NewsArticle(title="T", url="https://example.com")]
        )
        result = client.send(news)
        assert result["errcode"] == 0

    @responses.activate
    def test_send_template_card(self):
        responses.add(
            responses.POST,
            WEBHOOK_BASE_URL,
            json={"errcode": 0, "errmsg": "ok"},
            status=200,
        )
        client = WeComBotClient(key=TEST_KEY)
        card = TemplateCardMessage(
            main_title="Test", card_action_url="https://example.com"
        )
        result = client.send(card)
        assert result["errcode"] == 0
        body = json.loads(responses.calls[0].request.body)
        assert body["msgtype"] == "template_card"
