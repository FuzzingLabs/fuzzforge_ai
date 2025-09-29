"""
Penetration Testing Modules

This package contains modules for penetration testing and vulnerability assessment.

Available modules:
- Nuclei: Fast and customizable vulnerability scanner
- Nmap: Network discovery and security auditing
- Masscan: High-speed Internet-wide port scanner
- SQLMap: Automatic SQL injection detection and exploitation
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
PENETRATION_TESTING_MODULES: List[Type[BaseModule]] = []

def register_module(module_class: Type[BaseModule]):
    """Register a penetration testing module"""
    PENETRATION_TESTING_MODULES.append(module_class)
    return module_class

def get_available_modules() -> List[Type[BaseModule]]:
    """Get all available penetration testing modules"""
    return PENETRATION_TESTING_MODULES.copy()

# Import modules to trigger registration
from .nuclei import NucleiModule
from .nmap import NmapModule
from .masscan import MasscanModule
from .sqlmap import SQLMapModule