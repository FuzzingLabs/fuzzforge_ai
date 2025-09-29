"""
Fuzzing Modules

This package contains modules for various fuzzing techniques and tools.

Available modules:
- LibFuzzer: LLVM's coverage-guided fuzzing engine
- AFL++: Advanced American Fuzzy Lop with modern features
- AFL-RS: Rust-based AFL implementation
- Atheris: Python fuzzing engine for finding bugs in Python code
- Cargo Fuzz: Rust fuzzing integration with libFuzzer
- Go-Fuzz: Coverage-guided fuzzing for Go packages
- OSS-Fuzz: Google's continuous fuzzing for open source
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
FUZZING_MODULES: List[Type[BaseModule]] = []

def register_module(module_class: Type[BaseModule]):
    """Register a fuzzing module"""
    FUZZING_MODULES.append(module_class)
    return module_class

def get_available_modules() -> List[Type[BaseModule]]:
    """Get all available fuzzing modules"""
    return FUZZING_MODULES.copy()

# Import modules to trigger registration
from .libfuzzer import LibFuzzerModule
from .aflplusplus import AFLPlusPlusModule
from .aflrs import AFLRSModule
from .atheris import AtherisModule
from .cargo_fuzz import CargoFuzzModule
from .go_fuzz import GoFuzzModule
from .oss_fuzz import OSSFuzzModule