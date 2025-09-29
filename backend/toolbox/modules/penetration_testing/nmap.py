"""
Nmap Penetration Testing Module

This module uses Nmap for network discovery, port scanning, and security auditing.
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
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Dict, Any, List
import subprocess
import logging

from ..base import BaseModule, ModuleMetadata, ModuleFinding, ModuleResult
from . import register_module

logger = logging.getLogger(__name__)


@register_module
class NmapModule(BaseModule):
    """Nmap network discovery and security auditing module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="nmap",
            version="7.94",
            description="Network discovery and security auditing using Nmap",
            author="FuzzForge Team",
            category="penetration_testing",
            tags=["network", "port-scan", "discovery", "security-audit", "service-detection"],
            input_schema={
                "type": "object",
                "properties": {
                    "targets": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "List of targets (IP addresses, domains, CIDR ranges)"
                    },
                    "target_file": {
                        "type": "string",
                        "description": "File containing targets to scan"
                    },
                    "scan_type": {
                        "type": "string",
                        "enum": ["syn", "tcp", "udp", "ack", "window", "maimon"],
                        "default": "syn",
                        "description": "Type of scan to perform"
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
                    "service_detection": {
                        "type": "boolean",
                        "default": True,
                        "description": "Enable service version detection"
                    },
                    "os_detection": {
                        "type": "boolean",
                        "default": False,
                        "description": "Enable OS detection (requires root)"
                    },
                    "script_scan": {
                        "type": "boolean",
                        "default": True,
                        "description": "Enable default NSE scripts"
                    },
                    "scripts": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Specific NSE scripts to run"
                    },
                    "script_categories": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "NSE script categories to run (safe, vuln, etc.)"
                    },
                    "timing_template": {
                        "type": "string",
                        "enum": ["paranoid", "sneaky", "polite", "normal", "aggressive", "insane"],
                        "default": "normal",
                        "description": "Timing template (0-5)"
                    },
                    "max_retries": {
                        "type": "integer",
                        "default": 1,
                        "description": "Maximum number of retries"
                    },
                    "host_timeout": {
                        "type": "integer",
                        "default": 300,
                        "description": "Host timeout in seconds"
                    },
                    "min_rate": {
                        "type": "integer",
                        "description": "Minimum packet rate (packets/second)"
                    },
                    "max_rate": {
                        "type": "integer",
                        "description": "Maximum packet rate (packets/second)"
                    },
                    "stealth": {
                        "type": "boolean",
                        "default": False,
                        "description": "Enable stealth scanning options"
                    },
                    "skip_discovery": {
                        "type": "boolean",
                        "default": False,
                        "description": "Skip host discovery (treat all as online)"
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
                                "service": {"type": "string"},
                                "state": {"type": "string"},
                                "version": {"type": "string"}
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

        scan_type = config.get("scan_type", "syn")
        valid_scan_types = ["syn", "tcp", "udp", "ack", "window", "maimon"]
        if scan_type not in valid_scan_types:
            raise ValueError(f"Invalid scan type: {scan_type}. Valid: {valid_scan_types}")

        timing = config.get("timing_template", "normal")
        valid_timings = ["paranoid", "sneaky", "polite", "normal", "aggressive", "insane"]
        if timing not in valid_timings:
            raise ValueError(f"Invalid timing template: {timing}. Valid: {valid_timings}")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """Execute Nmap network scanning"""
        self.start_timer()

        try:
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info("Running Nmap network scan")

            # Prepare target file
            target_file = await self._prepare_targets(config, workspace)
            if not target_file:
                logger.info("No targets specified for scanning")
                return self.create_result(
                    findings=[],
                    status="success",
                    summary={"total_findings": 0, "hosts_scanned": 0}
                )

            # Run Nmap scan
            findings = await self._run_nmap_scan(target_file, config, workspace)

            # Create summary
            target_count = len(config.get("targets", [])) if config.get("targets") else 1
            summary = self._create_summary(findings, target_count)

            logger.info(f"Nmap found {len(findings)} results")

            return self.create_result(
                findings=findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"Nmap module failed: {e}")
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    async def _prepare_targets(self, config: Dict[str, Any], workspace: Path) -> Path:
        """Prepare target file for scanning"""
        targets = config.get("targets", [])
        target_file = config.get("target_file")

        if target_file:
            # Use existing target file
            target_path = workspace / target_file
            if target_path.exists():
                return target_path
            else:
                raise FileNotFoundError(f"Target file not found: {target_file}")

        if targets:
            # Create temporary target file
            target_path = workspace / "nmap_targets.txt"
            with open(target_path, 'w') as f:
                for target in targets:
                    f.write(f"{target}\n")
            return target_path

        return None

    async def _run_nmap_scan(self, target_file: Path, config: Dict[str, Any], workspace: Path) -> List[ModuleFinding]:
        """Run Nmap scan"""
        findings = []

        try:
            # Build nmap command
            cmd = ["nmap"]

            # Add scan type
            scan_type = config.get("scan_type", "syn")
            scan_type_map = {
                "syn": "-sS",
                "tcp": "-sT",
                "udp": "-sU",
                "ack": "-sA",
                "window": "-sW",
                "maimon": "-sM"
            }
            cmd.append(scan_type_map[scan_type])

            # Add port specification
            if config.get("top_ports"):
                cmd.extend(["--top-ports", str(config["top_ports"])])
            else:
                ports = config.get("ports", "1-1000")
                cmd.extend(["-p", ports])

            # Add service detection
            if config.get("service_detection", True):
                cmd.append("-sV")

            # Add OS detection
            if config.get("os_detection", False):
                cmd.append("-O")

            # Add script scanning
            if config.get("script_scan", True):
                cmd.append("-sC")

            # Add specific scripts
            scripts = config.get("scripts", [])
            if scripts:
                cmd.extend(["--script", ",".join(scripts)])

            # Add script categories
            script_categories = config.get("script_categories", [])
            if script_categories:
                cmd.extend(["--script", ",".join(script_categories)])

            # Add timing template
            timing = config.get("timing_template", "normal")
            timing_map = {
                "paranoid": "-T0",
                "sneaky": "-T1",
                "polite": "-T2",
                "normal": "-T3",
                "aggressive": "-T4",
                "insane": "-T5"
            }
            cmd.append(timing_map[timing])

            # Add retry options
            max_retries = config.get("max_retries", 1)
            cmd.extend(["--max-retries", str(max_retries)])

            # Add timeout
            host_timeout = config.get("host_timeout", 300)
            cmd.extend(["--host-timeout", f"{host_timeout}s"])

            # Add rate limiting
            if config.get("min_rate"):
                cmd.extend(["--min-rate", str(config["min_rate"])])

            if config.get("max_rate"):
                cmd.extend(["--max-rate", str(config["max_rate"])])

            # Add stealth options
            if config.get("stealth", False):
                cmd.extend(["-f", "--randomize-hosts"])

            # Skip host discovery if requested
            if config.get("skip_discovery", False):
                cmd.append("-Pn")

            # Add output format
            output_file = workspace / "nmap_results.xml"
            cmd.extend(["-oX", str(output_file)])

            # Add targets from file
            cmd.extend(["-iL", str(target_file)])

            # Add verbose and reason flags
            cmd.extend(["-v", "--reason"])

            logger.debug(f"Running command: {' '.join(cmd)}")

            # Run nmap
            process = await asyncio.create_subprocess_exec(
                *cmd,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                cwd=workspace
            )

            stdout, stderr = await process.communicate()

            # Parse results from XML file
            if output_file.exists():
                findings = self._parse_nmap_xml(output_file, workspace)
            else:
                error_msg = stderr.decode()
                logger.error(f"Nmap scan failed: {error_msg}")

        except Exception as e:
            logger.warning(f"Error running Nmap scan: {e}")

        return findings

    def _parse_nmap_xml(self, xml_file: Path, workspace: Path) -> List[ModuleFinding]:
        """Parse Nmap XML output into findings"""
        findings = []

        try:
            tree = ET.parse(xml_file)
            root = tree.getroot()

            # Process each host
            for host_elem in root.findall(".//host"):
                # Get host information
                host_status = host_elem.find("status")
                if host_status is None or host_status.get("state") != "up":
                    continue

                # Get IP address
                address_elem = host_elem.find("address[@addrtype='ipv4']")
                if address_elem is None:
                    address_elem = host_elem.find("address[@addrtype='ipv6']")

                if address_elem is None:
                    continue

                ip_address = address_elem.get("addr")

                # Get hostname if available
                hostname = ""
                hostnames_elem = host_elem.find("hostnames")
                if hostnames_elem is not None:
                    hostname_elem = hostnames_elem.find("hostname")
                    if hostname_elem is not None:
                        hostname = hostname_elem.get("name", "")

                # Get OS information
                os_info = self._extract_os_info(host_elem)

                # Process ports
                ports_elem = host_elem.find("ports")
                if ports_elem is not None:
                    for port_elem in ports_elem.findall("port"):
                        finding = self._process_port(port_elem, ip_address, hostname, os_info)
                        if finding:
                            findings.append(finding)

                # Process host scripts
                host_scripts = host_elem.find("hostscript")
                if host_scripts is not None:
                    for script_elem in host_scripts.findall("script"):
                        finding = self._process_host_script(script_elem, ip_address, hostname)
                        if finding:
                            findings.append(finding)

        except ET.ParseError as e:
            logger.warning(f"Failed to parse Nmap XML: {e}")
        except Exception as e:
            logger.warning(f"Error processing Nmap results: {e}")

        return findings

    def _extract_os_info(self, host_elem) -> Dict[str, Any]:
        """Extract OS information from host element"""
        os_info = {}

        os_elem = host_elem.find("os")
        if os_elem is not None:
            osmatch_elem = os_elem.find("osmatch")
            if osmatch_elem is not None:
                os_info["name"] = osmatch_elem.get("name", "")
                os_info["accuracy"] = osmatch_elem.get("accuracy", "0")

        return os_info

    def _process_port(self, port_elem, ip_address: str, hostname: str, os_info: Dict) -> ModuleFinding:
        """Process a port element into a finding"""
        try:
            port_id = port_elem.get("portid")
            protocol = port_elem.get("protocol")

            # Get state
            state_elem = port_elem.find("state")
            if state_elem is None:
                return None

            state = state_elem.get("state")
            reason = state_elem.get("reason", "")

            # Only report open ports
            if state != "open":
                return None

            # Get service information
            service_elem = port_elem.find("service")
            service_name = ""
            service_version = ""
            service_product = ""
            service_extra = ""

            if service_elem is not None:
                service_name = service_elem.get("name", "")
                service_version = service_elem.get("version", "")
                service_product = service_elem.get("product", "")
                service_extra = service_elem.get("extrainfo", "")

            # Determine severity based on service
            severity = self._get_port_severity(int(port_id), service_name)

            # Get category
            category = self._get_port_category(int(port_id), service_name)

            # Create description
            desc_parts = [f"Open port {port_id}/{protocol}"]
            if service_name:
                desc_parts.append(f"running {service_name}")
            if service_product:
                desc_parts.append(f"({service_product}")
                if service_version:
                    desc_parts.append(f"version {service_version}")
                desc_parts.append(")")

            description = " ".join(desc_parts)

            # Process port scripts
            script_results = []
            script_elems = port_elem.findall("script")
            for script_elem in script_elems:
                script_id = script_elem.get("id", "")
                script_output = script_elem.get("output", "")
                if script_output:
                    script_results.append({"id": script_id, "output": script_output})

            # Create finding
            finding = self.create_finding(
                title=f"Open Port: {port_id}/{protocol}",
                description=description,
                severity=severity,
                category=category,
                file_path=None,  # Network scan, no file
                recommendation=self._get_port_recommendation(int(port_id), service_name, script_results),
                metadata={
                    "host": ip_address,
                    "hostname": hostname,
                    "port": int(port_id),
                    "protocol": protocol,
                    "state": state,
                    "reason": reason,
                    "service_name": service_name,
                    "service_version": service_version,
                    "service_product": service_product,
                    "service_extra": service_extra,
                    "os_info": os_info,
                    "script_results": script_results
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error processing port: {e}")
            return None

    def _process_host_script(self, script_elem, ip_address: str, hostname: str) -> ModuleFinding:
        """Process a host script result into a finding"""
        try:
            script_id = script_elem.get("id", "")
            script_output = script_elem.get("output", "")

            if not script_output or not script_id:
                return None

            # Determine if this is a security issue
            severity = self._get_script_severity(script_id, script_output)

            if severity == "info":
                # Skip informational scripts
                return None

            category = self._get_script_category(script_id)

            finding = self.create_finding(
                title=f"Host Script Result: {script_id}",
                description=script_output.strip(),
                severity=severity,
                category=category,
                file_path=None,
                recommendation=self._get_script_recommendation(script_id, script_output),
                metadata={
                    "host": ip_address,
                    "hostname": hostname,
                    "script_id": script_id,
                    "script_output": script_output.strip()
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error processing host script: {e}")
            return None

    def _get_port_severity(self, port: int, service: str) -> str:
        """Determine severity based on port and service"""
        # High risk ports
        high_risk_ports = [21, 23, 135, 139, 445, 1433, 1521, 3389, 5432, 5900, 6379]
        # Medium risk ports
        medium_risk_ports = [22, 25, 53, 110, 143, 993, 995]
        # Web ports are generally lower risk
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

    def _get_port_category(self, port: int, service: str) -> str:
        """Determine category based on port and service"""
        service_lower = service.lower()

        if service_lower in ["http", "https"] or port in [80, 443, 8080, 8443]:
            return "web_services"
        elif service_lower in ["ssh"] or port == 22:
            return "remote_access"
        elif service_lower in ["ftp", "ftps"] or port in [20, 21]:
            return "file_transfer"
        elif service_lower in ["smtp", "pop3", "imap"] or port in [25, 110, 143, 587, 993, 995]:
            return "email_services"
        elif service_lower in ["mysql", "postgresql", "mssql", "oracle"] or port in [1433, 3306, 5432, 1521]:
            return "database_services"
        elif service_lower in ["rdp"] or port == 3389:
            return "remote_desktop"
        elif service_lower in ["dns"] or port == 53:
            return "dns_services"
        elif port in [135, 139, 445]:
            return "windows_services"
        else:
            return "network_services"

    def _get_script_severity(self, script_id: str, output: str) -> str:
        """Determine severity for script results"""
        script_lower = script_id.lower()
        output_lower = output.lower()

        # High severity indicators
        if any(term in script_lower for term in ["vuln", "exploit", "backdoor"]):
            return "high"
        if any(term in output_lower for term in ["vulnerable", "exploit", "critical"]):
            return "high"

        # Medium severity indicators
        if any(term in script_lower for term in ["auth", "brute", "enum"]):
            return "medium"
        if any(term in output_lower for term in ["anonymous", "default", "weak"]):
            return "medium"

        # Everything else is informational
        return "info"

    def _get_script_category(self, script_id: str) -> str:
        """Determine category for script results"""
        script_lower = script_id.lower()

        if "vuln" in script_lower:
            return "vulnerability_detection"
        elif "auth" in script_lower or "brute" in script_lower:
            return "authentication_testing"
        elif "enum" in script_lower:
            return "information_gathering"
        elif "ssl" in script_lower or "tls" in script_lower:
            return "ssl_tls_testing"
        else:
            return "service_detection"

    def _get_port_recommendation(self, port: int, service: str, scripts: List[Dict]) -> str:
        """Generate recommendation for open port"""
        # Check for script-based issues
        for script in scripts:
            script_id = script.get("id", "")
            if "vuln" in script_id.lower():
                return "Vulnerability detected by NSE scripts. Review and patch the service."

        # Port-specific recommendations
        if port == 21:
            return "FTP service detected. Consider using SFTP instead for secure file transfer."
        elif port == 23:
            return "Telnet service detected. Use SSH instead for secure remote access."
        elif port == 135:
            return "Windows RPC service exposed. Restrict access if not required."
        elif port in [139, 445]:
            return "SMB/NetBIOS services detected. Ensure proper access controls and patch levels."
        elif port == 1433:
            return "SQL Server detected. Ensure strong authentication and network restrictions."
        elif port == 3389:
            return "RDP service detected. Use strong passwords and consider VPN access."
        elif port in [80, 443]:
            return "Web service detected. Ensure regular security updates and proper configuration."
        else:
            return f"Open port {port} detected. Verify if this service is required and properly secured."

    def _get_script_recommendation(self, script_id: str, output: str) -> str:
        """Generate recommendation for script results"""
        if "vuln" in script_id.lower():
            return "Vulnerability detected. Apply security patches and updates."
        elif "auth" in script_id.lower():
            return "Authentication issue detected. Review and strengthen authentication mechanisms."
        elif "ssl" in script_id.lower():
            return "SSL/TLS configuration issue. Update SSL configuration and certificates."
        else:
            return "Review the script output and address any security concerns identified."

    def _create_summary(self, findings: List[ModuleFinding], hosts_count: int) -> Dict[str, Any]:
        """Create analysis summary"""
        severity_counts = {"critical": 0, "high": 0, "medium": 0, "low": 0, "info": 0}
        category_counts = {}
        port_counts = {}
        service_counts = {}
        host_counts = {}

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

            # Count by service
            service = finding.metadata.get("service_name", "unknown")
            service_counts[service] = service_counts.get(service, 0) + 1

            # Count by host
            host = finding.metadata.get("host", "unknown")
            host_counts[host] = host_counts.get(host, 0) + 1

        return {
            "total_findings": len(findings),
            "hosts_scanned": hosts_count,
            "severity_counts": severity_counts,
            "category_counts": category_counts,
            "unique_hosts": len(host_counts),
            "top_ports": dict(sorted(port_counts.items(), key=lambda x: x[1], reverse=True)[:10]),
            "top_services": dict(sorted(service_counts.items(), key=lambda x: x[1], reverse=True)[:10]),
            "host_counts": dict(sorted(host_counts.items(), key=lambda x: x[1], reverse=True)[:5])
        }