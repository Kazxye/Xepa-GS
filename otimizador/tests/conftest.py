from datetime import datetime

import pytest

from fabricas import AGORA


@pytest.fixture
def agora() -> datetime:
    return AGORA
