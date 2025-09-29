"""
Masscan Penetration Testing Module

This module uses Masscan for high-speed Internet-wide port scanning.
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


import asyncio
import json
from pathlib import Path
from typing import Dict, Any, List
import subprocess
import logging

from ..base import BaseModule, ModuleMetadata, ModuleFinding, ModuleResult
from . import register_module

logger = logging.getLogger(__name__)


@register_module
class MasscanModule(BaseModule):
    """Masscan high-speed port scanner module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="masscan",
            version="1.3.2",
            description="High-speed Internet-wide port scanner for large-scale network discovery",
            author="FuzzForge Team",
            category="penetration_testing",
            tags=["port-scan", "network", "discovery", "high-speed", "mass-scan"],
            input_schema={
                "type": "object",
                "properties": {
                    "targets": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "List of targets (IP addresses, CIDR ranges, domains)"
                    },
                    "target_file": {
                        "type": "string",
                        "description": "File containing targets to scan"
                    },
                    "ports": {
                        "type": "string",
                        "default": "1-1000",
                        "description": "Port range or specific ports to scan"
                    },
                    "top_ports": {
                        "type": "integer",
                        "description": "Scan top N most common ports"
                    },
                    "rate": {
                        "type": "integer",
                        "default": 1000,
                        "description": "Packet transmission rate (packets/second)"
                    },
                    "max_rate": {
                        "type": "integer",
                        "description": "Maximum packet rate limit"
                    },
                    "connection_timeout": {
                        "type": "integer",
                        "default": 10,
                        "description": "Connection timeout in seconds"
                    },
                    "wait_time": {
                        "type": "integer",
                        "default": 10,
                        "description": "Time to wait for responses (seconds)"
                    },
                    "retries": {
                        "type": "integer",
                        "default": 0,
                        "description": "Number of retries for failed connections"
                    },
                    "randomize_hosts": {
                        "type": "boolean",
                        "default": True,
                        "description": "Randomize host order"
                    },
                    "source_ip": {
                        "type": "string",
                        "description": "Source IP address to use"
                    },
                    "source_port": {
                        "type": "string",
                        "description": "Source port range to use"
                    },
                    "interface": {
                        "type": "string",
                        "description": "Network interface to use"
                    },
                    "router_mac": {
                        "type": "string",
                        "description": "Router MAC address"
                    },
                    "exclude_targets": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Targets to exclude from scanning"
                    },
                    "exclude_file": {
                        "type": "string",
                        "description": "File containing targets to exclude"
                    },
                    "ping": {
                        "type": "boolean",
                        "default": False,
                        "description": "Include ping scan"
                    },
                    "banners": {
                        "type": "boolean",
                        "default": False,
                        "description": "Grab banners from services"
                    },
                    "http_user_agent": {
                        "type": "string",
                        "description": "HTTP User-Agent string for banner grabbing"
                    }
                }
            },
            output_schema={
                "type": "object",
                "properties": {
                    "findings": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "host": {"type": "string"},
                                "port": {"type": "integer"},
                                "protocol": {"type": "string"},
                                "state": {"type": "string"},
                                "banner": {"type": "string"}
                            }
                        }
                    }
                }
            }
        )

    def validate_config(self, config: Dict[str, Any]) -> bool:
        """Validate configuration"""
        targets = config.get("targets", [])
        target_file = config.get("target_file")

        if not targets and not target_file:
            raise ValueError("Either 'targets' or 'target_file' must be specified")

        rate = config.get("rate", 1000)
        if rate <= 0 or rate > 10000000:  # Masscan limit
            raise ValueError("Rate must be between 1 and 10,000,000 packets/second")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """Execute Masscan port scanning"""
        self.start_timer()

        try:
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info("Running Masscan high-speed port scan")

            # Prepare target specification
            target_args = self._prepare_targets(config, workspace)
            if not target_args:
                logger.info("No targets specified for scanning")
                return self.create_result(
                    findings=[],
                    status="success",
                    summary={"total_findings": 0, "targets_scanned": 0}
                )

            # Run Masscan scan
            findings = await self._run_masscan_scan(target_args, config, workspace)

            # Create summary
            target_count = len(config.get("targets", [])) if config.get("targets") else 1
            summary = self._create_summary(findings, target_count)

            logger.info(f"Masscan found {len(findings)} open ports")

            return self.create_result(
                findings=findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"Masscan module failed: {e}")
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    def _prepare_targets(self, config: Dict[str, Any], workspace: Path) -> List[str]:
        """Prepare target arguments for masscan"""
        target_args = []

        # Add targets from list
        targets = config.get("targets", [])
        for target in targets:
            target_args.extend(["-t", target])

        # Add targets from file
        target_file = config.get("target_file")
        if target_file:
            target_path = workspace / target_file
            if target_path.exists():
                target_args.extend(["-iL", str(target_path)])
            else:
                raise FileNotFoundError(f"Target file not found: {target_file}")

        return target_args

    async def _run_masscan_scan(self, target_args: List[str], config: Dict[str, Any], workspace: Path) -> List[ModuleFinding]:
        """Run Masscan scan"""
        findings = []

        try:
            # Build masscan command
            cmd = ["masscan"]

            # Add target arguments
            cmd.extend(target_args)

            # Add port specification
            if config.get("top_ports"):
                # Masscan doesn't have built-in top ports, use common ports
                top_ports = self._get_top_ports(config["top_ports"])
                cmd.extend(["-p", top_ports])
            else:
                ports = config.get("ports", "1-1000")
                cmd.extend(["-p", ports])

            # Add rate limiting
            rate = config.get("rate", 1000)
            cmd.extend(["--rate", str(rate)])

            # Add max rate if specified
            max_rate = config.get("max_rate")
            if max_rate:
                cmd.extend(["--max-rate", str(max_rate)])

            # Add connection timeout
            connection_timeout = config.get("connection_timeout", 10)
            cmd.extend(["--connection-timeout", str(connection_timeout)])

            # Add wait time
            wait_time = config.get("wait_time", 10)
            cmd.extend(["--wait", str(wait_time)])

            # Add retries
            retries = config.get("retries", 0)
            if retries > 0:
                cmd.extend(["--retries", str(retries)])

            # Add randomization
            if config.get("randomize_hosts", True):
                cmd.append("--randomize-hosts")

            # Add source IP
            source_ip = config.get("source_ip")
            if source_ip:
                cmd.extend(["--source-ip", source_ip])

            # Add source port
            source_port = config.get("source_port")
            if source_port:
                cmd.extend(["--source-port", source_port])

            # Add interface
            interface = config.get("interface")
            if interface:
                cmd.extend(["-e", interface])

            # Add router MAC
            router_mac = config.get("router_mac")
            if router_mac:
                cmd.extend(["--router-mac", router_mac])

            # Add exclude targets
            exclude_targets = config.get("exclude_targets", [])
            for exclude in exclude_targets:
                cmd.extend(["--exclude", exclude])

            # Add exclude file
            exclude_file = config.get("exclude_file")
            if exclude_file:
                exclude_path = workspace / exclude_file
                if exclude_path.exists():
                    cmd.extend(["--excludefile", str(exclude_path)])

            # Add ping scan
            if config.get("ping", False):
                cmd.append("--ping")

            # Add banner grabbing
            if config.get("banners", False):
                cmd.append("--banners")

                # Add HTTP User-Agent
                user_agent = config.get("http_user_agent")
                if user_agent:
                    cmd.extend(["--http-user-agent", user_agent])

            # Set output format to JSON
            output_file = workspace / "masscan_results.json"
            cmd.extend(["-oJ", str(output_file)])

            logger.debug(f"Running command: {' '.join(cmd)}")

            # Run masscan
            process = await asyncio.create_subprocess_exec(
                *cmd,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                cwd=workspace
            )

            stdout, stderr = await process.communicate()

            # Parse results from JSON file
            if output_file.exists():
                findings = self._parse_masscan_json(output_file, workspace)
            else:
                # Try to parse stdout if no file was created
                if stdout:
                    findings = self._parse_masscan_output(stdout.decode(), workspace)
                else:
                    error_msg = stderr.decode()
                    logger.error(f"Masscan scan failed: {error_msg}")

        except Exception as e:
            logger.warning(f"Error running Masscan scan: {e}")

        return findings

    def _get_top_ports(self, count: int) -> str:
        """Get top N common ports for masscan"""
        # Common ports based on Nmap's top ports list
        top_ports = [
            80, 23, 443, 21, 22, 25, 53, 110, 111, 995, 993, 143, 993, 995, 587, 465,
            109, 88, 53, 135, 139, 445, 993, 995, 143, 25, 110, 465, 587, 993, 995,
            80, 8080, 443, 8443, 8000, 8888, 8880, 2222, 9999, 3389, 5900, 5901,
            1433, 3306, 5432, 1521, 50000, 1494, 554, 37, 79, 82, 5060, 50030
        ]

        # Take first N unique ports
        selected_ports = list(dict.fromkeys(top_ports))[:count]
        return ",".join(map(str, selected_ports))

    def _parse_masscan_json(self, json_file: Path, workspace: Path) -> List[ModuleFinding]:
        """Parse Masscan JSON output into findings"""
        findings = []

        try:
            with open(json_file, 'r') as f:
                content = f.read().strip()

            # Masscan outputs JSONL format (one JSON object per line)
            for line in content.split('\n'):
                if not line.strip():
                    continue

                try:
                    result = json.loads(line)
                    finding = self._process_masscan_result(result)
                    if finding:
                        findings.append(finding)
                except json.JSONDecodeError:
                    continue

        except Exception as e:
            logger.warning(f"Error parsing Masscan JSON: {e}")

        return findings

    def _parse_masscan_output(self, output: str, workspace: Path) -> List[ModuleFinding]:
        """Parse Masscan text output into findings"""
        findings = []

        try:
            for line in output.split('\n'):
                if not line.strip() or line.startswith('#'):
                    continue

                # Parse format: "open tcp 80 1.2.3.4"
                parts = line.split()
                if len(parts) >= 4 and parts[0] == "open":
                    protocol = parts[1]
                    port = int(parts[2])
                    ip = parts[3]

                    result = {
                        "ip": ip,
                        "ports": [{"port": port, "proto": protocol, "status": "open"}]
                    }

                    finding = self._process_masscan_result(result)
                    if finding:
                        findings.append(finding)

        except Exception as e:
            logger.warning(f"Error parsing Masscan output: {e}")

        return findings

    def _process_masscan_result(self, result: Dict) -> ModuleFinding:
        """Process a single Masscan result into a finding"""
        try:
            ip_address = result.get("ip", "")
            ports_data = result.get("ports", [])

            if not ip_address or not ports_data:
                return None

            # Process first port (Masscan typically reports one port per result)
            port_data = ports_data[0]
            port_number = port_data.get("port", 0)
            protocol = port_data.get("proto", "tcp")
            status = port_data.get("status", "open")
            service = port_data.get("service", {})
            banner = service.get("banner", "") if service else ""

            # Only report open ports
            if status != "open":
                return None

            # Determine severity based on port
            severity = self._get_port_severity(port_number)

            # Get category
            category = self._get_port_category(port_number)

            # Create description
            description = f"Open port {port_number}/{protocol} on {ip_address}"
            if banner:
                description += f" (Banner: {banner[:100]})"

            # Create finding
            finding = self.create_finding(
                title=f"Open Port: {port_number}/{protocol}",
                description=description,
                severity=severity,
                category=category,
                file_path=None,  # Network scan, no file
                recommendation=self._get_port_recommendation(port_number, banner),
                metadata={
                    "host": ip_address,
                    "port": port_number,
                    "protocol": protocol,
                    "status": status,
                    "banner": banner,
                    "service_info": service
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error processing Masscan result: {e}")
            return None

    def _get_port_severity(self, port: int) -> str:
        """Determine severity based on port number"""
        # High risk ports (commonly exploited or sensitive services)
        high_risk_ports = [21, 23, 135, 139, 445, 1433, 1521, 3389, 5900, 6379, 27017]

        # Medium risk ports (network services that could be risky if misconfigured)
        medium_risk_ports = [22, 25, 53, 110, 143, 993, 995, 3306, 5432]

        # Web ports are generally lower risk but still noteworthy
        web_ports = [80, 443, 8080, 8443, 8000, 8888]

        if port in high_risk_ports:
            return "high"
        elif port in medium_risk_ports:
            return "medium"
        elif port in web_ports:
            return "low"
        elif port < 1024:  # Well-known ports
            return "medium"
        else:
            return "low"

    def _get_port_category(self, port: int) -> str:
        """Determine category based on port number"""
        if port in [80, 443, 8080, 8443, 8000, 8888]:
            return "web_services"
        elif port == 22:
            return "remote_access"
        elif port in [20, 21]:
            return "file_transfer"
        elif port in [25, 110, 143, 587, 993, 995]:
            return "email_services"
        elif port in [1433, 3306, 5432, 1521, 27017, 6379]:
            return "database_services"
        elif port == 3389:
            return "remote_desktop"
        elif port == 53:
            return "dns_services"
        elif port in [135, 139, 445]:
            return "windows_services"
        elif port in [23, 5900]:
            return "insecure_protocols"
        else:
            return "network_services"

    def _get_port_recommendation(self, port: int, banner: str) -> str:
        """Generate recommendation based on port and banner"""
        # Port-specific recommendations
        recommendations = {
            21: "FTP service detected. Consider using SFTP instead for secure file transfer.",
            22: "SSH service detected. Ensure strong authentication and key-based access.",
            23: "Telnet service detected. Replace with SSH for secure remote access.",
            25: "SMTP service detected. Ensure proper authentication and encryption.",
            53: "DNS service detected. Verify it's not an open resolver.",
            80: "HTTP service detected. Consider upgrading to HTTPS.",
            110: "POP3 service detected. Consider using secure alternatives like IMAPS.",
            135: "Windows RPC service exposed. Restrict access if not required.",
            139: "NetBIOS service detected. Ensure proper access controls.",
            143: "IMAP service detected. Consider using encrypted IMAPS.",
            445: "SMB service detected. Ensure latest patches and access controls.",
            443: "HTTPS service detected. Verify SSL/TLS configuration.",
            993: "IMAPS service detected. Verify certificate configuration.",
            995: "POP3S service detected. Verify certificate configuration.",
            1433: "SQL Server detected. Ensure strong authentication and network restrictions.",
            1521: "Oracle DB detected. Ensure proper security configuration.",
            3306: "MySQL service detected. Secure with strong passwords and access controls.",
            3389: "RDP service detected. Use strong passwords and consider VPN access.",
            5432: "PostgreSQL detected. Ensure proper authentication and access controls.",
            5900: "VNC service detected. Use strong passwords and encryption.",
            6379: "Redis service detected. Configure authentication and access controls.",
            8080: "HTTP proxy/web service detected. Verify if exposure is intended.",
            8443: "HTTPS service on non-standard port. Verify certificate configuration."
        }

        recommendation = recommendations.get(port, f"Port {port} is open. Verify if this service is required and properly secured.")

        # Add banner-specific advice
        if banner:
            banner_lower = banner.lower()
            if "default" in banner_lower or "admin" in banner_lower:
                recommendation += " Default credentials may be in use - change immediately."
            elif any(version in banner_lower for version in ["1.0", "2.0", "old", "legacy"]):
                recommendation += " Service version appears outdated - consider upgrading."

        return recommendation

    def _create_summary(self, findings: List[ModuleFinding], targets_count: int) -> Dict[str, Any]:
        """Create analysis summary"""
        severity_counts = {"critical": 0, "high": 0, "medium": 0, "low": 0, "info": 0}
        category_counts = {}
        port_counts = {}
        host_counts = {}
        protocol_counts = {"tcp": 0, "udp": 0}

        for finding in findings:
            # Count by severity
            severity_counts[finding.severity] += 1

            # Count by category
            category = finding.category
            category_counts[category] = category_counts.get(category, 0) + 1

            # Count by port
            port = finding.metadata.get("port")
            if port:
                port_counts[port] = port_counts.get(port, 0) + 1

            # Count by host
            host = finding.metadata.get("host", "unknown")
            host_counts[host] = host_counts.get(host, 0) + 1

            # Count by protocol
            protocol = finding.metadata.get("protocol", "tcp")
            if protocol in protocol_counts:
                protocol_counts[protocol] += 1

        return {
            "total_findings": len(findings),
            "targets_scanned": targets_count,
            "severity_counts": severity_counts,
            "category_counts": category_counts,
            "protocol_counts": protocol_counts,
            "unique_hosts": len(host_counts),
            "top_ports": dict(sorted(port_counts.items(), key=lambda x: x[1], reverse=True)[:10]),
            "host_counts": dict(sorted(host_counts.items(), key=lambda x: x[1], reverse=True)[:10])
        }