"""
Infrastructure Security Modules

This package contains modules for Infrastructure as Code (IaC) security testing.

Available modules:
- Checkov: Terraform/CloudFormation/Kubernetes IaC security
- Hadolint: Dockerfile security linting and best practices
- Kubesec: Kubernetes security risk analysis
- Polaris: Kubernetes configuration validation
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
INFRASTRUCTURE_MODULES: List[Type[BaseModule]] = []

def register_module(module_class: Type[BaseModule]):
    """Register an infrastructure security module"""
    INFRASTRUCTURE_MODULES.append(module_class)
    return module_class

def get_available_modules() -> List[Type[BaseModule]]:
    """Get all available infrastructure security modules"""
    return INFRASTRUCTURE_MODULES.copy()

# Import modules to trigger registration
from .checkov import CheckovModule
from .hadolint import HadolintModule
from .kubesec import KubesecModule
from .polaris import PolarisModule