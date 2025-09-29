"""
Static Analysis Security Testing (SAST) Modules

This package contains modules for static code analysis and security testing.

Available modules:
- CodeQL: GitHub's semantic code analysis engine
- SonarQube: Code quality and security analysis platform
- Snyk: Vulnerability scanning for dependencies and code
- OpenGrep: Open-source pattern-based static analysis tool
- Bandit: Python-specific security issue identifier
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
STATIC_ANALYSIS_MODULES: List[Type[BaseModule]] = []

def register_module(module_class: Type[BaseModule]):
    """Register a static analysis module"""
    STATIC_ANALYSIS_MODULES.append(module_class)
    return module_class

def get_available_modules() -> List[Type[BaseModule]]:
    """Get all available static analysis modules"""
    return STATIC_ANALYSIS_MODULES.copy()