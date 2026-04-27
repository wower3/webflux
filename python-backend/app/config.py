from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    db_host: str = "localhost"
    db_port: int = 3306
    db_user: str = "root"
    db_password: str = "123456"
    db_name: str = "chat_chart"
    db_pool_size: int = 10

    ai_service_url: str = "http://aiml-pub.aisp.test.abc/agent-api/workflow-agent-1-a852be77"
    ai_chat_api: str = "/chatabc/chat"
    ai_timeout: int = 90000

    log_level: str = "DEBUG"

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8"}


settings = Settings()
