from pydantic import BaseModel, ConfigDict


class LoginRequest(BaseModel):
    model_config = ConfigDict(frozen=True)
    username: str
    password: str


class LoginResponse(BaseModel):
    model_config = ConfigDict(frozen=True)
    token: str
    username: str
