import random
import string
import time

_SUFFIX_CHARS = string.ascii_lowercase + string.digits
_SUFFIX_LENGTH = 6


def new_id() -> str:
    ts = int(time.time() * 1000)
    suffix = "".join(random.choices(_SUFFIX_CHARS, k=_SUFFIX_LENGTH))
    return f"{ts}-{suffix}"
