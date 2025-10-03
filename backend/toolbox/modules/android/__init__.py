"""
Android Security Modules

This package contains modules for android static code analysis and security testing.

Available modules:
- MobSF: Mobile Security Framework
- Jadx: Dex to Java decompiler
- OpenGrep: Open-source pattern-based static analysis tool
"""

from typing import List, Type
from ..base import BaseModule

# Module registry for automatic discovery
ANDROID_MODULES: List[Type[BaseModule]] = []

def register_module(module_class: Type[BaseModule]):
    """Register a android security module"""
    ANDROID_MODULES.append(module_class)
    return module_class

def get_available_modules() -> List[Type[BaseModule]]:
    """Get all available android modules"""
    return ANDROID_MODULES.copy()