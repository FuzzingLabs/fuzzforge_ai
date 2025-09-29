"""
FuzzForge CLI - Command-line interface for FuzzForge security testing platform.

This module provides the main entry point for the FuzzForge CLI application.
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


import typer
from src.fuzzforge_cli.main import app

if __name__ == "__main__":
    app()
