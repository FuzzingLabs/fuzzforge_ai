from pathlib import Path
from typing import Dict, Any
from toolbox.modules.base import BaseModule, ModuleResult, ModuleMetadata, ModuleFinding
import requests
import os
import time
import json
from collections import Counter

"""
TODO:
* Configure workspace storage for apk and reports
* Think about mobsf repo implementation inside workflow
* Curl mobsf pdf report
* Save Json mobsf report
* Export Web server interface from the Workflow docker 
"""

class MobSFModule(BaseModule):

    def __init__(self):
        self.mobsf_url = "http://localhost:8877"
        self.file_path = ""
        self.api_key = ""
        self.scan_id = None
        self.scan_hash = ""
        self.report_file = ""
        self._metadata = self.get_metadata()
        self.start_timer()  # <-- Add this line


    def upload_file(self):
        """
        Upload file to MobSF VM
        Returns scan hash if upload succeeded
        """
        # Ensure file_path is set and valid
        if not self.file_path or not os.path.isfile(self.file_path):
            raise ValueError("Invalid or missing file_path for upload.")

        # Don't set Content-Type manually - let requests handle it
        # MobSF expects API key in X-Mobsf-Api-Key header
        headers = {'X-Mobsf-Api-Key': self.api_key}
        
        # Keep the file open during the entire request
        with open(self.file_path, 'rb') as f:
            f.seek(0)
            # Extract just the filename from the full path
            filename = os.path.basename(self.file_path)
            files = {'file': (filename, f, 'application/vnd.android.package-archive')}
            
            # Make the request while the file is still open
            response = requests.post(f"{self.mobsf_url}/api/v1/upload", files=files, headers=headers)
        
        if response.status_code == 200:
            resp_json = response.json()
            if resp_json.get('hash'):
                print("[+] Upload succeeded, scan hash:", resp_json['hash'])
                return resp_json['hash']
            else:
                raise Exception(f"File upload failed: {resp_json}")
        else:
            raise Exception(f"Failed to upload file: {response.text}")

    def start_scan(self, re_scan: int = 0, max_attempts: int = 10, delay: int = 3):
        """
        Scan file that is already uploaded. Retries if scan is not ready.
        Returns scan result or raises Exception after max_attempts.
        """
        print("[+] Starting scan for hash", self.scan_hash)
        data = {'hash': self.scan_hash}
        headers = {'X-Mobsf-Api-Key': self.api_key}
        response = requests.post(f"{self.mobsf_url}/api/v1/scan", data=data, headers=headers)
        if response.status_code == 200:
            try:
                result = response.json()
                # Heuristic: check for expected keys in result
                if result:
                    print("[+] Scan succeeded for hash", self.scan_hash)
                    return result
            except Exception as e:
                print(f"Error parsing scan result: {e}")

    def get_json_results(self):
        """
        Retrieve JSON results for the scanned file
        """
        headers = {'X-Mobsf-Api-Key': self.api_key}
        data = {'hash': self.scan_hash}
        response = requests.post(f"{self.mobsf_url}/api/v1/report_json", data=data, headers=headers)
        if response.status_code == 200:
            f = open('dump.json', 'w').write(json.dumps(response.json(), indent=2))
            print("[+] Retrieved JSON results")
            return response.json()
        else:
            raise Exception(f"Failed to retrieve JSON results: {response.text}")
    
    def create_summary(self, findings):
        """
        Summarize findings by severity.
        Returns a dict like {'high': 3, 'info': 2, ...}
        """
        severity_counter = Counter()
        for finding in findings:
            sev = getattr(finding, "severity", None)
            if sev is None and isinstance(finding, dict):
                sev = finding.get("severity")
            if sev:
                severity_counter[sev] += 1
        res = dict(severity_counter)
        print("Total Findings:", len(findings))
        print("Severity counts:")
        print(res)
        return res



    def parse_json_results(self):
        if self.report_file=="" or not os.path.isfile(self.report_file):
            raise ValueError("Invalid or missing report_file for parsing.")
        f = open(self.report_file, 'r')
        data = json.load(f)
    
        findings = []

        # Check specific sections
        sections_to_parse = ['permissions', 'manifest_analysis', 'code_analysis', 'behaviour']
        
        for section_name in sections_to_parse:
            if section_name in data:
                section = data[section_name]

                #Permissions
                if section_name == 'permissions':
                    for name, attrs in section.items():
                        findings.append(self.create_finding(
                            title=name,
                            description=attrs.get('description'),
                            severity=attrs.get('status'),
                            category="permission",
                            metadata={
                                'info': attrs.get('info'),
                            }
                            ))

                #Manifest Analysis
                elif section_name == 'manifest_analysis':
                    findings_list = section.get('manifest_findings', [])
                    for x in findings_list:
                        findings.append(self.create_finding(
                            title=attrs.get('title') or attrs.get('name') or "unknown",
                            description=attrs.get('description', "No description"),
                            severity=attrs.get('severity', "unknown"),
                            category=section_name,
                            metadata={
                                'tag': attrs.get('rule')
                            }))
                #Code Analysis
                elif section_name == 'code_analysis':
                    findings_list = section.get('findings', [])
                    for name, attrs in findings_list.items():
                        metadata = attrs.get('metadata', {})
                        findings.append(self.create_finding(
                            title=name,
                            description=metadata.get('description'),
                            severity=metadata.get('severity'),
                            category="code_analysis",
                            metadata={
                                    'cwe': metadata.get('cwe'),
                                    'owasp': metadata.get('owasp'),
                                    'files': attrs.get('file')
                            }))

                #Behaviour
                elif section_name == 'behaviour':
                    finding = []
                    for key, value in data['behaviour'].items():
                        metadata = value.get('metadata', {})
                        findings.append(self.create_finding(
                            title="behaviour_"+metadata.get('label')[0],
                            description=metadata.get('description'),
                            severity=metadata.get('severity'),
                            category="behaviour",
                            metadata={
                            'file': value.get('files', {})
                            }
                        ))
        return findings
    
    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        findings = []

        #Checking that mobsf server is reachable
        self.mobsf_url = config.get("mobsf_url", "")
        self.file_path = config.get("file_path", "")
        # Get API key from config first, fallback to environment variable
        self.api_key = config.get("api_key", "") or os.environ.get("MOBSF_API_KEY", "")
        #Checking that the file to scan exists
        file_path = config.get("file_path", None)
        if not file_path or not os.path.isfile(file_path):
            raise ValueError(f"Invalid or missing file_path in configuration: {file_path}")

        try:
            self.scan_hash = self.upload_file()
        except Exception as e:
            raise Exception(f"Failed to upload file to MobSF: {e}")

        if self.scan_hash=="":
            raise Exception("scan_hash not returned after upload.")
        try:
            scan_result = self.start_scan()
        except Exception as e:
            raise Exception(f"Failed to scan file in MobSF: {e}")

        # Parse scan_result and convert to findings
        # This is a placeholder; actual parsing logic will depend on MobSF's JSON structure
        # Here we just create a dummy finding for illustration 

        try:
            json_data = self.get_json_results()
        except json.JSONDecodeError:
            return self.create_result(
                findings=[],
                status="failed",
                summary={"error": "Invalid JSON output from MOBSF"},
                metadata={"engine": "mobsf", "file_scanned": file_path, "mobsf_url": root_uri}
            )    
        
        self.report_file = 'dump.json'
        findings = self.parse_json_results()
        """
        findings.append(ModuleFinding(
            title="MobSF Finding",
            description="Finding generated by the MobSF module",
            severity="medium",
            category="mobsf",
            metadata={"scan_result": scan_result}
        ))
        """   
        tmp_summary = self.create_summary(findings)
        summary = {
            "total_findings": len(findings),
            "dangerous_severity": tmp_summary.get('dangerous', 0),
            "warning_severity": tmp_summary.get('warning', 0),
            "high_severity": tmp_summary.get('high', 0),
            "medium_severity": tmp_summary.get('medium', 0),
            "low_severity": tmp_summary.get('low', 0),
            "info_severity": tmp_summary.get('info', 0),
        }
        metadata={"engine": "mobsf", "file_scanned": file_path, "mobsf_url": self.mobsf_url}#Add: "json_report": str(json_output_path

        return self.create_result(findings=findings, status="success",summary=summary, metadata=metadata)
        return ModuleResult(findings=findings, status="success",summary=summary, metadata=metadata)


    def get_metadata(self) -> ModuleMetadata:
        return ModuleMetadata(
            name="Mobile Security Framework (MobSF)",
            version="1.0.0",
            description="Integrates MobSF for mobile app security scanning",
            author="FuzzForge Team",
            category="scanner",
            tags=["mobsf", "mobile", "sast", "scanner"]
        )
    
    def validate_config(self, config: Dict[str, Any]) -> bool:
        """
        Config pattern:
        **config
        findings: []
        "tool_name": "FuzzForge Hello World",
        "tool_version": "1.0.0",
        "mobsf_uri": "(default: http://localhost:8000)",
        "file_path": "(path to the APK or IPA file to scan)"
        """
        if "mobsf_url" in config and not isinstance(config["mobsf_url"], str):
            return False
        # Check that mobsf_url does not render 404 when curling /
   
        if "file_path" in config and not isinstance(config["file_path"], str):
                return False
        return True

if __name__ == "__main__":
    import asyncio
    module = MobSFModule()
    config = {
        "mobsf_url": "http://localhost:8877",
        "file_path": "./toolbox/modules/android/beetlebug.apk",
    }
    workspace = Path("./toolbox/modules/android/")
    result = asyncio.run(module.execute(config, workspace))
    print(result)