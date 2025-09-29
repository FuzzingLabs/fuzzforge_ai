"""
CI/CD Security Modules

This package contains modules for CI/CD pipeline and workflow security testing.

Available modules:
- Zizmor: GitHub Actions workflow security analyzer
"""
# Copyright (c) 2025 FuzzingLabs
#
# Licensed under the Business Source License 1.1 (BSL). See the LICENSE file
# at the root of this repository for details.
#
# After the Change Date (four years from publication), this version of the
# Licensed Work will be made available under the Apache License, Version 2.0.
# See the LICENSE-APACHE file or http://www.apache.org/licenses/LICENSE-2.0
#
# Additional attribution and requirements are provided in the NOTICE file.


from typing import List, Type
from ..base import BaseModule

# Module registry for automatic discovery
CICD_SECURITY_MODULES: List[Type[BaseModule]] = []

def register_module(module_class: Type[BaseModule]):
    """Register a CI/CD security module"""
    CICD_SECURITY_MODULES.append(module_class)
    return module_class

def get_available_modules() -> List[Type[BaseModule]]:
    """Get all available CI/CD security modules"""
    return CICD_SECURITY_MODULES.copy()

# Import modules to trigger registration
from .zizmor import ZizmorModule